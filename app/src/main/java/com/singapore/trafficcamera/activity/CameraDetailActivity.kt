package com.singapore.trafficcamera.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.ActivityCameraDetailBinding
import com.singapore.trafficcamera.interfaces.FavouriteListener
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.FavouriteStaticData
import com.singapore.trafficcamera.models.dao.Favourite
import com.singapore.trafficcamera.tools.Constants
import com.singapore.trafficcamera.utils.GeneralUtils
import com.singapore.trafficcamera.utils.UserManager
import com.singapore.trafficcamera.viewModels.FavouriteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraDetailActivity : AppCompatActivity(), FavouriteListener {

    private lateinit var binding: ActivityCameraDetailBinding

    private val favouriteViewModel: FavouriteViewModel by viewModels()

    private var cameraItem: CameraItem?= null
    private var cameraItemPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get cameraItem from intent
        cameraItem = intent.getParcelableExtra(Constants.CAMERA_ITEM)
        cameraItemPosition = intent.getIntExtra(Constants.CAMERA_ITEM_POS, -1)

        initData()
        initObserver()
        initClickEvent()
    }

    override fun onFavouriteItemUpdated(position: Int) {
        updateFavouriteIcon()
    }

    override fun onBackPressed() {
        finishActivityWithResult()
        super.onBackPressed()
    }

    private fun initObserver() {
        val username = UserManager.data?.user?.username!!
        favouriteViewModel.getFavouriteCameraBasedOnUsername(username = username).observe(this) { data ->
            if (data.isNotEmpty()) {
                //add all favourite camera list to static list
                FavouriteStaticData.data?.favouriteList?.clear()
                FavouriteStaticData.data?.favouriteList?.addAll(data)
            }
        }
    }

    private fun initData() {
        val favouriteOn = ContextCompat.getDrawable(this, R.drawable.ic_favourite_on)
        val favouriteOff = ContextCompat.getDrawable(this, R.drawable.ic_favourite_off)

        //load image
        GeneralUtils.loadImage(
            context = this,
            imageURL = cameraItem?.image!!,
            appCompatImageView = binding.ivCameraImage
        )

        //camera id
        binding.tvCameraId.text = cameraItem?.camera_id

        //time
        binding.tvTimeStamp.text = cameraItem?.timestamp

        //lat,lng
        val lat = cameraItem?.location!!.latitude.toString()
        val lng = cameraItem?.location!!.longitude.toString()
        binding.tvLocationLat.text =  lat
        binding.tvLocationLng.text = lng

        //image metadata
        val height = cameraItem?.image_metadata!!.height.toString()
        val width = cameraItem?.image_metadata!!.width.toString()
        val md5 = cameraItem?.image_metadata!!.md5
        binding.tvMetadataHeight.text = height
        binding.tvMetadataWidth.text = width
        binding.tvMetadataMd5.text = md5

        //is favourite
        when {
            FavouriteStaticData.data?.favouriteList!!.contains(cameraItem?.camera_id) -> {
                binding.ivFavourite.setImageDrawable(favouriteOn)
            }
            else -> {
                binding.ivFavourite.setImageDrawable(favouriteOff)
            }
        }
    }

    private fun performFavouriteCameraItem() {
        val favourite = Favourite()
        favourite.username = UserManager.data?.user?.username!!
        favourite.favouriteCamera = cameraItem?.camera_id!!

        //insert to db
        favouriteViewModel.insertUserFavouriteCamera(
            favourite = favourite,
            position = cameraItemPosition,
            listener = this
        )
    }

    private fun performUnFavoriteCameraItem() {
        //remove from db
        favouriteViewModel.deleteUserFavouriteCamera(
            username = UserManager.data?.user?.username!!,
            position = cameraItemPosition,
            cameraId = cameraItem?.camera_id!!,
            listener = this
        )
    }

    private fun performCameraFavouriteIconClickEvent() {
        if (FavouriteStaticData.data?.favouriteList?.contains(cameraItem?.camera_id)!!) {
            //contain favourite item
            //remove it
            performUnFavoriteCameraItem()
            return
        }
        //favourite item
        performFavouriteCameraItem()
    }

    private fun initClickEvent() {
        binding.ivClose.setOnClickListener {
            finishActivityWithResult()
        }

        binding.ivFavourite.setOnClickListener {
            performCameraFavouriteIconClickEvent()
        }
    }

    private fun updateFavouriteIcon() {
        val favouriteOn = ContextCompat.getDrawable(this, R.drawable.ic_favourite_on)
        val favouriteOff = ContextCompat.getDrawable(this, R.drawable.ic_favourite_off)

        Handler().postDelayed({
            //update specific adapter item
            when {
                FavouriteStaticData.data?.favouriteList!!.contains(cameraItem?.camera_id) -> {
                    binding.ivFavourite.setImageDrawable(favouriteOn)
                }
                else -> {
                    binding.ivFavourite.setImageDrawable(favouriteOff)
                }
            }
        }, Constants.DELAY_SEC)
    }

    private fun finishActivityWithResult() {
        val data = Intent()
        data.putExtra(Constants.CAMERA_ITEM_POS, cameraItemPosition)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}