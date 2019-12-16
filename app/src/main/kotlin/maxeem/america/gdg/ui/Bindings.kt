package maxeem.america.gdg.ui

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import maxeem.america.ext.fromHtml

@BindingAdapter("goneIf")
fun View.goneIf(condition: Boolean?) {
    visibility = if (condition == true) View.GONE else View.VISIBLE
}

@BindingAdapter("visibleOn")
fun View.visibleOn(condition: Boolean?) {
    visibility = if (condition == true) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("textHtml")
fun TextView.textHtml(str: String) {
    text = str.fromHtml()
}

