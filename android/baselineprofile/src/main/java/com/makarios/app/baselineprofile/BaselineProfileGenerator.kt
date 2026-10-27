package com.makarios.app.baselineprofile

import androidx.benchmark.macro.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = "com.makarios.app",
        includeInStartupProfile = true
    ) {
        startActivityAndWait()
    }
}