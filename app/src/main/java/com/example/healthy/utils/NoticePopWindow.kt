package com.example.healthy.utils

import android.app.ActionBar
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import com.example.healthy.R
import kotlinx.android.synthetic.main.notice_pop_window.view.*

object NoticePopWindow {

    private var popupWindow: PopupWindow? = null

    private fun init(context: Context?, root: ViewGroup?) {
        if (context == null) {
            return
        }
        val view = LayoutInflater.from(context.applicationContext)
            .inflate(R.layout.notice_pop_window, root, false)
        with(view) {
            notice_view?.setOnClickListener {
                dismiss()
            }
        }
        popupWindow = PopupWindow(
            view,
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )
    }

    fun show(context: Context?, root: ViewGroup?) {
        if(isShow()){
            return
        }
        if (popupWindow == null) {
            init(context, root)
        }
        popupWindow?.showAtLocation(root, Gravity.CENTER, 0, 0)
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    fun isShow(): Boolean {
        return popupWindow?.isShowing ?: false
    }

    fun setNoticeText(text: String, clear: Boolean = false) {
        val textView: TextView? = popupWindow?.contentView?.findViewById(R.id.notice)
        if (clear) {
            textView?.text = text
        } else {
            val str = textView?.text.toString() + "\n" + text
            textView?.text = str
        }

    }


}