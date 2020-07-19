package com.cocosw.formfiller

import android.app.Application
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ScenarioTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = mockk<Application>(relaxed = true)
        FormFiller.instance = null
    }

    @Test
    fun scenarioWithId() {
        var filler = FormFiller.Builder(app).doubleTap()
            .scenario {
                id(1, "test")
                id(2) {

                }
            }
            .build()

        assertThat(filler.currentScenario).isNotNull()
        assertThat(filler.currentScenario.ids).hasSize(2)
        assertThat(filler.currentScenario.ids[1]?.first).isEqualTo("test")
        assertThat(filler.currentScenario.ids[1]?.second).isNull()
        assertThat(filler.currentScenario.ids[2]?.first).isNull()
        assertThat(filler.currentScenario.ids[2]?.second).isNotNull()
    }

    @Test
    fun scenarioWithTag() {
        var filler = FormFiller.Builder(app).doubleTap()
            .scenario {
                tag("test", "test")
                tag("block") {}
            }
            .build()

        assertThat(filler.currentScenario).isNotNull()
        assertThat(filler.currentScenario.tags).hasSize(2)
        assertThat(filler.currentScenario.tags["test"]?.first).isEqualTo("test")
        assertThat(filler.currentScenario.tags["test"]?.second).isNull()
        assertThat(filler.currentScenario.tags["block"]?.first).isNull()
        assertThat(filler.currentScenario.tags["block"]?.second).isNotNull()
    }

    @Test
    fun invalidScenarioName() {
        var filler = FormFiller.Builder(app).doubleTap()
            .scenario {
                tag("test", "test")
            }
            .build()

        try {
            filler.changeScenario("invalid")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun switchScenario() {
        var filler = FormFiller.Builder(app).doubleTap()
            .scenario {
                tag("test", "test")
            }.scenario("test") {
                id(1, "test")
                tag("test", "newtest")
            }
            .build()

        assertThat(filler.currentScenario).isNotNull()
        assertThat(filler.currentScenario.tags).hasSize(1)
        assertThat(filler.currentScenario.ids).isEmpty()
        assertThat(filler.currentScenario.tags["test"]?.first).isEqualTo("test")

        filler.changeScenario("test")

        assertThat(filler.currentScenario.tags).hasSize(1)
        assertThat(filler.currentScenario.ids).hasSize(1)

        assertThat(filler.currentScenario.tags["test"]?.first).isEqualTo("newtest")
    }
}