package com.cocosw.formfiller

/** Produces a Fill Value when its Scenario entry first matches during a Fill Run. */
fun interface ValueProvider {
    fun provide(): CharSequence
}
