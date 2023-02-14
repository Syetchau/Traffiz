package com.singapore.trafficcamera.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.singapore.trafficcamera.models.dao.Favourite

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFavouriteCamera(favourite: Favourite)

    @Query("DELETE from favourite WHERE username =:username AND favouriteCamera =:cameraId")
    suspend fun deleteUserFavouriteCamera(username: String, cameraId: String)

    @Query("DELETE FROM favourite")
    suspend fun dropFavouriteTable()

    @Query("SELECT favouriteCamera as favouriteCamera FROM favourite WHERE username =:username")
    fun getFavouriteCameraBasedOnUsername(username: String): LiveData<List<String>>
}