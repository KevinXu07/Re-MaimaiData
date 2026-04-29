package com.kevinxu.remaidata.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import com.kevinxu.remaidata.BuildConfig
import com.kevinxu.remaidata.db.AppDataBase.Companion.DATABASE_VERSION
import com.kevinxu.remaidata.db.dao.AliasDao
import com.kevinxu.remaidata.db.dao.ChartDao
import com.kevinxu.remaidata.db.dao.ChartStatsDao
import com.kevinxu.remaidata.db.dao.RecordDao
import com.kevinxu.remaidata.db.dao.SongDao
import com.kevinxu.remaidata.db.dao.SongWithChartsDao
import com.kevinxu.remaidata.db.entity.AliasEntity
import com.kevinxu.remaidata.db.entity.ChartEntity
import com.kevinxu.remaidata.db.entity.ChartStatsEntity
import com.kevinxu.remaidata.db.entity.ListIntConverter
import com.kevinxu.remaidata.db.entity.RecordEntity
import com.kevinxu.remaidata.db.entity.SongDataEntity
import com.kevinxu.remaidata.utils.SpUtil

@Database(
    entities = [SongDataEntity::class, ChartEntity::class, AliasEntity::class, RecordEntity::class, ChartStatsEntity::class],
    version = DATABASE_VERSION
)
@TypeConverters(ListIntConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun chartDao(): ChartDao
    abstract fun songWithChartDao(): SongWithChartsDao
    abstract fun aliasDao(): AliasDao
    abstract fun recordDao(): RecordDao
    abstract fun chartStatsDao(): ChartStatsDao


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "maimaidata_db"

        @Volatile
        private lateinit var instance: AppDataBase


        fun getInstance(): AppDataBase {
            if (!::instance.isInitialized) {
                throw IllegalStateException("AppDataBase must be initialized first. Call init(context) before getInstance().")
            }
            return instance
        }

        fun init(context: Context): AppDataBase {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDataBase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(BuildConfig.DEBUG)
                .addCallback(object : Callback() {
                    override fun onDestructiveMigration(connection: SQLiteConnection) {
                        SpUtil.setDataVersion("0")
                    }
                })
                .build()
            return instance
        }
    }


}
