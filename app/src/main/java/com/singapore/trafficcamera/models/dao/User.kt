package com.singapore.trafficcamera.models.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity("user")
class User(username: String, password: String): Serializable {
    @PrimaryKey
    var username: String = ""
    var password: String = ""

    init {
        this.username = username
        this.password = password
    }
}