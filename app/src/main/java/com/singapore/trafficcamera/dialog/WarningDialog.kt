package com.singapore.trafficcamera.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.singapore.trafficcamera.databinding.DialogWarningBinding
import com.singapore.trafficcamera.interfaces.WarningDialogListener

class WarningDialog(context: Context?, private val listener: WarningDialogListener,
                    private val title: String, private val body: String): AlertDialog(context) {

    private var _binding: DialogWarningBinding?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = DialogWarningBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)
        super.onCreate(savedInstanceState)

        initData()
        initClickEvent()
    }

    private fun initData() {
        binding.tvTitle.text = title
        binding.tvBody.text = body
    }

    private fun initClickEvent() {
        binding.buttonConfirm.setOnClickListener {
            listener.onWarningDialogClicked()
            dismiss()
        }
    }
}