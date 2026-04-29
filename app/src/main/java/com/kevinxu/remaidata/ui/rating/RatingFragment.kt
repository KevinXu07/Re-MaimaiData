package com.kevinxu.remaidata.ui.rating

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.crawler.CrawlerCaller
import com.kevinxu.remaidata.crawler.WechatCrawlerListener
import com.kevinxu.remaidata.databinding.FragmentRatingBinding
import com.kevinxu.remaidata.network.server.HttpServerService
import com.kevinxu.remaidata.network.vpn.core.LocalVpnService
import com.kevinxu.remaidata.ui.BaseFragment
import com.kevinxu.remaidata.ui.about.SettingsActivity
import com.kevinxu.remaidata.ui.checklist.LevelCheckActivity
import com.kevinxu.remaidata.ui.checklist.VersionCheckActivity
import com.kevinxu.remaidata.ui.finaletodx.FinaleToDxActivity
import com.kevinxu.remaidata.ui.maimaidxprober.LoginActivity
import com.kevinxu.remaidata.ui.maimaidxprober.ProberActivity
import com.kevinxu.remaidata.ui.theme.MaimaiDataTheme
import com.kevinxu.remaidata.utils.SpUtil
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class RatingFragment : BaseFragment<FragmentRatingBinding>(), WechatCrawlerListener,
    LocalVpnService.onStatusChangedListener {

    private lateinit var binding: FragmentRatingBinding

    private val proberUpdateDialog by lazy { ProberUpdateDialog(requireContext()) }
    private lateinit var proberUpdateTipsDialog: MaterialDialog


    private val httpServiceIntent by lazy {
        Intent(requireContext(), HttpServerService::class.java)
    }

    private val vpnActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startProxyServices()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RatingFragment()

        const val TAG = "RatingFragment"

    }

    override fun getViewBinding(container: ViewGroup?): FragmentRatingBinding {
        binding = FragmentRatingBinding.inflate(layoutInflater, container, false)
        return binding
    }

    override fun onResume() {
        super.onResume()
        if (SpUtil.getUserName().isEmpty()) {
            binding.accountText.setText(R.string.no_logged_in)
            binding.proberQueryBtn.visibility = View.GONE
            binding.proberLoginBtn.setText(R.string.login)
            binding.proberLoginBtn.layoutParams = binding.proberLoginBtn.layoutParams.apply {
                width = (260 * binding.root.context.resources.displayMetrics.density).toInt()
            }
        } else {
            binding.accountText.text = SpUtil.getUserName()
            binding.proberQueryBtn.visibility = View.VISIBLE
            binding.proberLoginBtn.setText(R.string.change_account)
            binding.proberLoginBtn.layoutParams = binding.proberLoginBtn.layoutParams.apply {
                width = (120 * binding.root.context.resources.displayMetrics.density).toInt()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ratingCalculatorCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaimaiDataTheme {
                    RatingCalculator(
                        onInvalidTargetRating = ::showToast,
                        onCalculateClick = { hideKeyboard(binding.ratingCalculatorCompose) }
                    )
                }
            }
        }

        binding.proberLoginBtn.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
        }

        binding.proberQueryBtn.setOnClickListener {
            startActivity(Intent(context, ProberActivity::class.java))
        }

        binding.proberLoginBtn.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
        }

        binding.proberLevelCheckBtn.setOnClickListener {
            startActivity(Intent(context, LevelCheckActivity::class.java))
        }

        binding.proberVersionCheckBtn.setOnClickListener {
            startActivity(Intent(context, VersionCheckActivity::class.java))
        }

        binding.proberFinaleToDxBtn.setOnClickListener {
            startActivity(Intent(context, FinaleToDxActivity::class.java))
        }

        CrawlerCaller.setOnWechatCrawlerListener(this)
        LocalVpnService.addOnStatusChangedListener(this)

        binding.proberProxySimpleText.setOnClickListener {
            proberUpdateDialog.show()
        }

        binding.proberProxyUpdateBtn.setOnClickListener {
            if (!LocalVpnService.IsRunning) {
                val intent: Intent? = LocalVpnService.prepare(context)
                if (intent == null) {
                    startProxyServices()
                } else {
                    vpnActivityResultLauncher.launch(intent)
                }
            } else {
                LocalVpnService.IsRunning = false
                stopHttpService()
            }
        }

        binding.proberProxyUpdateHelpIv.setOnClickListener {
            showHelpDialog()
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.settings).isVisible = !isHidden
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.about_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.settings -> {
                        startActivity(Intent(requireContext(), SettingsActivity::class.java))
                        return true
                    }
                }
                return false
            }

        })
    }

    private fun startProxyServices() {
        if (SpUtil.getUserName().isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.vpn_please_login),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        startVPNService()
        startHttpService()
        createLinkUrl()
        getWechatApi()
    }


    private fun createLinkUrl() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val randomChar = (1..10)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")

        val link = "http://127.0.0.2:8284/$randomChar"

        val mClipData = ClipData.newPlainText("copyText", link)
        (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            mClipData
        )

        Toast.makeText(
            requireContext(),
            "已复制链接，请在微信中粘贴并打开",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getWechatApi() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            val cmp = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            intent.apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = cmp
            }
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun startVPNService() {
        requireContext().startService(Intent(requireContext(), LocalVpnService::class.java))
    }

    private fun startHttpService() {
        requireContext().startService(httpServiceIntent)
    }

    private fun stopHttpService() {
        requireContext().stopService(httpServiceIntent)
    }

    private fun showToast() {
        val text = "请输入内容!"
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this.context, text, duration)
        toast.show()
    }

    override fun onStatusChanged(status: String, isRunning: Boolean) {
        binding.proberProxyUpdateBtn.setText(if (isRunning) R.string.stop_proxy else R.string.start_proxy)
    }

    @SuppressLint("SetTextI18n")
    override fun onLogReceived(logString: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis())
        proberUpdateDialog.appendText("[$timestamp] $logString\n")
    }

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(logString: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis())
        proberUpdateDialog.appendText("[$timestamp] $logString\n")
        binding.proberProxySimpleText.text = "[$timestamp] $logString"
    }

    override fun onStartAuth() {
        binding.proberProxySimpleText.text = ""
        binding.proberProxyIndicator.isIndeterminate = true
        binding.proberProxyStatusGroup.visibility = View.VISIBLE
    }

    override fun onFinishUpdate() {
        binding.proberProxyIndicator.visibility = View.INVISIBLE
        stopHttpService()
    }

    @SuppressLint("SetTextI18n")
    override fun onError(e: Exception) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis())
        proberUpdateDialog.appendText("[$timestamp] $e\n")
        binding.proberProxySimpleText.text = "[$timestamp] $e"
        binding.proberProxyIndicator.visibility = View.INVISIBLE
        stopHttpService()
    }

    override fun onDestroy() {
        super.onDestroy()
        CrawlerCaller.removeOnWechatCrawlerListener()
        LocalVpnService.removeOnStatusChangedListener(this)
    }

    private fun showHelpDialog() {
        if (::proberUpdateTipsDialog.isInitialized) {
            proberUpdateTipsDialog.show()
        } else {
            var helpStringBuilder = ""
            helpStringBuilder = getString(R.string.prober_update_content_step1) + "\n" +
                    getString(R.string.prober_update_content_step2) + "\n" +
                    getString(R.string.prober_update_content_step3) + "\n" +
                    getString(R.string.prober_update_content_step4) + "\n"

            proberUpdateTipsDialog = MaterialDialog.Builder(requireContext())
                .title(getString(R.string.prober_update_help))
                .content(helpStringBuilder)
                .build()
            proberUpdateTipsDialog.show()
        }
    }
}
