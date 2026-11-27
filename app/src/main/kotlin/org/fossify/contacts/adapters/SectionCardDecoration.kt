package org.fossify.contacts.adapters

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Samsung One UI style contact list: rows sit inside white rounded cards with side margins, every letter
 * starts a new card under a small grey letter header, and rows within a card are separated by inset dividers.
 *
 * The card shape of each row is set by [ContactsAdapter]; this decoration adds the spacing, headers and dividers.
 */
class SectionCardDecoration(
    private val adapter: ContactsAdapter,
    textColor: Int,
) : RecyclerView.ItemDecoration() {

    private var sideMarginPx = 0
    private var headerHeightPx = 0
    private var sectionGapPx = 0
    private var dividerStartPx = 0
    private var dividerEndPx = 0
    private var headerTextStartPx = 0

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        alpha = 150
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val dividerPaint = Paint().apply {
        color = textColor
        alpha = 28
    }

    private fun ensureSizes(view: View) {
        if (sideMarginPx != 0) return
        val metrics = view.resources.displayMetrics
        fun dp(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics).toInt()
        sideMarginPx = dp(12f)
        headerHeightPx = dp(34f)
        sectionGapPx = dp(4f)
        // divider starts where the name text starts: card padding 16 + avatar 40 + gap 16
        dividerStartPx = sideMarginPx + dp(72f)
        dividerEndPx = sideMarginPx + dp(16f)
        headerTextStartPx = sideMarginPx + dp(16f)
        headerPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, metrics)
        dividerPaint.strokeWidth = maxOf(1f, metrics.density * 0.6f)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || !adapter.useSectionCards()) return
        ensureSizes(view)

        outRect.left = sideMarginPx
        outRect.right = sideMarginPx
        if (adapter.isFirstInSection(position)) {
            outRect.top = if (adapter.getSectionTitle(position).isNotEmpty()) headerHeightPx else sectionGapPx * 2
        }
        if (adapter.isLastInSection(position)) {
            outRect.bottom = sectionGapPx
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (!adapter.useSectionCards()) return
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION || !adapter.isFirstInSection(position)) continue
            val title = adapter.getSectionTitle(position)
            if (title.isEmpty()) continue
            ensureSizes(child)
            val baseline = child.top + child.translationY - headerHeightPx * 0.32f
            c.drawText(title, headerTextStartPx.toFloat(), baseline, headerPaint)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (!adapter.useSectionCards()) return
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION || adapter.isLastInSection(position)) continue
            ensureSizes(child)
            val y = child.bottom + child.translationY
            c.drawLine(dividerStartPx.toFloat(), y, (parent.width - dividerEndPx).toFloat(), y, dividerPaint)
        }
    }
}
