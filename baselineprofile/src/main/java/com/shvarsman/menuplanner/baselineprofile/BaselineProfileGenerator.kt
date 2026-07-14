package com.shvarsman.menuplanner.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.shvarsman.menuplanner",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        // Warm bottom navigation destinations used on cold start / first session.
        device.wait(Until.hasObject(By.text("Меню")), 5_000)
        device.findObject(By.text("Холодильник"))?.click()
        device.waitForIdle()
        device.findObject(By.text("Рецепты"))?.click()
        device.waitForIdle()
        device.findObject(By.text("Покупки"))?.click()
        device.waitForIdle()
        device.findObject(By.text("Меню"))?.click()
        device.waitForIdle()
    }
}
