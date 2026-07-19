package com.noor.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupWithoutCompilation() = measureStartup(CompilationMode.None())

    @Test
    fun coldStartupWithBaselineProfile() = measureStartup(
        CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
    )

    private fun measureStartup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = NOOR_PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = ITERATIONS,
            setupBlock = { pressHome() },
        ) {
            startActivityAndWait()
        }
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
