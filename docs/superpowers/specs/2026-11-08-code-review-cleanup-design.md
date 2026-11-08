# Design Document: Code Review Cleanup & Architecture Realignment

**Date**: 2026-11-08  
**Topic**: Code Review Cleanup & Architecture Realignment  
**Scope**: Project-level refactoring and spec synchronization

---

## 1. Intent and Why

During a full project review of MenuPlanner (Coolinar), several architectural layer breaches, code duplication smells, and divergences between the codebase and the documentation were identified:
- ViewModels in the `presentation` layer directly reference concrete classes in `data` (`DemoDataRepositoryImpl`, `ImageFileManager`, `RemoteImageUploader`), violating Clean Architecture layer boundaries.
- Authentication error-to-resource resolution logic is duplicated across multiple ViewModels.
- Naming inconsistencies exist between files and classes (`HomeViewModel.kt` vs `MenuViewModel`).
- The project's actual capabilities (Firebase Auth, Firestore sync, interactive cooking mode, onboarding tour, product catalog) significantly surpass the original `README.md` spec, while certain spec behaviors (such as selecting ingredients specifically from the fridge) remained unfulfilled.

The goal is to eliminate technical debt, restore Clean Architecture boundaries, improve user experience in ingredient selection, and synchronize project documentation with modern reality.

---

## 2. Goals, Non-Goals, and Anti-Goals

### Goals
1. **Clean Architecture Adherence**: Ensure `presentation` classes depend exclusively on `domain` interfaces and use cases, never on concrete `data` classes or internal file managers.
2. **Deduplication**: Centralize error-handling logic for authentication across ViewModels.
3. **Naming Consistency**: Resolve naming confusion between `HomeViewModel.kt` and `MenuViewModel`.
4. **UX Spec Alignment**: Enable filtering by "In Fridge" (`В холодильнике`) in `ProductPickerDialog` when choosing recipe ingredients in `RecipeEditorScreen`.
5. **Documentation Accuracy**: Rewrite `README.md` to reflect the true v2.0 architecture, stack, feature set, and real shopping-list workflow, removing outdated roadmap items.

### Non-Goals
- Full rewrite of all single-line use cases (Middle Man smell). We intentionally do not alter the broader use-case pattern across the entire application in this iteration to avoid unnecessary regressions in stable features.
- Major UI redesign of screens outside the scope of the review findings.
- Database schema migrations (all existing Room and Firestore models remain stable).

### Anti-Goals
- Introducing any runtime behavior breaks or data loss during tour/demo cleanup, avatar uploads, or recipe saving.
- Breaking offline-first semantics of Room and Firestore sync.

---

## 3. Constraints
- Kotlin 2.x, Jetpack Compose, Material 3.
- Android minSdk 24, targetSdk 34.
- Dagger Hilt dependency injection.
- Room database for local caching and offline-first storage.
- Clean Architecture dependencies: `presentation` -> `domain` <- `data`.

---

## 4. Approaches Considered

### Approach 1: Architecture Decoupling + UX Alignment (Chosen)
- Introduce domain repository interfaces for `DemoDataRepository` and `ImageStorageRepository`.
- Move avatar upload/deletion orchestration into `AuthRepository`.
- Extract duplicated `toErrorRes()` into an auth error mapper utility.
- Rename `MenuViewModel` in `HomeViewModel.kt` to `HomeViewModel` (or align file and class names consistently).
- Add an "In Fridge" filter tab in `ProductPickerDialog` for recipe ingredient selection.
- Update `README.md` comprehensively.
- **Trade-off**: Solves all critical hard violations, fixes code smells, and aligns product behavior with spec without risking regressions across unrelated use-case consumers.

### Approach 2: Deep Clean (Aggressive Use-Case Pruning)
- Everything in Approach 1, plus deleting 15+ trivial pass-through UseCases in `domain/usecase/preferences/` and `domain/usecase/recipe/`, injecting repositories directly into ViewModels.
- **Trade-off**: Reduces boilerplate significantly, but requires modifying constructor signatures of numerous ViewModels and DI modules, increasing the surface area for build and runtime regressions.

### Approach 3: Minimalist Fix
- Only fix the 3 presentation-to-data imports and update `README.md`. No UX changes (no fridge ingredient filter) and no smell cleanup.
- **Trade-off**: Quickest, but leaves baseline code smells intact and ignores user-facing gaps.

**Decision**: Approach 1 was selected as it balances architectural purity, code hygiene, and user experience without unnecessary churn.

---

## 5. Detailed Design & Changes

