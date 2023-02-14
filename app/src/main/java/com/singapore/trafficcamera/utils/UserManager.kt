package com.singapore.trafficcamera.utils

import com.singapore.trafficcamera.models.dao.User

class UserManager private constructor() {
    var user: User?= null

    companion object {
        @get:Synchronized
        var data: UserManager? = null
            get() {
                if (field == null) {
                    field = UserManager()
                }
                return field
            }
            private set
    }

    fun clearUser() {
        user = null
    }
}