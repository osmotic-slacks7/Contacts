package org.fossify.contacts.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils
import com.google.android.material.textfield.TextInputLayout
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.isSystemInDarkMode

/**
 * Material 3 color roles used for the Google Contacts style.
 *
 * With Material You (the default on Android 12+), the colors come straight from the system's
 * wallpaper-based palettes, which is what Google's own apps use. With a custom Fossify theme,
 * they are mixed from the chosen background and primary colors instead.
 */
enum class ToneRole {
    PRIMARY_CONTAINER,
    ON_PRIMARY_CONTAINER,
    SECONDARY_CONTAINER,
    ON_SECONDARY_CONTAINER,
    SURFACE_CONTAINER,
    SURFACE_CONTAINER_HIGH,
}

fun Context.isDarkBackground(): Boolean = if (isDynamicTheme()) {
    isSystemInDarkMode()
} else {
    getProperBackgroundColor().getContrastColor() == Color.WHITE
}

@SuppressLint("InlinedApi")
fun Context.getToneColor(role: ToneRole): Int {
    val dark = isDarkBackground()
    if (isDynamicTheme()) {
        @ColorRes val res = when (role) {
            ToneRole.PRIMARY_CONTAINER -> if (dark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
            ToneRole.ON_PRIMARY_CONTAINER -> if (dark) android.R.color.system_accent1_100 else android.R.color.system_accent1_900
            ToneRole.SECONDARY_CONTAINER -> if (dark) android.R.color.system_accent2_700 else android.R.color.system_accent2_100
            ToneRole.ON_SECONDARY_CONTAINER -> if (dark) android.R.color.system_accent2_100 else android.R.color.system_accent2_900
            ToneRole.SURFACE_CONTAINER -> if (dark) android.R.color.system_neutral1_900 else android.R.color.system_neutral2_50
            ToneRole.SURFACE_CONTAINER_HIGH -> if (dark) android.R.color.system_neutral2_800 else android.R.color.system_neutral2_100
        }
        return resources.getColor(res, theme)
    }

    val background = getProperBackgroundColor()
    val primary = getProperPrimaryColor()
    val text = getProperTextColor()
    return when (role) {
        ToneRole.PRIMARY_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.45f else 0.25f)
        ToneRole.SECONDARY_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.30f else 0.16f)
        ToneRole.ON_PRIMARY_CONTAINER, ToneRole.ON_SECONDARY_CONTAINER -> text
        ToneRole.SURFACE_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.08f else 0.05f)
        ToneRole.SURFACE_CONTAINER_HIGH -> ColorUtils.blendARGB(background, primary, if (dark) 0.14f else 0.09f)
    }
}

/**
 * Outlined Material text fields (Google Contacts edit screen): grey outline, primary color when focused,
 * and each box follows the visibility of the field inside it, since the app shows and hides the fields themselves.
 */
fun Context.updateOutlinedFields(viewGroup: ViewGroup) {
    val primary = getProperPrimaryColor()
    val text = getProperTextColor()
    val outline = text.adjustAlpha(0.45f)
    val strokeColors = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
        intArrayOf(primary, outline)
    )

    fun walk(group: ViewGroup) {
        for (i in 0 until group.childCount) {
            when (val child = group.getChildAt(i)) {
                is TextInputLayout -> {
                    child.setBoxStrokeColorStateList(strokeColors)
                    child.defaultHintTextColor = ColorStateList.valueOf(text.adjustAlpha(0.7f))
                    child.hintTextColor = ColorStateList.valueOf(primary)
                    child.boxBackgroundColor = Color.TRANSPARENT
                    // MyEditText tints its background with the accent color, which would override the outline colors
                    child.editText?.background?.colorFilter = null
                    child.visibility = child.editText?.visibility ?: View.VISIBLE
                }

                is ViewGroup -> walk(child)
            }
        }
    }
    walk(viewGroup)
}