### 5.1 Domain Layer Interfaces
1. **`DemoDataRepository` (`domain/repository/DemoDataRepository.kt`)**:
   ```kotlin
   package com.shvarsman.coolinar.domain.repository

   interface DemoDataRepository {
       suspend fun purgeAll()
       suspend fun purgeAllExceptRecipes()
   }
   ```
   - Implemented by `data/repository/DemoDataRepositoryImpl.kt`.
   - Bound in `di/RepositoryModule.kt` as `@Binds abstract fun bindDemoDataRepository(...)`.
   - `DemoDataViewModel` injects `DemoDataRepository` instead of `DemoDataRepositoryImpl`.

2. **`ImageStorageRepository` (`domain/repository/ImageStorageRepository.kt`)**:
   ```kotlin
   package com.shvarsman.coolinar.domain.repository

   import android.net.Uri

   interface ImageStorageRepository {
       suspend fun persistImage(sourceUri: Uri): Uri
       suspend fun deleteImage(uri: Uri): Boolean
   }
   ```
   - Implemented by `data/local/ImageFileManager.kt` (or an adapter).
   - Injected into `RecipeEditorViewModel` instead of the concrete `ImageFileManager`.

3. **Avatar Management in `AuthRepository` (`domain/repository/AuthRepository.kt`)**:
   - Add methods to `AuthRepository`:
     ```kotlin
     suspend fun uploadAvatar(sourceUri: Uri): Result<String>
     suspend fun removeAvatar(): Result<Unit>
     ```
   - `FirebaseAuthRepositoryImpl` takes responsibility for calling `ImageFileManager` to persist locally, `RemoteImageUploader` to send to Firebase Storage, deleting local temporary files, and calling `updatePhotoUrl`.
   - `ProfileSettingsViewModel` no longer injects `ImageFileManager` or `RemoteImageUploader`, keeping presentation decoupled from data/storage infrastructure.

### 5.2 Baseline Smells & Naming Cleanup
1. **Auth Error Mapping Consolidation (`presentation/utils/AuthErrorMapper.kt`)**:
   ```kotlin
   package com.shvarsman.coolinar.presentation.utils

   import com.shvarsman.coolinar.R
   import com.shvarsman.coolinar.domain.model.AuthException

   fun Throwable.toAuthErrorRes(): Int = when (this) {
       is AuthException.InvalidEmail -> R.string.profile_error_invalid_email
       is AuthException.WeakPassword -> R.string.profile_error_weak_password
       is AuthException.WrongCredentials -> R.string.profile_error_wrong_credentials
       is AuthException.EmailAlreadyInUse -> R.string.profile_error_email_in_use
       is AuthException.NetworkError -> R.string.profile_error_network
       else -> R.string.profile_error_generic
   }
   ```
   - Replace private implementations in `ProfileViewModel.kt`, `ProfileSettingsViewModel.kt`, and `OnboardingViewModel.kt`.

2. **ViewModel & File Name Alignment**:
   - In `HomeViewModel.kt`, rename `class MenuViewModel` to `class HomeViewModel`.
   - Update usages in `HomeScreen.kt` and `WeekMenuScreen.kt`.

### 5.3 Recipe Ingredient Selection from Fridge (UX Alignment)
1. **`ProductPickerDialog.kt`**:
   - Add parameter `fridgeProductIds: Set<String>? = null`.
   - When `fridgeProductIds` is non-null and not empty, render a filter toggle/segmented chip: `Все` (All) vs `В холодильнике` (In Fridge).
   - When "В холодильнике" is selected, the list filters products where `it.id in fridgeProductIds`.
2. **`RecipeEditorScreen.kt`**:
   - Pass `fridgeItems.map { it.productId }.toSet()` into `ProductPickerDialog`.

### 5.4 Spec & Documentation Synchronization
1. **`README.md`**:
   - Update stack section: Add Firebase Auth, Firestore offline sync, Coil, Material 3 with Dynamic Color.
   - Update architecture section: Document `com.shvarsman.coolinar.di` for Hilt dependency injection.
   - Clarify shopping list feature: Document that purchased items move into the fridge, and missing ingredients from meal planning auto-populate the shopping list.
   - Document new modules: Interactive Cooking mode with step-by-step guidance and timers, guided tour with mascot tips, onboarding, and product catalog management.
   - Remove completed roadmap items (portion adjustment, recipe cards showing cooking method and time, snackbar with undo on deletion).

---

## 6. What's Left to the Builder
- Visual styling details for the filter chips in `ProductPickerDialog` (using existing Material 3 `FilterChip` or `SuggestionChip`).
- Exact string resources for any new UI labels (e.g. `R.string.filter_in_fridge`, `R.string.filter_all`).
- Minor Gradle dependency verification during build.
