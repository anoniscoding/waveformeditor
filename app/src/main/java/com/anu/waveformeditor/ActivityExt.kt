package com.anu.waveformeditor

import android.app.Activity
import android.widget.Toast

fun Activity.toast(text: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(
        this,
        text,
        length
    ).show()
}