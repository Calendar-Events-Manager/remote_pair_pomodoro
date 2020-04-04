package com.mymeetings.pairpomodoro.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mymeetings.pairpomodoro.R
import kotlinx.android.synthetic.main.dialog_sharing_key.view.*


object ViewUtils {

    fun buildInputDialog(context: Context, textCallback: (String) -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val view = View.inflate(context, R.layout.dialog_sharing_key, null)
        builder.setView(view)

        val dialog = builder.show()

        view.sharingKeyText.requestFocus()
        view.syncButton.setOnClickListener {
            val sharingKey = view.sharingKeyText.text.toString()
            if (sharingKey.length == 5) {
                textCallback(sharingKey)
                dialog.dismiss()
            }
        }

        view.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
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

    fun infoDialog(
        context: Context,
        message: String,
        confirmCallback: () -> Unit
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(
            context.getString(R.string.ok)
        ) { dialog, _ ->
            confirmCallback()
            dialog.dismiss()
        }

        builder.show()
    }

    fun copyTextToClipBoard(context: Context, textToCopy: String) {
        val clipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("sharing key", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.key_copied), Toast.LENGTH_SHORT).show()
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}