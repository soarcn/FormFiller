package com.cocosw.formfiller

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import android.widget.EditText
import java.util.Collections

class FormFiller internal constructor(
    internal val scenarios: Map<String, Scenario>
    , internal val keycodes: Map<Int, Boolean>
    , internal val doubleTap: Boolean,
    internal val enableSwitcher: Boolean
) {
    internal var currentScenario: Scenario = scenarios[DEFAULT_SCENARIO] ?: Scenario()

    internal fun fill(view: EditText, scenario: ScenarioSnapshot): Boolean {
        val entry = scenario.entries.firstOrNull {
            when (val selector = it.selector) {
                is ScenarioSelector.TargetTag -> view.tag == selector.value
                is ScenarioSelector.ResourceId -> view.id != NO_ID && view.id == selector.value
            }
        } ?: return false

        return when (val resolution = entry.resolve()) {
            is FillValueResolution.Value -> {
                view.setText(resolution.fillValue)
                true
            }
            FillValueResolution.ProviderFailure -> false
        }
    }

    internal fun newScenarioSnapshot(): ScenarioSnapshot = currentScenario.newSnapshot()

    fun changeScenario(scenarioName: String) {
        val scenario = scenarios[scenarioName]
        if (scenario == null) {
            throw IllegalArgumentException("Invalid scenario name")
        } else {
            currentScenario = scenarios[DEFAULT_SCENARIO]?.merge(scenario) ?: scenario
        }

    }

    companion object {
        private val DEFAULT_SCENARIO = "Default"
        internal var instance: FormFiller? = null
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

        fun build(): FormFiller {
            if (instance == null) {
                if (keycodes.isEmpty() && !doubleTap) {
                    throw IllegalArgumentException("You must define one trigger by calling keyCode() or doubleTap()")
                }

                val instance = FormFiller(scenarios, keycodes, doubleTap, enableSwitcher)
                FormFiller.instance = instance
                application.registerActivityLifecycleCallbacks(FormFillerActivityLifeCycle(instance))
            }
            return instance!!
        }
    }

    class Scenario {
        internal val tags = linkedMapOf<String, FillValueSource>()
        internal val ids = linkedMapOf<Int, FillValueSource>()

        fun id(id: Int, value: CharSequence) {
            ids[id] = FillValueSource.Static(value)
        }

        fun id(id: Int, provider: ValueProvider) {
            ids[id] = FillValueSource.Provided(provider)
        }

        fun tag(tag: String, value: CharSequence) {
            tags[tag] = FillValueSource.Static(value)
        }

        fun tag(tag: String, provider: ValueProvider) {
            tags[tag] = FillValueSource.Provided(provider)
        }

        internal fun newSnapshot(): ScenarioSnapshot {
            val entries = buildList {
                tags.forEach { (tag, source) ->
                    add(ScenarioEntry(ScenarioSelector.TargetTag(tag), source))
                }
                ids.forEach { (id, source) ->
                    add(ScenarioEntry(ScenarioSelector.ResourceId(id), source))
                }
            }
            return ScenarioSnapshot(entries)

        }

        internal fun merge(another: Scenario): Scenario {
            val self = this
            return Scenario().apply {
                tags.putAll(self.tags)
                tags.putAll(another.tags)
                ids.putAll(self.ids)
                ids.putAll(another.ids)
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

internal sealed interface FillValueSource {
    data class Static(val value: CharSequence) : FillValueSource
    data class Provided(val provider: ValueProvider) : FillValueSource
}

internal sealed interface ScenarioSelector {
    data class TargetTag(val value: String) : ScenarioSelector
    data class ResourceId(val value: Int) : ScenarioSelector
}

internal data class ScenarioEntry(
    val selector: ScenarioSelector,
    val source: FillValueSource
)

internal sealed interface FillValueResolution {
    data class Value(val fillValue: CharSequence) : FillValueResolution
    data object ProviderFailure : FillValueResolution
}

internal class ResolvedScenarioEntry internal constructor(
    val selector: ScenarioSelector,
    private val source: FillValueSource
) {
    private var cachedResolution: FillValueResolution? = when (source) {
        is FillValueSource.Static -> FillValueResolution.Value(source.value.toString())
        is FillValueSource.Provided -> null
    }

    fun resolve(): FillValueResolution {
        cachedResolution?.let { return it }

        val resolution = try {
            val provider = (source as FillValueSource.Provided).provider
            FillValueResolution.Value(provider.provide().toString())
        } catch (_: Exception) {
            FillValueResolution.ProviderFailure
        }
        cachedResolution = resolution
        return resolution
    }
}

internal class ScenarioSnapshot(entries: List<ScenarioEntry>) {
    val entries: List<ResolvedScenarioEntry> = Collections.unmodifiableList(
        entries.map { ResolvedScenarioEntry(it.selector, it.source) }
    )
}
