package com.singapore.trafficcamera.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singapore.trafficcamera.models.dao.User
import com.singapore.trafficcamera.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository): ViewModel() {

    fun insertUser(user: User) = viewModelScope.launch {
        userRepository.insertUser(user = user)
    }

    fun dropUserTable() = viewModelScope.launch {
        userRepository.dropUserTable()
    }

    fun dropUserTableAndReinsert(user: User) = viewModelScope.launch {
        userRepository.dropUserTableAndReinsert(user = user)
    }

    fun checkIfUserExist(username: String): LiveData<Boolean> {
        return userRepository.checkIfUserExist(username = username)
    }

    fun getPasswordFromUsername(username: String): LiveData<User> {
        return userRepository.getPasswordFromUsername(username = username)
    }
}