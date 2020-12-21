package com.example.healthy.utils

import android.content.Context

class DialogUtil {

    constructor(context: Context, title: String) : this(context, title, "")

    constructor(context: Context, title: String, content: String?) : this(
        context,
        title,
        content,
        null,
        null
    )

    constructor(
        context: Context,
        title: String,
        content: String?,
        ensure: String?,
        cancel: String?
    ) {
    }

}