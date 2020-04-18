package com.example.ftpclient.ui

import android.app.Dialog
import android.content.Context
import android.text.Spannable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import com.example.ftpclient.R
import kotlinx.android.synthetic.main.dialog_layout.*


class CustomDialog(context: Context) : Dialog(context) {
    override fun onBackPressed() {
        dismiss()
        super.onBackPressed()
    }

    constructor(context: Context, params: Params) : this(context) {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_layout)
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            if (params.strCancel != null) {
                cancelButton.visibility = View.VISIBLE
            } else {
                cancelButton.visibility = View.GONE
            }

            if (params.inputVisibility) {
                inputEditText.visibility = View.VISIBLE
            } else {
                inputEditText.visibility = View.GONE
            }

            params.inputHint?.let {
                inputEditText.setHint(it)
            }
            params.title?.let{
                titleTextView.setText(it)
            }
            titleTextView.visibility = if (params.title != null) View.VISIBLE else View.GONE
            titleTextView.isSelected = true
            if (params.messageSpannable != null) {
                messageTextView.setText(params.messageSpannable)
                messageTextView.visibility = View.VISIBLE
            } else if (params.message != null) {
                messageTextView.setText(params.message)
                messageTextView.visibility = View.VISIBLE
            } else {
                messageTextView.visibility = View.GONE
            }
            params.messageAlign?.alignment?.let {
                messageTextView.gravity = it
            }

            params.strOk?.let{
                okButton.setText(it)
            }
            params.strCancel?.let{
                cancelButton.setText(it)
            }

            okButton.setOnClickListener {
                try {
                    dismiss()
                    params.onClickListener?.invoke(inputEditText?.text?.toString() ?: "")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            cancelButton.setOnClickListener {
                try {
                    dismiss()
                    params.onClickListener?.invoke(inputEditText?.text?.toString() ?: "")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            window?.setLayout(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    class Builder(){
        var params = Params()
        fun setOkButtonText(text: String): Builder {
            params.strOk = text
            return this
        }
        fun setCancelButtonText(text: String): Builder {
            params.strCancel = text
            return this
        }
        fun setTitle(text: String): Builder {
            params.title = text
            return this
        }
        fun setMessage(text: String): Builder {
            params.message = text
            return this
        }
        fun setMessage(text: Spannable): Builder {
            params.messageSpannable = text
            return this
        }
        fun setMessageAlign(align: TextAlign): Builder {
            params.messageAlign = align
            return this
        }
        fun setOnClickListener(listener: ((String)->Unit)?): Builder {
            params.onClickListener = listener
            return this
        }
        fun setInputEnabled(isEnabled: Boolean): Builder {
            params.inputVisibility = isEnabled
            return this
        }
        fun setInputHint(inputHint: String): Builder {
            params.inputHint = inputHint
            return this
        }

        fun build(context: Context): CustomDialog {
            return CustomDialog(context, params)
        }
    }

    class Params() {
        var title: String? = null
        var message: String? = null
        var messageSpannable: Spannable? = null
        var messageAlign: TextAlign? = null

        var strOk: String? = null
        var strCancel: String? = null
        var inputVisibility: Boolean = false
        var inputHint: String? = null
        var onClickListener: ((String)->Unit)? = null
    }

    enum class TextAlign (val alignment: Int){
        LEFT(Gravity.START),
        CENTER(Gravity.CENTER),
        RIGHT(Gravity.END)
    }
}