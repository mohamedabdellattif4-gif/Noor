package com.noor.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollReader() {
        benchmarkRule.measureRepeated(
            packageName = NOOR_PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = ITERATIONS,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                openAlFatiha()
            },
        ) {
            scrollReader(count = SCROLLS_PER_ITERATION)
        }
    }

    private companion object {
        const val ITERATIONS = 10
        const val SCROLLS_PER_ITERATION = 4
    }
}
