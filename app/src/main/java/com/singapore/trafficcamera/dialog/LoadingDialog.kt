package com.singapore.trafficcamera.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.singapore.trafficcamera.databinding.DialogLoadingBinding

class LoadingDialog(context: Context?) : AlertDialog(context) {

    private var _binding: DialogLoadingBinding?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}