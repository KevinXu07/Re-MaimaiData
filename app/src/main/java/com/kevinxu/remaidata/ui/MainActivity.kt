package com.kevinxu.remaidata.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener3
import com.kevinxu.remaidata.BuildConfig
import com.kevinxu.remaidata.MaimaiDataApplication
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.databinding.ActivityMainBinding
import com.kevinxu.remaidata.db.AppDataBase
import com.kevinxu.remaidata.model.AppUpdateModel
import com.kevinxu.remaidata.network.MaimaiDataRequests
import com.kevinxu.remaidata.repository.ChartRepository
import com.kevinxu.remaidata.repository.ChartStatsRepository
import com.kevinxu.remaidata.repository.SongDataRepository
import com.kevinxu.remaidata.repository.SongWithChartRepository
import com.kevinxu.remaidata.ui.rating.RatingFragment
import com.kevinxu.remaidata.ui.songlist.SongListFragment
import com.kevinxu.remaidata.utils.JsonConvertToDb
import com.kevinxu.remaidata.utils.SpUtil
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var ratingFragment: RatingFragment
    private lateinit var songListFragment: SongListFragment
    private var updateDisposable: Disposable? = null
    private var checkChartStatusDisposable: Disposable? = null
    private var isUpdateChecked = false
    private var downloadTask: DownloadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLayout.toolbar)

        checkChartStatus()

        queryMaxNotes()

        if (savedInstanceState != null) {
            supportActionBar?.title = savedInstanceState.getString("TOOLBAR_TITLE")

            supportFragmentManager.getFragment(
                savedInstanceState, SongListFragment.TAG
            )?.apply {
                songListFragment = this as SongListFragment
            }

            supportFragmentManager.getFragment(
                savedInstanceState, RatingFragment.TAG
            )?.apply {
                ratingFragment = this as RatingFragment
            }
        } else {
            showFragment(R.id.navDXSongList)
        }

        binding.mainBottomNaviView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navDXSongList -> {
                    showFragment(R.id.navDXSongList)
                    true
                }

                R.id.navDxTarget -> {
                    showFragment(R.id.navDxTarget)
                    true
                }

                else -> {
                    true
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("TOOLBAR_TITLE", supportActionBar?.title.toString())
        if (::songListFragment.isInitialized) supportFragmentManager.putFragment(
            outState, SongListFragment.TAG, songListFragment
        )
        if (::ratingFragment.isInitialized) supportFragmentManager.putFragment(
            outState, RatingFragment.TAG, ratingFragment
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isUpdateChecked) {
            updateDisposable?.dispose()
            checkUpdate()
        }
    }

    /**
     * check update
     */
    private fun checkUpdate() {
        updateDisposable = Observable.zip(
            MaimaiDataRequests.fetchAppUpdateInfo(),
            MaimaiDataRequests.fetchDataUpdateInfo(),
        ) { appUpdate, dataUpdate ->
            appUpdate to dataUpdate
        }.subscribe({ (appUpdate, dataUpdate) ->
            isUpdateChecked = true
            if (isNewerVersion(appUpdate.version, BuildConfig.VERSION_NAME) && !appUpdate.url.isNullOrBlank()) {
                MaterialDialog.Builder(this).title(
                    this@MainActivity.getString(
                        R.string.maimai_data_update_title, appUpdate.version
                    )
                ).content(
                    (appUpdate.info
                        ?: this@MainActivity.getString(R.string.maimai_data_update_default_content)).replace(
                        "\\n", "\n"
                    )
                ).positiveText(R.string.maimai_data_update_download)
                    .negativeText(R.string.common_cancel).onPositive { _, which ->
                        if (DialogAction.POSITIVE == which) {
                            startActivity(Intent().apply {
                                action = Intent.ACTION_VIEW
                                data = Uri.parse(appUpdate.url)
                            })
                        }
                    }.onNegative { d, _ ->
                        d.dismiss()
                    }.autoDismiss(true).cancelable(true).show()
            } else if (!dataUpdate.dataVersion3.isNullOrBlank()
                && SpUtil.getDataVersion() < dataUpdate.dataVersion3!!
            ) {
                MaterialDialog.Builder(this)
                    .title(this@MainActivity.getString(R.string.maimai_data_data_update_title))
                    .content(
                        String.format(
                            this@MainActivity.getString(R.string.maimai_data_data_update_info),
                            SpUtil.getDataVersion(),
                            dataUpdate.dataVersion3
                        )
                    ).positiveText(R.string.maimai_data_update_download)
                    .negativeText(R.string.common_cancel).onPositive { _, which ->
                        if (DialogAction.POSITIVE == which) {
                            startDataDownload(dataUpdate)
                        }
                    }.onNegative { d, _ ->
                        d.dismiss()
                    }.autoDismiss(true).cancelable(true).show()
            }
        }, {
            it.printStackTrace()
        })
    }

    private fun isNewerVersion(remoteVersion: String?, localVersion: String): Boolean {
        if (remoteVersion.isNullOrBlank()) return false

        val remoteParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = localVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(remoteParts.size, localParts.size)

        for (index in 0 until maxLength) {
            val remote = remoteParts.getOrElse(index) { 0 }
            val local = localParts.getOrElse(index) { 0 }
            if (remote != local) {
                return remote > local
            }
        }
        return false
    }

    /**
     * 查询最大notes数量
     */
    private fun queryMaxNotes() {
        ChartRepository.getInstance(AppDataBase.getInstance().chartDao()).getMaxNotes()
            .observe(this) {
                MaimaiDataApplication.instance.maxNotesStats = it
            }
    }

    /**
     * 检查水鱼谱面数据
     */
    private fun checkChartStatus() {
        //每五天更新数据
        val lastUpdateTime = SpUtil.getLastUpdateChartStats()
        val currentTime = System.currentTimeMillis()
        val fiveDaysMillis = 5 * 24 * 60 * 60 * 1000L
        if ((currentTime - lastUpdateTime) >= fiveDaysMillis) {
            checkChartStatusDisposable = MaimaiDataRequests.getChartStatus().subscribe(
                { t ->
                    lifecycleScope.launch {
                        val convertChatStats = JsonConvertToDb.convertChatStats(t)
                        val result = ChartStatsRepository.getInstance(
                            AppDataBase.getInstance().chartStatsDao()
                        ).replaceAllChartStats(convertChatStats)
                        if (result) {
                            SpUtil.saveLastUpdateChartStats(currentTime)
                        }
                    }
                }, {
                    it.printStackTrace()
                    Toast.makeText(this, "谱面状态数据下载失败", Toast.LENGTH_LONG).show()
                })
        }
    }

    private fun startDataDownload(appUpdateModel: AppUpdateModel) {
        val updateDialog =
            MaterialDialog.Builder(this).title(getString(R.string.maimai_data_download_title))
                .content(getString(R.string.maimai_data_start_download)).cancelable(false).show()
        downloadTask =
            DownloadTask.Builder(
                appUpdateModel.dataUrl3!!,
                filesDir.path,
                "songdata.json"
            )
                .setMinIntervalMillisCallbackProcess(16).setPassIfAlreadyCompleted(false).build()


        downloadTask!!.enqueue(object : DownloadListener3() {
            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {
            }

            override fun connected(
                task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long
            ) {
            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                updateDialog.setContent("$currentOffset/$totalLength")
            }

            override fun started(task: DownloadTask) {
                updateDialog.show()
            }

            override fun completed(task: DownloadTask) {
                updateDialog.dismiss()
                lifecycleScope.launch {
                    val data = SongDataRepository().getData(this@MainActivity)
                    val result = SongWithChartRepository.getInstance(
                        AppDataBase.getInstance().songWithChartDao()
                    ).updateDatabase(data)
                    if (result) {
                        SpUtil.setDataVersion(appUpdateModel.dataVersion3!!)
                    }
                }
            }

            override fun canceled(task: DownloadTask) {
                updateDialog.dismiss()
            }

            override fun error(task: DownloadTask, e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.maimai_data_download_error),
                    Toast.LENGTH_SHORT
                ).show()
                updateDialog.dismiss()
            }

            override fun warn(task: DownloadTask) {
            }

        })
    }

    private fun showFragment(int: Int) {
        invalidateMenu()
        val ft = supportFragmentManager.beginTransaction()
        hideAllFragment(ft)
        when (int) {

            R.id.navDxTarget -> {
                supportActionBar?.setTitle(R.string.dx_rating_correlation)
                if (!::ratingFragment.isInitialized) {
                    ratingFragment = RatingFragment.newInstance()
                    ft.add(R.id.fragment_content, ratingFragment, RatingFragment.TAG)
                } else {
                    ft.show(ratingFragment)
                }
            }

            R.id.navDXSongList -> {
                supportActionBar?.setTitle(R.string.dx_song_list)
                if (!::songListFragment.isInitialized) {
                    songListFragment = SongListFragment.newInstance()
                    ft.add(R.id.fragment_content, songListFragment, SongListFragment.TAG)
                } else {
                    ft.show(songListFragment)
                }

            }
        }
        ft.commit()
    }

    private fun hideAllFragment(ft: FragmentTransaction) {
        ft.apply {
            if (::ratingFragment.isInitialized) {
                hide(ratingFragment)
            }
            if (::songListFragment.isInitialized) {
                hide(songListFragment)
            }
        }
    }
}
