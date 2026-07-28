package com.cocosw.formfiller

import android.app.Application
import android.view.View.NO_ID
import android.widget.EditText
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ViewFillerTest {

    private lateinit var filler: FormFiller
    private lateinit var app: Application
    private val view = mockk<EditText>(relaxed = true)

    @Before
    fun setup() {
        app = mockk<Application>(relaxed = true)
        FormFiller.instance = null
        filler = FormFiller.Builder(app).doubleTap()
            .scenario {
                id(NO_ID, "must-not-fill")
                id(1, "idtest")
                tag("test", "tagtest")
            }.scenario("test") {
                id(1, "newidtest")
                tag("test", "newtagtest")
            }
            .build()
    }

    @Test
    fun fillViewWithTag() {
        every { view.tag }.returns("test")
        filler.fill(view, filler.currentScenario.newSnapshot())
        verify { view.setText("tagtest") }
        filler.changeScenario("test")
        filler.fill(view, filler.currentScenario.newSnapshot())
        verify { view.setText("newtagtest") }
    }

    @Test
    fun fillViewWithId() {
        every { view.id }.returns(1)
        every { view.tag }.returns("notme")
        filler.fill(view, filler.currentScenario.newSnapshot())
        verify { view.setText("idtest") }
        filler.changeScenario("test")
        filler.fill(view, filler.currentScenario.newSnapshot())
        verify { view.setText("newidtest") }
    }

    @Test
    fun viewWithoutResourceIdDoesNotMatchNoIdSelector() {
        every { view.id }.returns(NO_ID)
        every { view.tag }.returns(null)

        val filled = filler.fill(view, filler.currentScenario.newSnapshot())

        assertThat(filled).isFalse()
        verify(exactly = 0) { view.setText(any<CharSequence>()) }
    }
}
