package com.singapore.trafficcamera.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.databinding.FragmentMapBinding
import com.singapore.trafficcamera.dialog.MarkerInfoWindowAdapter
import com.singapore.trafficcamera.models.CameraItem
import com.singapore.trafficcamera.models.TrafficItem
import com.singapore.trafficcamera.models.PlaceRenderer
import com.singapore.trafficcamera.tools.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var cameraList: ArrayList<CameraItem> = ArrayList()
    private lateinit var clusterManager: ClusterManager<CameraItem>

    companion object fun newInstance(cameraList: ArrayList<CameraItem>): MapFragment {
        val mapFragment = MapFragment()
        val argument = Bundle()
        argument.putParcelableArrayList(Constants.CAMERA_ITEM_LIST, cameraList)
        mapFragment.arguments = argument
        return mapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            cameraList = requireArguments().getParcelableArrayList(Constants.CAMERA_ITEM_LIST)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        initMapFragment()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMapFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateCameraList(data: ArrayList<TrafficItem>) {
        cameraList.clear()
        cameraList.addAll(data[0].cameras)

        //get latest data and update data to map
        clusterManager.clearItems()
        clusterManager.addItems(cameraList)
        clusterManager.cluster()
    }

    private fun initMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycleScope.launchWhenCreated {
            // Get map
            val googleMap = mapFragment.awaitMap()

            addClusteredMarkers(googleMap)

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            // Ensure all places are visible in the map
            val bounds = LatLngBounds.builder()
            cameraList.forEach {
                bounds.include(LatLng(it.location?.latitude!!, it.location?.longitude!!))
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))

            //add zoom, compass, my location
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer
        clusterManager = ClusterManager<CameraItem>(requireContext(), googleMap)
        clusterManager.renderer = PlaceRenderer(
            context = requireContext(),
            map = googleMap,
            clusterManager = clusterManager
        )
        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(context = requireContext()))

        // set listener to it
        clusterManager.markerCollection.setOnInfoWindowClickListener { marker ->
            when {
                marker.isInfoWindowShown -> {
                    marker.hideInfoWindow()
                }
                else -> {
                    marker.showInfoWindow()
                }
            }
        }
        // Add the location to the ClusterManager
        clusterManager.addItems(cameraList)
        clusterManager.cluster()

        // When the camera starts moving, change the alpha value of the marker to translucent
        googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }
    }
}