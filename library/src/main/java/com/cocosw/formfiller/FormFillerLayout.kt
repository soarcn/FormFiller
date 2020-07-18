package com.cocosw.formfiller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout


internal class FormFillerLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    private val filler = FormFiller.getInstant()
    private val mDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                triggerFormFill()
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                if (filler.enableSwitcher && e?.pointerCount == 1 && filler.scenarios.size > 1) {
                    val list = filler.scenarios.keys.toTypedArray()
                    AlertDialog.Builder(context).setItems(list) { _: DialogInterface, i: Int ->
                        filler.changeScenario(list[i])
                    }.show()
                }
            }
        })

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (!filler.doubleTap)
            super.onTouchEvent(event)
        else {
            mDetector.onTouchEvent(event)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (filler.keycodes.isEmpty()) {
            super.dispatchKeyEvent(event)
        } else {
            when (event.action) {
                KeyEvent.ACTION_UP -> {
                    return keyCode(event.keyCode)
                }
                else -> super.dispatchKeyEvent(event)
            }
        }
    }


    private fun keyCode(keyCode: Int): Boolean {
        val value = filler.keycodes[keyCode]
        if (value != null) {
            if (!value || (value && isKeyboardConnected())) {
                triggerFormFill()
            }
        }
        return false
    }

    /**
     * Checks whether an external keyboard is connected
     *
     * @return True if the external keyboard is connected,
     *         False if only SoftKeyboard is connected
     */
    private fun isKeyboardConnected(): Boolean {
        return resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY
    }

    private fun triggerFormFill() {
        findViews(context, this)
    }

    private fun findViews(context: Context, v: View) {
        try {
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    findViews(context, v.getChildAt(i))
                }
            } else if (v is EditText) {
                filler.fill(v)
            }
        } catch (ignore: Exception) {
        }
    }

}