package com.kevinxu.remaidata

import android.app.Application
import android.content.Context
import com.kevinxu.remaidata.db.AppDataBase
import com.kevinxu.remaidata.model.MaxNotesStats
import com.kevinxu.remaidata.network.MaimaiDataClient
import com.kevinxu.remaidata.utils.SpUtil
import com.kevinxu.remaidata.widgets.Settings

/**
 * @author BBS
 * @since  2021/5/13
 */
class MaimaiDataApplication : Application() {
    companion object {
        lateinit var instance: MaimaiDataApplication
    }

    var maxNotesStats: MaxNotesStats? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
        MaimaiDataClient.instance.init()
    }

    override fun onCreate() {
        super.onCreate()

        Settings.init(this)

        SpUtil.init(this)

        AppDataBase.init(this)
    }
}