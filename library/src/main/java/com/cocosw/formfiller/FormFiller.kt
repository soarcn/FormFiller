package com.cocosw.formfiller

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import android.widget.EditText

class FormFiller internal constructor(
    internal val scenarios: Map<String, Scenario>
    , internal val keycodes: Map<Int, Boolean>
    , internal val doubleTap: Boolean,
    internal val enableSwitcher: Boolean
) {
    private var currentScenario: Scenario = scenarios[DEFAULT_SCENARIO] ?: Scenario()

    internal fun fill(view: EditText) {
        currentScenario.fill(view)
    }

    fun changeScenario(scenario: String) {
        scenarios[scenario]?.apply {
            currentScenario = scenarios[DEFAULT_SCENARIO]?.merge(this) ?: this
        }
    }

    companion object {
        private val DEFAULT_SCENARIO = "Default"
        private var instance: FormFiller? = null
        internal fun getInstant() = instance!!
    }


    class Builder(private val application: Application) {
        private var enableSwitcher: Boolean = false
        private val scenarios = mutableMapOf<String, Scenario>()
        private val keycodes = mutableMapOf<Int, Boolean>()
        private var doubleTap = false

        /**
         * Add a keycode that will trigger form filler
         *
         * @param keyCode keycode from [android.view.KeyEvent]
         * @param externalOnly only respond to external keyboard event
         */
        fun keyCode(keyCode: Int, externalOnly: Boolean = true): Builder {
            keycodes[keyCode] = externalOnly
            return this
        }

        /**
         * Allow double tap to trigger form filler
         */
        fun doubleTap(): Builder {
            doubleTap = true
            return this
        }

        fun scenario(block: Scenario.() -> Unit): Builder {
            scenarios[DEFAULT_SCENARIO] = Scenario().apply(block)
            return this
        }

        fun scenario(name: String, block: Scenario.() -> Unit): Builder {
            if (name == DEFAULT_SCENARIO) {
                throw IllegalArgumentException("Please do not use [Default] for scenario name")
            }
            scenarios[name] = Scenario().apply(block)
            return this
        }

        fun enableScenariosSwitcher(): Builder {
            enableSwitcher = true
            return this
        }

        fun build() {
            if (instance == null) {
                if (keycodes.isEmpty() && !doubleTap) {
                    throw IllegalArgumentException("You must define one trigger by calling keyCode() or doubleTap()")
                }

                val instance = FormFiller(scenarios, keycodes, doubleTap, enableSwitcher)
                FormFiller.instance = instance
                application.registerActivityLifecycleCallbacks(FormFillerActivityLifeCycle(instance))
            }
        }
    }

    class Scenario {
        private var tags = mutableMapOf<String, Pair<CharSequence?, Callback?>>()
        private val ids = mutableMapOf<Int, Pair<CharSequence?, Callback?>>()
        private val hints = mutableMapOf<String, Pair<CharSequence?, Callback?>>()

        fun id(id: Int, value: CharSequence? = null, block: Callback? = null) {
            ids[id] = value to block
        }

        fun tag(tag: String, value: CharSequence? = null, block: Callback? = null) {
            tags[tag] = value to block
        }

        private fun hint(tag: String, value: CharSequence? = null, block: Callback? = null) {
            hints[tag] = value to block
        }

        internal fun fill(view: EditText): Boolean {
            if (tags.isNotEmpty()) {
                view.tag?.apply {
                    tags[this]?.apply {
                        if (first != null) {
                            view.setText(first)
                        }
                        second?.invoke(view)
                        return true
                    }
                }
            }
            if (ids.isNotEmpty() && NO_ID != view.id) {
                view.id.apply {
                    ids[this]?.apply {
                        if (first != null) {
                            view.setText(first)
                        }
                        second?.invoke(view)
                        return true
                    }
                }
            }
            return false

        }

        internal fun merge(another: Scenario): Scenario {
            val self = this
            return Scenario().apply {
                tags.putAll(self.tags)
                tags.putAll(another.tags)
                ids.putAll(self.ids)
                ids.putAll(another.ids)
                hints.putAll(self.hints)
                hints.putAll(another.hints)
            }
        }
    }

}

private class FormFillerActivityLifeCycle(private val filler: FormFiller) :
    Application.ActivityLifecycleCallbacks {

    private val Tag = "_formfiller_"
    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {
        activity.findViewById<ViewGroup>(android.R.id.content)?.apply {
            if (findViewWithTag<View>(Tag) == null && childCount > 0) {
                val layout = FormFillerLayout(activity, null)
                layout.tag = Tag
                val children = arrayListOf<View>()
                for (idx in 0 until childCount) {
                    children.add(getChildAt(idx))
                }
                removeAllViews()
                children.forEach {
                    layout.addView(it)
                }
                addView(layout)
            }
        }
    }

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}
}

private typealias Callback = (EditText) -> Unit
