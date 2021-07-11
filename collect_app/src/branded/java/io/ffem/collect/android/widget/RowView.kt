package io.ffem.collect.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import org.odk.collect.android.R

class RowView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    TableRow(context, attrs) {
    private val textPrimary: TextView?
    private val textSecondary: TextView
    private val textTertiary: TextView
    fun setPrimaryText(s: String?) {
        textPrimary!!.text = s
    }

    fun setSecondaryText(s: String?) {
        textSecondary.text = s?.trim()
    }

    fun setTertiaryText(s: String?) {
        textTertiary.text = s?.trim()
    }

    fun setLaunchButton(t: View?): Boolean {
        return if (textPrimary!!.text.length < 20) {
            (findViewById<View>(R.id.row_layout) as LinearLayout).addView(t)
            true
        } else {
            false
        }
    }

    val isAnswered: Boolean
        get() = textPrimary != null && textPrimary.text.toString().isNotEmpty()

    init {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.row_view, this, true)
        val tableRow = getChildAt(0) as TableRow
        textPrimary = tableRow.getChildAt(0) as TextView
        textSecondary = (tableRow.getChildAt(1) as LinearLayout).getChildAt(0) as TextView
        textTertiary = (tableRow.getChildAt(1) as LinearLayout).getChildAt(1) as TextView
    }
}