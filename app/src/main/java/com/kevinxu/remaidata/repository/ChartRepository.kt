package com.kevinxu.remaidata.repository

import androidx.lifecycle.LiveData
import com.kevinxu.remaidata.db.dao.ChartDao
import com.kevinxu.remaidata.model.MaxNotesStats

class ChartRepository private constructor(private val chartDao: ChartDao) {
    companion object {
        @Volatile
        private var instance: ChartRepository? = null
        fun getInstance(chartDao: ChartDao): ChartRepository {
            if (instance == null) {
                instance = ChartRepository(chartDao)
            }
            return instance!!
        }
    }

    /**
     * 获取最大音符数据，用于展示图表
     */
    fun getMaxNotes(): LiveData<MaxNotesStats> {
        return chartDao.getMaxNotes()
    }

}
