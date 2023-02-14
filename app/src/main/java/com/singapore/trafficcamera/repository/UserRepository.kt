package com.singapore.trafficcamera.repository

import androidx.lifecycle.LiveData
import com.singapore.trafficcamera.daos.UserDao
import com.singapore.trafficcamera.models.dao.User
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user = user)
    }

    suspend fun dropUserTable() {
        userDao.dropUserTable()
    }

    suspend fun dropUserTableAndReinsert(user: User) {
        userDao.dropUserTableAndReinsert(user = user)
    }

    fun checkIfUserExist(username: String): LiveData<Boolean> {
        return userDao.checkIfUserExist(username = username)
    }

    fun getPasswordFromUsername(username: String): LiveData<User> {
        return userDao.getPasswordFromUsername(username = username)
    }
}