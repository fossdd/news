package common

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import co.appreactor.news.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

fun Fragment.showDialog(
    @StringRes titleId: Int,
    @StringRes messageId: Int,
    onDismissListener: DialogInterface.OnDismissListener? = null,
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(titleId)
        .setMessage(messageId)
        .setPositiveButton(android.R.string.ok, null)
        .setOnDismissListener(onDismissListener)
        .show()
}

fun Fragment.showDialog(
    @StringRes titleId: Int,
    message: String,
    onDismissListener: DialogInterface.OnDismissListener? = null,
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(titleId)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .setOnDismissListener(onDismissListener)
        .show()
}

fun Fragment.showDialog(
    title: String,
    message: String,
    onDismissListener: DialogInterface.OnDismissListener? = null,
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .setOnDismissListener(onDismissListener)
        .show()
}

fun Fragment.showErrorDialog(t: Throwable) {
    Timber.e(t)
    showDialog(R.string.error, t.message ?: "")
}