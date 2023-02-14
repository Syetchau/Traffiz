package com.singapore.trafficcamera.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.ActivityMainBinding
import com.singapore.trafficcamera.dialog.LoadingDialog
import com.singapore.trafficcamera.dialog.WarningDialog
import com.singapore.trafficcamera.fragment.CameraFragment
import com.singapore.trafficcamera.fragment.MapFragment
import com.singapore.trafficcamera.interfaces.SubscribeTrafficListener
import com.singapore.trafficcamera.interfaces.TrafficListener
import com.singapore.trafficcamera.interfaces.WarningDialogListener
import com.singapore.trafficcamera.models.TrafficItem
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.FavouriteStaticData
import com.singapore.trafficcamera.tools.Constants
import com.singapore.trafficcamera.utils.DateTimeUtils
import com.singapore.trafficcamera.utils.GeneralUtils
import com.singapore.trafficcamera.utils.UserManager
import com.singapore.trafficcamera.utils.WifiUtils
import com.singapore.trafficcamera.viewModels.FavouriteViewModel
import com.singapore.trafficcamera.viewModels.InternetViewModel
import com.singapore.trafficcamera.viewModels.TrafficViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TrafficListener, WarningDialogListener,
    SubscribeTrafficListener {

    private lateinit var binding: ActivityMainBinding

    private val internetViewModel: InternetViewModel by viewModels()
    private val trafficViewModel: TrafficViewModel by viewModels()
    private val favouriteViewModel: FavouriteViewModel by viewModels()

    private var loadingDialog: LoadingDialog?= null
    private var disposable: Disposable? = null

    private var cameraItemList: MutableList<CameraItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initInternetObserver()
        initObserver()
        getTrafficDataFromAPI()
        initClickEvent()
    }

    override fun onGetTrafficSuccess(data: ArrayList<TrafficItem>) {
        dismissLoadingDialog()

        val cameraList = data[0].cameras
        cameraList.sortBy { it.camera_id }
        setDataToCameraItemList(data = cameraList)

        //get data success then load mapFragment
        val mapFragment = MapFragment().newInstance(cameraList = cameraList)
        initFragment(fragment = mapFragment, tag = Constants.TAG_MAP)

        //start subscribe api
        subscribeToTrafficAPI()
    }

    override fun onGetTrafficFailed(error: String) {
        dismissLoadingDialog()
        showWarningDialog(content = error)
    }

    override fun onSubscribeSuccess(data: ArrayList<TrafficItem>) {
        val cameraList = data[0].cameras
        cameraList.sortBy { it.camera_id }
        setDataToCameraItemList(data = cameraList)

        //find currentFragment and update data
        when (val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_container)) {
            is MapFragment -> {
                currentFragment.updateCameraList(data = data)
            }
            is CameraFragment -> {
                currentFragment.updateCameraList(data = data)
            }
        }
    }

    override fun onSubscribeError(error: String) {
        disposable!!.dispose()
        //show error dialog
        showWarningDialog(content = error)
    }

    override fun onWarningDialogClicked() {
       Log.d("onWarningDialogClicked", "")
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
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

    private fun subscribeToTrafficAPI() {
        //initial delay put 60s -> mean after 1 min then call
        //subscribe put 60s -> mean after 60 sec, trigger again
        disposable = Observable
            .interval(Constants.SUBSCRIBE_INITIAL_DELAY, Constants.SUBSCRIBE_INTERVAL, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { subscribeToTrafficData() } )
                { throwable: Throwable -> subscribeFailed(throwable = throwable) }
    }

    private fun getTrafficDataFromAPI() {
        showLoadingDialog()

        val currentDateTime = DateTimeUtils.getApiDateTimeFormat()
        trafficViewModel.getTraffic(dateTime = currentDateTime, listener = this)
    }

    private fun subscribeToTrafficData() {
        val currentDateTime = DateTimeUtils.getApiDateTimeFormat()
        trafficViewModel.listenTrafficData(dateTime = currentDateTime, listener = this)
    }

    private fun subscribeFailed(throwable: Throwable) {
        disposable!!.dispose()
        //show error dialog
        showWarningDialog(content = throwable.localizedMessage!!)
    }

    private fun initClickEvent() {
        binding.bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_container)
            when (menuItem.itemId) {
                R.id.menu_map -> {
                    if (currentFragment is CameraFragment) {
                        val mapFragment = MapFragment().newInstance(cameraList = cameraItemList as ArrayList<CameraItem>)
                        initFragment(fragment = mapFragment, tag = Constants.TAG_MAP)
                    }
                }
                R.id.menu_camera -> {
                    if (currentFragment is MapFragment) {
                        val cameraFragment = CameraFragment().newInstance(cameraList = cameraItemList as ArrayList<CameraItem>)
                        initFragment(fragment = cameraFragment, tag = Constants.TAG_CAMERA)
                    }
                }
            }
            true
        }
    }

    private fun setDataToCameraItemList(data: ArrayList<CameraItem>) {
        cameraItemList.clear()
        cameraItemList.addAll(data)
    }

    private fun initFragment(fragment: Fragment, tag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_container, fragment, tag)
        fragmentTransaction.commit()
    }

    private fun initInternetObserver() {
        internetViewModel.connected.observe(this) { connected ->
            WifiUtils.wifi?.isConnectedInternet = connected
        }
    }

    private fun showWarningDialog(content: String) {
        val warningDialog = WarningDialog(context = this, listener = this,
            title = getString(R.string.label_warning), body = content)
        GeneralUtils.showDialog(dialog = warningDialog, cancelable = false)
    }

    private fun showLoadingDialog() {
        loadingDialog = LoadingDialog(context = this)
        loadingDialog!!.show()
        loadingDialog!!.setCancelable(false)
        loadingDialog!!.setCanceledOnTouchOutside(false)
        loadingDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismiss()
        }
    }
}