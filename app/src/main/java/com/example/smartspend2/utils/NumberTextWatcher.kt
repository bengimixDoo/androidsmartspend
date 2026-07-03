package com.example.smartspend2.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class NumberTextWatcher(private val editText: EditText) : TextWatcher {
    private val df: DecimalFormat
    private val dfnd: DecimalFormat
    private var hasFractionalPart: Boolean = false

    init {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = '.' // Use dot as thousands separator
        symbols.decimalSeparator = ',' // Use comma for decimal separator
        df = DecimalFormat("#,###.##", symbols)
        df.isDecimalSeparatorAlwaysShown = true
        dfnd = DecimalFormat("#,###", symbols)
    }

    override fun afterTextChanged(s: Editable) {
        editText.removeTextChangedListener(this)
        try {
            val inilen: Int = editText.text.length
            val v: String = s.toString().replace(df.decimalFormatSymbols.groupingSeparator.toString(), "")
            val n = df.parse(v)
            val cp = editText.selectionStart
            if (hasFractionalPart) {
                editText.setText(df.format(n))
            } else {
                editText.setText(dfnd.format(n))
            }
            val endlen: Int = editText.text.length
            val sel = (cp + (endlen - inilen))
            if (sel > 0 && sel <= editText.text.length) {
                editText.setSelection(sel)
            } else {
                editText.setSelection(editText.text.length - 1)
            }
        } catch (e: Exception) {
            // Do nothing
        }
        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        hasFractionalPart = s.toString().contains(df.decimalFormatSymbols.decimalSeparator.toString())
    }
    
    companion object {
        fun getCleanValue(text: String): String {
            return text.replace(".", "").replace(",", ".")
        }
    }
}
