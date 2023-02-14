package com.singapore.trafficcamera.dialog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.MarkerInfoContentBinding
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.FavouriteStaticData
import com.singapore.trafficcamera.tools.Constants

class MarkerInfoWindowAdapter(private val context: Context): GoogleMap.InfoWindowAdapter {

    private var binding = MarkerInfoContentBinding.inflate(LayoutInflater.from(context))

    //initiate those two variables
    private val images: HashMap<Marker, Bitmap> = HashMap()
    private val targets: HashMap<Marker, CustomTarget<Bitmap>> = HashMap()

    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val cameraItem = marker.tag as? CameraItem ?: return null

        val timeStamp = context.getString(R.string.label_timestamp) + " " + cameraItem.timestamp
        val cameraID = context.getString(R.string.label_camera_id) + " " + cameraItem.camera_id
        val lat = context.getString(R.string.label_latitude) + " " + cameraItem.location?.latitude!!
        val lng = context.getString(R.string.label_longitude) + " " + cameraItem.location?.longitude!!
        val height = context.getString(R.string.label_height) + " " + cameraItem.image_metadata?.height!!
        val width = context.getString(R.string.label_width) + " " + cameraItem.image_metadata?.width!!
        val md5 = context.getString(R.string.label_md5) + " " + cameraItem.image_metadata?.md5!!
        val favouriteOn = ContextCompat.getDrawable(context, R.drawable.ic_favourite_on)
        val favouriteOff = ContextCompat.getDrawable(context, R.drawable.ic_favourite_off)

        //insert image here (in your acctual population code)
        when (val image = images[marker]) {
            null -> {
                Glide.with(context)
                    .asBitmap()
                    .load(cameraItem.image)
                    .dontAnimate()
                    .override(Constants.IMAGE_WIDTH,Constants.IMAGE_HEIGHT)
                    .into(getTarget(marker))
            }
            else -> {
                binding.ivCameraImage.setImageBitmap(image)
            }
        }

        //data
        binding.tvTimestamp.text = timeStamp
        binding.tvCameraId.text = cameraID
        binding.tvCameraLocationLat.text = lat
        binding.tvCameraLocationLng.text = lng
        binding.tvCameraMetadataHeight.text = height
        binding.tvCameraMetadataWidth.text = width
        binding.tvCameraMetadataMd5.text = md5

        //is favourite
        when {
            FavouriteStaticData.data?.favouriteList!!.contains(cameraItem.camera_id) -> {
                binding.ivFavourite.setImageDrawable(favouriteOn)
            }
            else -> {
                binding.ivFavourite.setImageDrawable(favouriteOff)
            }
        }
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }

    private fun getTarget(marker: Marker): CustomTarget<Bitmap> {
        var target = targets[marker]
        if (target == null) {
            target = InfoTarget(marker)
            targets[marker] = target
        }
        return target
    }

    inner class InfoTarget(var marker: Marker) : CustomTarget<Bitmap>() {
        override fun onLoadCleared(placeholder: Drawable?) {
            images.remove(marker)
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            images[marker] = resource
            marker.showInfoWindow()
        }
    }
}

