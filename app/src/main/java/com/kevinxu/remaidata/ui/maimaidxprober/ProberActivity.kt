package com.kevinxu.remaidata.ui.maimaidxprober

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.databinding.ActivityProberBinding
import com.kevinxu.remaidata.db.AppDataBase
import com.kevinxu.remaidata.db.entity.RecordEntity
import com.kevinxu.remaidata.db.entity.SongWithChartsEntity
import com.kevinxu.remaidata.network.MaimaiDataRequests
import com.kevinxu.remaidata.repository.RecordRepository
import com.kevinxu.remaidata.repository.SongWithChartRepository
import com.kevinxu.remaidata.utils.ConvertUtils
import com.kevinxu.remaidata.utils.CreateBest50
import com.kevinxu.remaidata.utils.JsonConvertToDb
import com.kevinxu.remaidata.utils.PermissionHelper
import com.kevinxu.remaidata.utils.SpUtil
import com.kevinxu.remaidata.widgets.AnimationHelper
import com.kevinxu.remaidata.widgets.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProberBinding

    private lateinit var proberVersionAdapter: ProberVersionAdapter
    private var recordList = listOf<RecordEntity>()
    private var oldRating = listOf<RecordEntity>()
    private var newRating = listOf<RecordEntity>()
    private var dataList = listOf<SongWithChartsEntity>()

    private lateinit var animationHelper: AnimationHelper

    private lateinit var permissionHelper: PermissionHelper

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            permissionHelper.onRequestPermissionsResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animationHelper = AnimationHelper(layoutInflater)
        binding.proberContainerLayout.addView(animationHelper.loadLayout(), 0)
        animationHelper.startAnimation()

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        supportActionBar?.title = getString(R.string.maimaidx_prober)
        binding.oldVersionRdoBtn.text = String.format(getString(R.string.old_version_35), 0)
        binding.newVersionRdoBtn.text = String.format(getString(R.string.new_version_15), 0)

        permissionHelper = PermissionHelper.with(this)


        SongWithChartRepository.getInstance(AppDataBase.getInstance().songWithChartDao())
            .getAllSongWithCharts().observe(this) {
                dataList = it
                setData(dataList)
            }

    }

    private fun setData(list: List<SongWithChartsEntity>) {
        binding.proberVp.apply {
            proberVersionAdapter = ProberVersionAdapter(list)
            adapter = proberVersionAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        binding.oldVersionRdoBtn.isChecked = true
                        binding.oldVersionIndicator.visibility = View.VISIBLE
                        binding.newVersionIndicator.visibility = View.GONE
                    } else {
                        binding.newVersionRdoBtn.isChecked = true
                        binding.oldVersionIndicator.visibility = View.GONE
                        binding.newVersionIndicator.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.versionGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.old_version_rdo_btn -> {
                    binding.proberVp.currentItem = 0
                    binding.oldVersionIndicator.visibility = View.VISIBLE
                    binding.newVersionIndicator.visibility = View.GONE
                }

                R.id.new_version_rdo_btn -> {
                    binding.proberVp.currentItem = 1
                    binding.oldVersionIndicator.visibility = View.GONE
                    binding.newVersionIndicator.visibility = View.VISIBLE
                }
            }
        }

        binding.refreshLayout.apply {
            isEnabled = false
            isRefreshing = true
            setColorSchemeResources(R.color.colorPrimary)
        }

        getRecord(list)

    }

    @SuppressLint("CheckResult")
    private fun getRecord(list: List<SongWithChartsEntity>) {
        MaimaiDataRequests.getRecords(SpUtil.getCookie()).subscribe({ it ->
            binding.refreshLayout.isRefreshing = false
            val hasStatus = it.asJsonObject.has("status")
            if (hasStatus) {
                if (it.asJsonObject.get("status").asString == "error") {
                    Toast.makeText(
                        this@ProberActivity, "请求出错，请重新登录", Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ProberActivity, LoginActivity::class.java))
                    finish()
                    return@subscribe
                }
            }

            if (Settings.getEnableDivingFishNickname()) {
                val hasNickname = it.asJsonObject.has("nickname")
                if (hasNickname) {
                    SpUtil.saveDivingFishNickname(it.asJsonObject.get("nickname").asString)
                } else {
                    SpUtil.saveDivingFishNickname("")
                }
            }

            val hasRecords = it.asJsonObject.has("records")
            if (hasRecords) {
                val convertRecord = JsonConvertToDb.convertRecord(it.asJsonObject.get("records"))
                lifecycleScope.launch {
                    RecordRepository.getInstance(AppDataBase.getInstance().recordDao())
                        .replaceAllRecord(convertRecord)
                }


                recordList = convertRecord
                proberVersionAdapter.setData(recordList)

                if (!proberVersionAdapter.isDataMatching()) {
                    MaterialDialog.Builder(this@ProberActivity)
                        .title(getString(R.string.mismatching_data_title))
                        .content(getString(R.string.mismatching_data_content))
                        .positiveText(R.string.common_confirm).show()
                }

                oldRating = recordList.sortedByDescending {
                    it.ra
                }.filter {
                    val find = list.find { data -> data.songData.id == it.songId }
                    if (find == null) false else !find.songData.isNew
                }.let {
                    it.subList(0, if (it.size >= 35) 35 else it.size)
                }
                newRating = recordList.sortedByDescending {
                    it.ra
                }.filter {
                    val find = list.find { data -> data.songData.id == it.songId }
                    find?.songData?.isNew == true
                }.let {
                    it.subList(0, if (it.size >= 15) 15 else it.size)
                }

                binding.oldVersionRdoBtn.text = String.format(
                    getString(R.string.old_version_35), oldRating.sumOf {
                        ConvertUtils.achievementToRating(
                            (it.ds * 10).toInt(), (it.achievements * 10000).toInt()
                        )
                    })
                binding.newVersionRdoBtn.text = String.format(
                    getString(R.string.new_version_15), newRating.sumOf {
                        ConvertUtils.achievementToRating(
                            (it.ds * 10).toInt(), (it.achievements * 10000).toInt()
                        )
                    })

            }
        }, { error ->
            binding.refreshLayout.isRefreshing = false
            error.printStackTrace()
            Toast.makeText(this@ProberActivity, "请求出错，请重新登录", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@ProberActivity, LoginActivity::class.java))
            finish()
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.menu_share -> permissionHelper.registerLauncher(requestPermissionLauncher)
                .checkStoragePermission(object : PermissionHelper.PermissionCallback {
                    override fun onAllGranted() {
                        createImage()
                    }

                    override fun onDenied(deniedPermissions: List<String>) {
                        Toast.makeText(
                            this@ProberActivity,
                            getString(R.string.storage_permission_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        return true
    }


    private fun createImage() {
        lifecycleScope.launch {
            binding.loading.visibility = View.VISIBLE
            CreateBest50.createSongInfo(
                this@ProberActivity, dataList, oldRating, newRating
            )

            binding.loading.visibility = View.GONE
        }

    }


    override fun onResume() {
        super.onResume()
        animationHelper.resumeAnimation()
    }

    override fun onPause() {
        super.onPause()
        animationHelper.pauseAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        animationHelper.stopAnimation()
    }


}