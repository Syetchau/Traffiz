package com.singapore.trafficcamera.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.singapore.trafficcamera.models.dao.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM user")
    suspend fun dropUserTable()

    @Transaction
    suspend fun dropUserTableAndReinsert(user: User) {
        dropUserTable()
        insertUser(user = user)
    }

    @Query("SELECT EXISTS(SELECT * FROM user WHERE username =:username)")
    fun checkIfUserExist(username: String): LiveData<Boolean>

    @Query("SELECT * FROM user WHERE username =:username")
    fun getPasswordFromUsername(username: String): LiveData<User>
}