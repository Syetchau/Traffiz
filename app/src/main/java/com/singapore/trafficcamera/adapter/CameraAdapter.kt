package com.singapore.trafficcamera.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.ItemCameraBinding
import com.singapore.trafficcamera.interfaces.CameraItemListener
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.FavouriteStaticData

class CameraAdapter(private val context: Context, private var cameraList: List<CameraItem>,
                    private val listener: CameraItemListener): RecyclerView.Adapter<CameraAdapter.ViewHolder>() {

   @SuppressLint("NotifyDataSetChanged")
   fun updateCameraList(cameraList: List<CameraItem>) {
       this.cameraList = cameraList
       notifyDataSetChanged()
   }

    fun updateCameraItem(position: Int) {
        notifyItemChanged(position)
    }

    fun getCurrentList(): MutableList<CameraItem> {
        return this.cameraList as MutableList<CameraItem>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCameraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val camera = cameraList[position]
        val favouriteIcon = ContextCompat.getDrawable(context, R.drawable.ic_favourite_on)
        val unFavouriteIcon = ContextCompat.getDrawable(context, R.drawable.ic_favourite_off)

        with(holder) {
            binding.tvCameraNo.text = camera.camera_id

            //contain favourite camera
            when {
                FavouriteStaticData.data?.favouriteList?.contains(camera.camera_id)!! -> {
                    binding.ivFavourite.setImageDrawable(favouriteIcon)
                }
                else -> {
                    //not favourite camera
                    binding.ivFavourite.setImageDrawable(unFavouriteIcon)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return cameraList.size
    }

    inner class ViewHolder(val binding: ItemCameraBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                listener.onCameraItemSelected(cameraItem = cameraList[adapterPosition], position = adapterPosition)
            }

            binding.ivFavourite.setOnClickListener {
               handleFavouriteLogic(position = adapterPosition)
            }
        }
    }

    private fun handleFavouriteLogic(position: Int) {
        if (FavouriteStaticData.data?.favouriteList?.contains(cameraList[position].camera_id)!!) {
            //contain favourite item
            //remove it
            listener.onCameraItemRemoveFromFavourite(
                cameraItem = cameraList[position],
                position = position
            )
            return
        }
        //favourite item
        listener.onCameraItemAddToFavourite(
            cameraItem = cameraList[position],
            position = position
        )
    }
}