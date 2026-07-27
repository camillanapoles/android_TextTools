package com.corphish.quicktools.actions

import androidx.annotation.DrawableRes
import com.corphish.quicktools.R

/** Resolve iconKey (string do objeto) para drawable resource (estático). */
object ActionIcons {
    private val MAP = mapOf(
        "ic_text_transform" to R.drawable.ic_text_transform,
        "ic_text_count" to R.drawable.ic_text_count,
        "ic_edit_note" to R.drawable.ic_edit_note,
        "ic_find_and_replace" to R.drawable.ic_find_and_replace,
        "ic_save" to R.drawable.ic_save,
        "ic_numbers" to R.drawable.ic_numbers,
        "ic_whatsapp" to R.drawable.ic_whatsapp,
        "ic_auto_awesome" to R.drawable.ic_auto_awesome,
    )

    @DrawableRes
    fun resolve(key: String?): Int = MAP[key] ?: R.drawable.ic_auto_awesome

    val keys: List<String> = MAP.keys.toList()
}
