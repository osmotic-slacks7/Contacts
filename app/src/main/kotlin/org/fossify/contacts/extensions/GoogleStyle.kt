package org.fossify.contacts.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    /** Samsung One UI style: light grey page behind white rounded cards (black page and dark grey cards in dark mode). */
    PAGE,
    CARD,
}

fun Context.isDarkBackground(): Boolean = if (isDynamicTheme()) {
    isSystemInDarkMode()
} else {
    getProperBackgroundColor().getContrastColor() == Color.WHITE
}

@SuppressLint("InlinedApi")
fun Context.getToneColor(role: ToneRole): Int {
    val dark = isDarkBackground()
    val background = getProperBackgroundColor()
    when (role) {
        ToneRole.PAGE -> return if (dark) background else ColorUtils.blendARGB(background, Color.BLACK, 0.05f)
        ToneRole.CARD -> return if (dark) ColorUtils.blendARGB(background, Color.WHITE, 0.09f) else background
        else -> {}
    }
    if (isDynamicTheme()) {
        @ColorRes val res = when (role) {
            ToneRole.PRIMARY_CONTAINER -> if (dark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
            ToneRole.ON_PRIMARY_CONTAINER -> if (dark) android.R.color.system_accent1_100 else android.R.color.system_accent1_900
            ToneRole.SECONDARY_CONTAINER -> if (dark) android.R.color.system_accent2_700 else android.R.color.system_accent2_100
            ToneRole.ON_SECONDARY_CONTAINER -> if (dark) android.R.color.system_accent2_100 else android.R.color.system_accent2_900
            ToneRole.SURFACE_CONTAINER -> if (dark) android.R.color.system_neutral1_900 else android.R.color.system_neutral2_50
            ToneRole.SURFACE_CONTAINER_HIGH -> if (dark) android.R.color.system_neutral2_800 else android.R.color.system_neutral2_100
            else -> android.R.color.transparent
        }
        return resources.getColor(res, theme)
    }

    val primary = getProperPrimaryColor()
    val text = getProperTextColor()
    return when (role) {
        ToneRole.PRIMARY_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.45f else 0.25f)
        ToneRole.SECONDARY_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.30f else 0.16f)
        ToneRole.ON_PRIMARY_CONTAINER, ToneRole.ON_SECONDARY_CONTAINER -> text
        ToneRole.SURFACE_CONTAINER -> ColorUtils.blendARGB(background, primary, if (dark) 0.08f else 0.05f)
        ToneRole.SURFACE_CONTAINER_HIGH -> ColorUtils.blendARGB(background, primary, if (dark) 0.14f else 0.09f)
        else -> background
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

// ---------------------------------------------------------------------------------------------
// Samsung One UI style avatars and tiles (original artwork, drawn in code)
// ---------------------------------------------------------------------------------------------

private val avatarBaseColors = intArrayOf(
    0xFFF28B82.toInt(), // coral
    0xFFF6A85B.toInt(), // orange
    0xFFF2C94C.toInt(), // amber
    0xFF8BD3A7.toInt(), // mint
    0xFF5FC9A8.toInt(), // green
    0xFF4FC3E8.toInt(), // sky
    0xFF7FA7F5.toInt(), // blue
    0xFF9A8CF0.toInt(), // indigo
    0xFFC08CE6.toInt(), // purple
    0xFFF28CB8.toInt(), // pink
)

/** Two colors for a soft diagonal gradient, picked from the name so a contact always gets the same colors. */
fun getAvatarGradient(name: String): IntArray {
    val base = avatarBaseColors[Math.floorMod(name.hashCode(), avatarBaseColors.size)]
    return intArrayOf(ColorUtils.blendARGB(base, Color.WHITE, 0.30f), base)
}

/** Up to two initials, e.g. "Alex Rowe" -> "AR", "Gwen" -> "G". */
fun getContactInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return ""
    val first = words.first().first()
    val last = if (words.size > 1) words.last().first() else null
    return if (first.isLetterOrDigit() && last != null && last.isLetter()) {
        "${first.uppercaseChar()}${last.uppercaseChar()}"
    } else {
        first.uppercaseChar().toString()
    }
}

/** Round avatar with a pastel gradient and white initials, used when a contact has no photo. */
fun createInitialsAvatar(name: String, sizePx: Int = 192): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val colors = getAvatarGradient(name)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), colors[0], colors[1], Shader.TileMode.CLAMP)
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, fill)

    val initials = getContactInitials(name)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sizePx * if (initials.length > 1) 0.36f else 0.42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val y = sizePx / 2f - (text.descent() + text.ascent()) / 2f
    canvas.drawText(initials, sizePx / 2f, y, text)
    return bitmap
}

/** Favourites tile artwork: a thin white ring with one big initial, drawn on a transparent background. */
fun createOutlinedInitialAvatar(name: String, sizePx: Int = 256): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val stroke = sizePx * 0.012f
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.WHITE
        alpha = 170
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - stroke, ring)

    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sizePx * 0.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val initial = getContactInitials(name).take(1)
    val y = sizePx / 2f - (text.descent() + text.ascent()) / 2f
    canvas.drawText(initial, sizePx / 2f, y, text)
    return bitmap
}

/** Rounded-square gradient background for a favourites tile. */
fun createTileBackground(name: String, cornerRadiusPx: Float): GradientDrawable {
    val colors = getAvatarGradient(name)
    return GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(colors[0], colors[1])).apply {
        cornerRadius = cornerRadiusPx
    }
}

/**
 * Background for one row of a One UI style card: only the first row of a section gets rounded top corners
 * and only the last one rounded bottom corners, so the rows of a section join into one card.
 */
fun createCardSegmentBackground(color: Int, radiusPx: Float, isFirst: Boolean, isLast: Boolean): GradientDrawable {
    val top = if (isFirst) radiusPx else 0f
    val bottom = if (isLast) radiusPx else 0f
    return GradientDrawable().apply {
        setColor(color)
        cornerRadii = floatArrayOf(top, top, top, top, bottom, bottom, bottom, bottom)
    }
}
