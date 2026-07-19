package com.noor.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

internal const val NOOR_PACKAGE_NAME = "com.noor.app"
internal const val UI_TIMEOUT_MS = 10_000L

internal fun MacrobenchmarkScope.openAlFatiha() {
    val surah = device.wait(
        Until.findObject(By.text("الفاتحة")),
        UI_TIMEOUT_MS,
    ) ?: error("Al-Fatiha did not become visible within $UI_TIMEOUT_MS ms")
    surah.click()
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.scrollReader(
    count: Int,
    steps: Int = 20,
) {
    repeat(count) {
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            device.displayWidth / 2,
            device.displayHeight / 4,
            steps,
        )
        device.waitForIdle()
    }
}
