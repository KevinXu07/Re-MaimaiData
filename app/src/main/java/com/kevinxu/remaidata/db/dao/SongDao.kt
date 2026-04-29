package com.kevinxu.remaidata.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kevinxu.remaidata.db.entity.SongDataEntity

@Dao
interface SongDao {

    @Insert
    fun insertAllSongs(songDataList: List<SongDataEntity>)

    @Query("DELETE FROM song_data")
    fun clearSongData()

}