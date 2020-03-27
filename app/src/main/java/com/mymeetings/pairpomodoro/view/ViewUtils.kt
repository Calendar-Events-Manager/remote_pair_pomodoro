package com.mymeetings.pairpomodoro.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mymeetings.pairpomodoro.R


object ViewUtils {

    fun buildInputDialog(context: Context, textCallback: (String) -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.enter_sharing_key))

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        builder.setView(input)

        builder.setPositiveButton(
            context.getString(R.string.ok)
        ) { dialog, _ ->
            if (input.text.isNotBlank()) {
                textCallback(input.text.toString())
                dialog.dismiss()
            }
        }
        builder.setNegativeButton(
            context.getString(R.string.cancel)
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    fun confirmationDialog(
        context: Context,
        message: String,
        confirmCallback: (status: Boolean) -> Unit
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(
            context.getString(R.string.ok)
        ) { dialog, _ ->
            confirmCallback(true)
            dialog.dismiss()
        }
        builder.setNegativeButton(
            context.getString(R.string.cancel)
        ) { dialog, _ ->
            confirmCallback(false)
            dialog.cancel()
        }

        builder.show()
    }

    fun copyTextToClipBoard(context: Context, textToCopy: String) {
        val clipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("sharing key", textToCopy.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.key_copied), Toast.LENGTH_SHORT).show()
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.visible() {
    visibility = View.VISIBLE
}