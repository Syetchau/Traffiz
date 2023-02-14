package com.singapore.trafficcamera.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Parcelable
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.singapore.trafficcamera.R
import java.util.regex.Pattern

object GeneralUtils {

    fun showDialog(dialog: AlertDialog, cancelable: Boolean) {
        dialog.show()
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelable)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun loadImage(context: Context, imageURL: String, appCompatImageView: AppCompatImageView) {
        Glide.with(context)
            .load(imageURL)
            .error(R.drawable.ic_default_icon)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(appCompatImageView)
    }

    fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile("^" +
                "(?=.*[0-9])" +         //at least 1 number
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$")
        return passwordREGEX.matcher(password).matches()
    }

    // This is because enums are Serializable by default,
    // and when you add Parcelable, it matches both method signatures.
    // You can add an extension function to resolve the ambiguity
    fun Intent.putParcelableExtra(key: String, value: Parcelable) {
        putExtra(key, value)
    }
}