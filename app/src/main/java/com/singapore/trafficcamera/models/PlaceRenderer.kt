package com.singapore.trafficcamera.models

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.singapore.trafficcamera.R
import com.singapore.trafficcamera.utils.BitmapUtils

class PlaceRenderer(private val context: Context, map: GoogleMap, clusterManager: ClusterManager<CameraItem>):
    DefaultClusterRenderer<CameraItem>(context, map, clusterManager) {

    /**
     * The icon to use for each cluster item
     */
    private val markerIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(context, R.color.color_primary)
        BitmapUtils.vectorToBitmap(context, R.drawable.ic_marker, color)
    }

    /**
     * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
     * options should be set
     */
    override fun onBeforeClusterItemRendered(item: CameraItem, markerOptions: MarkerOptions) {
        markerOptions.title(item.camera_id)
            .position(LatLng(item.location?.latitude!!, item.location?.longitude!!))
            .icon(markerIcon)
    }

    /**
     * Method called right after the cluster item (i.e. the marker) is rendered. This is where
     * properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: CameraItem, marker: Marker) {
        marker.tag = clusterItem
    }
}