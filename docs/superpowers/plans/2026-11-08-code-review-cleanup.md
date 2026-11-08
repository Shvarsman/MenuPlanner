# Implementation Plan: Code Review Cleanup, Architecture Realignment & UI/UX Polish

**Date**: 2026-11-08  
**Design Document**: [docs/superpowers/specs/2026-11-08-code-review-cleanup-design.md](file:///Users/shvarsman/developer/MenuPlanner/docs/superpowers/specs/2026-11-08-code-review-cleanup-design.md)

---

## Overview
This plan implements the architectural decoupling, code smell eliminations, UX enhancements, documentation alignment, and UI design system polish approved across the review and critique passes.

---

## Tasks

### Phase 1: Architecture & Standards (Clean Architecture)

#### Task 1: Decouple `DemoDataViewModel` from Concrete Data Layer
- [x] Create domain interface `com.shvarsman.coolinar.domain.repository.DemoDataRepository`:
  - Methods: `suspend fun purgeAll()`, `suspend fun purgeAllExceptRecipes()`
- [x] Update `DemoDataRepositoryImpl` to implement `DemoDataRepository`.
- [x] Register `@Binds abstract fun bindDemoDataRepository(impl: DemoDataRepositoryImpl): DemoDataRepository` in `di/RepositoryModule.kt`.
- [x] Update `DemoDataViewModel` constructor to inject `DemoDataRepository` instead of `DemoDataRepositoryImpl`.

#### Task 2: Decouple `RecipeEditorViewModel` from Concrete `ImageFileManager`
- [x] Create domain interface `com.shvarsman.coolinar.domain.repository.ImageStorageRepository`:
  - Methods: `suspend fun persistImage(sourceUri: Uri): String`, `suspend fun deleteImage(uriString: String): Boolean`.
- [x] Update `ImageFileManager` to implement `ImageStorageRepository`.
- [x] Register `@Binds abstract fun bindImageStorageRepository(impl: ImageFileManager): ImageStorageRepository` in `di/RepositoryModule.kt`.
- [x] Update `RecipeEditorViewModel` to inject `ImageStorageRepository` instead of `ImageFileManager`.

#### Task 3: Decouple `ProfileSettingsViewModel` from Data Layer & Remote Storage
- [x] Add avatar management methods to `com.shvarsman.coolinar.domain.repository.AuthRepository`:
  - `suspend fun uploadAvatar(sourceUri: Uri): Result<String>`
  - `suspend fun removeAvatar(): Result<Unit>`
- [x] Implement `uploadAvatar` and `removeAvatar` in `FirebaseAuthRepositoryImpl`:
  - Inject `ImageFileManager` and `RemoteImageUploader`.
  - Handle local copy -> remote upload -> local cleanup -> update Firebase profile photo URL.
- [x] Update `ProfileSettingsViewModel`:
  - Remove injections and imports of `ImageFileManager` and `RemoteImageUploader`.
  - Update `updateAvatar` and `removeAvatar` to call `authRepository.uploadAvatar` and `authRepository.removeAvatar`.

#### Task 4: Deduplicate Auth Error Mapping
- [x] Create `presentation/utils/AuthErrorMapper.kt` with extension function `fun Throwable.toAuthErrorRes(): Int`.
- [x] Replace private `toErrorRes()` duplicates in:
  - `ProfileViewModel.kt`
  - `ProfileSettingsViewModel.kt`
  - `OnboardingViewModel.kt`

#### Task 5: Naming Alignment for `HomeViewModel`
- [x] In `HomeViewModel.kt`, rename `class MenuViewModel` to `class HomeViewModel`.
- [x] In `HomeScreen.kt`, update `viewModel: MenuViewModel = hiltViewModel()` to `viewModel: HomeViewModel = hiltViewModel()`.
- [x] In `WeekMenuScreen.kt`, update `viewModel: MenuViewModel = hiltViewModel()` to `viewModel: HomeViewModel = hiltViewModel()`.

---

### Phase 2: UI/UX Polish & Design Tokens

#### Task 6: Unify Design System Tokens (Color Palette & Dark Mode)
- [x] Clean up `Color.kt` and `Theme.kt`:
  - Eliminate leftover iOS system color tokens (`#F2F2F7`, `#34C759`, `#30D158`, `0x993C3C43`).
  - Standardize on warm culinary palette (terracotta primary `#AC7F5E`, fresh sage secondary `#4A7C59`, warm natural backgrounds).
  - Fix translucent `LightSurface = Color(0x1F767680)` to an opaque, contrast-safe surface token.
  - Introduce tiered corner radius scale: `ShapeSmall` (8dp), `ShapeMedium` (16dp), `ShapeLarge` (24dp) instead of uniform 28dp everywhere.
  - Clean up pseudo-gradients in `ModifierExtensions.kt`.

#### Task 7: Establish Material 3 Typography Scale
- [x] In `Type.kt`:
  - Define complete semantic scale: `displayMedium`, `headlineMedium`, `headlineSmall`, `titleLarge`, `titleMedium`, `titleSmall`, `bodyLarge`, `bodyMedium`, `bodySmall`, `labelLarge`, `labelMedium`, `labelSmall`.
  - Retain `molleFont` for logo/branding, pairing it cleanly with the content typography scale.

#### Task 8: Fix Button Action Hierarchy in `CookingScreen`
- [x] In `CookingScreen.kt`:
  - Consolidate the two stacked bottom buttons into a single primary action button with contextual states:
    - Step active: "Отметить готовым" (primary filled with checkmark).
    - Dish finished but others remaining: "Блюдо готово" badge with happy mascot and secondary "Завершить готовку" action.
    - All dishes done: celebration mascot with single "Завершить готовку" primary button.

#### Task 9: Recipe Ingredient Selection Filter ("In Fridge")
- [x] In `ProductPickerDialog.kt`:
  - Add optional parameter `fridgeProductIds: Set<String>? = null`.
  - Add filter chips for "Все" / "В холодильнике" when `!fridgeProductIds.isNullOrEmpty()`.
  - Filter `filtered` list accordingly when toggled.
- [x] In `RecipeEditorScreen.kt`:
  - Pass `fridgeProductIds = fridgeItems.map { it.productId }.toSet()` into `ProductPickerDialog`.
- [x] Add string resources in `res/values/strings.xml` and `res/values-ru/strings.xml`.

---

### Phase 3: Documentation & Verification

#### Task 10: Update `README.md`
- [x] Rewrite architecture section to document `com.shvarsman.coolinar.di` and clean layer boundaries.
- [x] Document full v2.0 feature set (Firebase Auth, Firestore sync, interactive cooking, tour & mascot, product catalog).
- [x] Document the accurate bidirectional shopping list & fridge flow.
- [x] Clean up completed roadmap items.

#### Task 11: Build Verification
- [x] Check all file modifications, package imports, and signatures.
- [x] Verify git diff.
