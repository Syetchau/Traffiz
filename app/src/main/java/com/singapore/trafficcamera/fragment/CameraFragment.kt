package com.singapore.trafficcamera.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.activity.CameraDetailActivity
import com.singapore.trafficcamera.adapter.CameraAdapter
import com.singapore.trafficcamera.databinding.FragmentCameraBinding
import com.singapore.trafficcamera.dialog.LoadingDialog
import com.singapore.trafficcamera.dialog.WarningDialog
import com.singapore.trafficcamera.interfaces.CameraItemListener
import com.singapore.trafficcamera.interfaces.FavouriteListener
import com.singapore.trafficcamera.interfaces.TrafficListener
import com.singapore.trafficcamera.interfaces.WarningDialogListener
import com.singapore.trafficcamera.models.TrafficItem
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.dao.Favourite
import com.singapore.trafficcamera.tools.Constants
import com.singapore.trafficcamera.utils.DateTimeUtils
import com.singapore.trafficcamera.utils.GeneralUtils
import com.singapore.trafficcamera.utils.GeneralUtils.putParcelableExtra
import com.singapore.trafficcamera.utils.KeyboardUtils.hideKeyboard
import com.singapore.trafficcamera.utils.UserManager
import com.singapore.trafficcamera.viewModels.FavouriteViewModel
import com.singapore.trafficcamera.viewModels.TrafficViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CameraFragment : Fragment(), CameraItemListener, FavouriteListener, WarningDialogListener,
    TrafficListener {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val favouriteViewModel: FavouriteViewModel by viewModels()
    private val trafficViewModel: TrafficViewModel by viewModels()

    private var cameraAdapter: CameraAdapter?= null
    private var isSortByDesc = false
    private var isSearching = false

    //adapter list data
    private var cameraList: ArrayList<CameraItem> = ArrayList()

    //to hold search camera list
    private var searchCameraList: MutableList<CameraItem> = ArrayList()

    private var loadingDialog: LoadingDialog?= null

    //to hold calendar search data
    private var searchDate = 0
    private var searchMonth = 0
    private var searchYear = 0

    companion object fun newInstance(cameraList: ArrayList<CameraItem>): CameraFragment {
        val cameraFragment = CameraFragment()
        val argument = Bundle()
        argument.putParcelableArrayList(Constants.CAMERA_ITEM_LIST, cameraList)
        cameraFragment.arguments = argument
        return cameraFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            cameraList = requireArguments().getParcelableArrayList(Constants.CAMERA_ITEM_LIST)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initListener()
        initClickEvent()
        assignValueToSearchingData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCameraItemSelected(cameraItem: CameraItem, position: Int) {
        //hide keyboard, clear search
        binding.root.hideKeyboard()
        binding.etInput.text?.clear()
        binding.etInput.clearFocus()

        navigateToCameraDetailActivity(cameraItem = cameraItem, position = position)
    }

    override fun onCameraItemAddToFavourite(cameraItem: CameraItem, position: Int) {
        val favourite = Favourite()
        favourite.username = UserManager.data?.user?.username!!
        favourite.favouriteCamera = cameraItem.camera_id

        //insert to db
        favouriteViewModel.insertUserFavouriteCamera(
            favourite = favourite,
            position = position,
            listener = this
        )
    }

    override fun onCameraItemRemoveFromFavourite(cameraItem: CameraItem, position: Int) {
        //remove from db
        favouriteViewModel.deleteUserFavouriteCamera(
            username = UserManager.data?.user?.username!!,
            position = position,
            cameraId = cameraItem.camera_id,
            listener = this
        )
    }

    override fun onFavouriteItemUpdated(position: Int) {
        //need delay update, as getObserve need proceed data
        //not a good approach, come back to it after done other
        delayUpdateIcon(position = position, delaySec = Constants.DELAY_SEC)
    }

    override fun onGetTrafficSuccess(data: ArrayList<TrafficItem>) {
        dismissLoadingDialog()

        //check current sorting flag, to sort data and display
        val cameraList = data[0].cameras
        sortDataBasedOnStatus(cameraList = cameraList)
        searchCameraList.clear()
        searchCameraList.addAll(cameraList)

        //update to adapter
        cameraAdapter?.updateCameraList(cameraList = cameraList)
    }

    override fun onGetTrafficFailed(error: String) {
        dismissLoadingDialog()
        showWarningDialog(content = error)
    }

    //search traffic through calendar failed
    override fun onWarningDialogClicked() {
        Log.d("onWarningDialogClicked", "")
    }

    fun updateCameraList(data: ArrayList<TrafficItem>) {
        cameraList.clear()
        cameraList.addAll(data[0].cameras)

        //check currentFlag, as rx keep update data
        //prevent data position messed up as received data from rx had perform sorting
        sortDataBasedOnStatus(cameraList = cameraList)

        //check currently is searching data and is within today
        //if filtering data, dun update data, as the data will reshuffle
        if (isSearching) {
            return
        }

        //checking is date is match today
        //if searching by calendar, dun update data as well
        if (DateTimeUtils.checkingIsTodayDate(year = searchYear, month = searchMonth, date = searchDate)) {
            //update adapter item
            cameraAdapter?.updateCameraList(cameraList = cameraList)
        }
    }

    private fun initRecyclerView() {
        if (cameraList.isNotEmpty()) {
            cameraAdapter = CameraAdapter(context = requireContext(), cameraList = cameraList, listener = this)
            binding.rvCamera.layoutManager = LinearLayoutManager(requireContext())
            binding.rvCamera.setHasFixedSize(true)
            binding.rvCamera.adapter = cameraAdapter
        }
    }

    private fun sortDataBasedOnStatus(cameraList: MutableList<CameraItem>) {
        when {
            isSortByDesc -> {
                cameraList.sortByDescending { it.camera_id }
            }
            else -> {
                cameraList.sortBy { it.camera_id }
            }
        }
    }

    private fun performSortingSearchData() {
        isSortByDesc = !isSortByDesc
        val searchingData = cameraAdapter?.getCurrentList()

        checkingToSortData(cameraList = searchingData!!)
    }

    private fun performSortingAllData() {
        isSortByDesc = !isSortByDesc

        checkingToSortData(cameraList = cameraList)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkingToSortData(cameraList: MutableList<CameraItem>) {
        val drawable: Drawable?

        when {
            isSortByDesc -> {
                drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort_ascending)
                cameraList.sortByDescending { it.camera_id }
            }
            else -> {
                drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sory_descending)
                cameraList.sortBy { it.camera_id }
            }
        }
        binding.ivSort.setImageDrawable(drawable)
        cameraAdapter?.notifyDataSetChanged()
    }

    private fun initListener() {
        binding.etInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val input = text.toString().trim()
                when {
                    input.isEmpty() -> {
                        isSearching = false
                        when {
                            //reset today data
                            DateTimeUtils.checkingIsTodayDate(year = searchYear, month = searchMonth, date = searchDate) -> {
                                sortDataBasedOnStatus(cameraList = cameraList)
                                //update adapter item
                                cameraAdapter?.updateCameraList(cameraList = cameraList)
                            }
                            else -> {
                                //reset search calendar data
                                sortDataBasedOnStatus(cameraList = searchCameraList)
                                cameraAdapter?.updateCameraList(cameraList = searchCameraList)
                            }
                        }
                    }
                    else -> {
                        var searchingCameraList = searchCameraList
                        if (DateTimeUtils.checkingIsTodayDate(year = searchYear, month = searchMonth, date = searchDate)){
                            searchingCameraList = cameraList
                        }
                        searchTodayCamera(input = input, cameraList = searchingCameraList)
                    }
                }
            }

            override fun afterTextChanged(text: Editable?) {

            }
        })
    }

    private fun searchTodayCamera(input: String, cameraList: List<CameraItem>) {
        isSearching = true
        val searchingList: MutableList<CameraItem> = ArrayList()

        for (i in cameraList.indices) {
            if (cameraList[i].camera_id.contains(input.lowercase(Locale.ROOT), ignoreCase = true)) {
                searchingList.add(cameraList[i])
            }
        }
        sortDataBasedOnStatus(cameraList = searchingList)
        cameraAdapter?.updateCameraList(cameraList = searchingList)
    }

    private fun sortEvent() {
        ///check isSearching or search data from calendar
        //perform sort data based on these flag
        if (isSearching || !DateTimeUtils.checkingIsTodayDate(year = searchYear, month = searchMonth, searchDate) ) {
            performSortingSearchData()
            return
        }
        //sort today data
        performSortingAllData()
    }

    private fun initClickEvent() {
        binding.ivCalendar.setOnClickListener {
            showCalendarDialog()
        }

        binding.ivSort.setOnClickListener {
            sortEvent()
        }
    }

    private fun showCalendarDialog() {
        val calendarListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, date: Int ->
            //call api based on selected date
            //date picker month start from 0, so need increment 1
            searchYear = year
            searchMonth = month + 1
            searchDate = date
            getTrafficDataFromAPI(year = searchYear, month = searchMonth, date = searchDate)
        }
        //initialize datePicker with today date
        val datePicker = DatePickerDialog(
            requireContext(),
            calendarListener,
            DateTimeUtils.getCurrentYear(),
            DateTimeUtils.getCurrentMonth(),
            DateTimeUtils.getCurrentDate()
        )
        //limit to maxData = today date
        datePicker.datePicker.maxDate = DateTimeUtils.getCurrentTimeInMillis()
        datePicker.show()
    }

    private fun showWarningDialog(content: String) {
        val warningDialog = WarningDialog(context = context, listener = this,
            title = getString(R.string.label_warning), body = content)
        GeneralUtils.showDialog(dialog = warningDialog, cancelable = false)
    }

    private fun showLoadingDialog() {
        loadingDialog = LoadingDialog(context = context)
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

    private fun getTrafficDataFromAPI(year: Int, month: Int, date: Int) {
        showLoadingDialog()

        val searchDateTime = DateTimeUtils.getSearchDateTimeFormatForApi(
            year = year,
            month = month,
            date = date
        )
        //trigger api
        //time getCurrentTime, lazy to create timePicker
        trafficViewModel.getTraffic(dateTime = searchDateTime, listener = this)
    }

    private fun delayUpdateIcon(position: Int, delaySec: Long) {
        Handler().postDelayed({
            //update specific adapter item
            cameraAdapter?.updateCameraItem(position = position)
        }, delaySec)
    }

    private fun assignValueToSearchingData() {
        searchYear = DateTimeUtils.getCurrentYear()
        searchMonth = DateTimeUtils.getCurrentMonth()
        searchDate = DateTimeUtils.getCurrentDate()
    }

    private fun navigateToCameraDetailActivity(cameraItem: CameraItem, position: Int) {
        val intent = Intent(context, CameraDetailActivity::class.java)
        intent.putParcelableExtra(Constants.CAMERA_ITEM, cameraItem)
        intent.putExtra(Constants.CAMERA_ITEM_POS, position)
        launcher.launch(intent)
    }

    private var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val position = result.data?.getIntExtra(Constants.CAMERA_ITEM_POS, -1)
            delayUpdateIcon(position = position!!, delaySec = 0)
        }
    }
}