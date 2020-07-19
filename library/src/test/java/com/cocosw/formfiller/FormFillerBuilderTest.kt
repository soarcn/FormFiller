package com.cocosw.formfiller

import android.app.Application
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class FormFillerBuilderTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = mockk<Application>(relaxed = true)
        FormFiller.instance = null
    }

    @Test
    fun builderWithoutTrigger() {
        try {
            FormFiller.Builder(app).build()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun builderWithDoubleTap() {
        var filler = FormFiller.Builder(app).keyCode(1).build()
        assertThat(filler.doubleTap).isFalse()
        FormFiller.instance = null
        filler = FormFiller.Builder(app).doubleTap().build()
        assertThat(filler.doubleTap).isTrue()
    }

    @Test
    fun builderWithKeyCodes() {
        var filler = FormFiller.Builder(app).doubleTap().build()
        assertThat(filler.keycodes).isEmpty()
        FormFiller.instance = null
        filler = FormFiller.Builder(app).keyCode(1).build()
        assertThat(filler.keycodes).hasSize(1)
        FormFiller.instance = null
        filler = FormFiller.Builder(app)
            .keyCode(1)
            .keyCode(2, false)
            .build()
        assertThat(filler.keycodes).hasSize(2)
        assertThat(filler.keycodes[1]).isTrue()
        assertThat(filler.keycodes[2]).isFalse()
    }

    @Test
    fun builderWithSwitcher() {
        var filler = FormFiller.Builder(app).doubleTap().build()
        assertThat(filler.enableSwitcher).isFalse()
        FormFiller.instance = null
        filler = FormFiller.Builder(app).doubleTap().enableScenariosSwitcher().build()
        assertThat(filler.enableSwitcher).isTrue()
    }

    @Test
    fun builderWithoutScenario() {
        var filler = FormFiller.Builder(app).doubleTap().build()
        assertThat(filler.currentScenario).isNotNull()
        assertThat(filler.currentScenario.ids).isEmpty()
        assertThat(filler.currentScenario.tags).isEmpty()
    }

    @Test
    fun useDefaultAsScenarioName() {
        try {
            FormFiller.Builder(app).doubleTap()
                .scenario("Default") {
                    id(1, "test")
                }
                .build()
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

}