package com.singapore.trafficcamera.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singapore.trafficcamera.interfaces.FavouriteListener
import com.singapore.trafficcamera.models.dao.Favourite
import com.singapore.trafficcamera.repository.FavouriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(private val favouriteRepository: FavouriteRepository): ViewModel() {

    fun insertUserFavouriteCamera(favourite: Favourite, position: Int, listener: FavouriteListener) = viewModelScope.launch {
        favouriteRepository.insertUserFavouriteCamera(favourite = favourite)
        listener.onFavouriteItemUpdated(position = position)
    }

    fun deleteUserFavouriteCamera(username: String, cameraId: String, position: Int, listener: FavouriteListener) = viewModelScope.launch {
        favouriteRepository.deleteUserFavouriteCamera(username = username, cameraId = cameraId)
        listener.onFavouriteItemUpdated(position = position)
    }

    fun dropFavouriteTable() = viewModelScope.launch {
        favouriteRepository.dropFavouriteTable()
    }

    fun getFavouriteCameraBasedOnUsername(username: String): LiveData<List<String>> {
        return favouriteRepository.getFavouriteCameraBasedOnUsername(username = username)
    }
}