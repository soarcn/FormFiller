package com.cocosw.formfiller

import android.app.Application
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThrows

class ScenarioValueModelTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = mockk(relaxed = true)
        FormFiller.instance = null
    }

    @Test
    fun matchedValueProviderIsResolvedOncePerFillRun() {
        var evaluations = 0
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario {
                tag("email") {
                    evaluations += 1
                    "developer@example.com"
                }
            }
            .build()

        val entry = filler.currentScenario.newSnapshot().entries.single()

        assertThat(evaluations).isEqualTo(0)
        assertThat(entry.resolve()).isEqualTo(
            FillValueResolution.Value("developer@example.com")
        )
        assertThat(entry.resolve()).isEqualTo(
            FillValueResolution.Value("developer@example.com")
        )
        assertThat(evaluations).isEqualTo(1)
    }

    @Test
    fun effectiveScenarioEntriesCannotBeChangedDuringAFillRun() {
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario { tag("email", "default@example.com") }
            .build()
        val entries = filler.currentScenario.newSnapshot().entries

        assertThrows(UnsupportedOperationException::class.java) {
            (entries as MutableList).clear()
        }
    }

    @Test
    fun staticFillValueCannotChangeAfterFillRunStarts() {
        val configuredValue = StringBuilder("original@example.com")
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario { tag("email", configuredValue) }
            .build()
        val entry = filler.currentScenario.newSnapshot().entries.single()

        configuredValue.replace(0, configuredValue.length, "changed@example.com")

        assertThat(entry.resolve()).isEqualTo(
            FillValueResolution.Value("original@example.com")
        )
    }

    @Test
    fun providedFillValueCannotChangeDuringAFillRun() {
        val providedValue = StringBuilder("first@example.com")
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario { tag("email") { providedValue } }
            .build()
        val entry = filler.currentScenario.newSnapshot().entries.single()

        assertThat(entry.resolve()).isEqualTo(
            FillValueResolution.Value("first@example.com")
        )
        providedValue.replace(0, providedValue.length, "second@example.com")

        assertThat(entry.resolve()).isEqualTo(
            FillValueResolution.Value("first@example.com")
        )
    }

    @Test
    fun namedScenarioOverridesDefaultInDeterministicSelectorOrder() {
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario {
                id(10, "default-id-10")
                tag("first-tag", "default-first")
                tag("overridden-tag", "default-overridden")
                id(20, "default-id-20")
            }
            .scenario("Named") {
                id(10) { "named-id-10" }
                tag("overridden-tag", "named-overridden")
                tag("named-tag", "named-only")
            }
            .build()

        filler.changeScenario("Named")
        val entries = filler.currentScenario.newSnapshot().entries

        assertThat(entries.map { it.selector }).containsExactly(
            ScenarioSelector.TargetTag("first-tag"),
            ScenarioSelector.TargetTag("overridden-tag"),
            ScenarioSelector.TargetTag("named-tag"),
            ScenarioSelector.ResourceId(10),
            ScenarioSelector.ResourceId(20)
        ).inOrder()
        assertThat(entries.map { it.resolve() }).containsExactly(
            FillValueResolution.Value("default-first"),
            FillValueResolution.Value("named-overridden"),
            FillValueResolution.Value("named-only"),
            FillValueResolution.Value("named-id-10"),
            FillValueResolution.Value("default-id-20")
        ).inOrder()
    }

    @Test
    fun providerFailureIsMemoizedAndDoesNotStopUnrelatedEntries() {
        var failedEvaluations = 0
        var successfulEvaluations = 0
        var unmatchedEvaluations = 0
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario {
                tag("broken") {
                    failedEvaluations += 1
                    error("sensitive provider detail")
                }
                tag("healthy") {
                    successfulEvaluations += 1
                    "safe value"
                }
                tag("unmatched") {
                    unmatchedEvaluations += 1
                    "unused value"
                }
            }
            .build()
        val entries = filler.currentScenario.newSnapshot().entries

        assertThat(entries[0].resolve()).isEqualTo(FillValueResolution.ProviderFailure)
        assertThat(entries[0].resolve()).isEqualTo(FillValueResolution.ProviderFailure)
        assertThat(entries[1].resolve()).isEqualTo(FillValueResolution.Value("safe value"))
        assertThat(failedEvaluations).isEqualTo(1)
        assertThat(successfulEvaluations).isEqualTo(1)
        assertThat(unmatchedEvaluations).isEqualTo(0)
    }

    @Test
    fun fillRunKeepsScenarioCapturedBeforeScenarioChange() {
        val filler = FormFiller.Builder(app)
            .doubleTap()
            .scenario { tag("email", "default@example.com") }
            .scenario("Named") { tag("email", "named@example.com") }
            .build()
        val capturedEntry = filler.currentScenario.newSnapshot().entries.single()

        filler.changeScenario("Named")

        assertThat(capturedEntry.resolve()).isEqualTo(
            FillValueResolution.Value("default@example.com")
        )
        assertThat(filler.currentScenario.newSnapshot().entries.single().resolve()).isEqualTo(
            FillValueResolution.Value("named@example.com")
        )
    }
}
