package com.kevinxu.remaidata.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kevinxu.remaidata.BuildConfig
import com.kevinxu.remaidata.ui.theme.MaimaiDataTheme
import com.kevinxu.remaidata.utils.SpUtil
import com.kevinxu.remaidata.widgets.Settings
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    companion object {
        private const val PROJECT_URL = "https://github.com/KevinXu07/Re-MaimaiData"
        private const val FEEDBACK_URL = "https://github.com/KevinXu07/Re-MaimaiData/issues"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var settingsState by remember {
                mutableStateOf(
                    SettingsUiState(
                        showAlias = Settings.getEnableShowAlias(),
                        aliasSearch = Settings.getEnableAliasSearch(),
                        charterSearch = Settings.getEnableCharterSearch(),
                        useDivingFishNickname = Settings.getEnableDivingFishNickname(),
                        nickname = Settings.getNickname(),
                        selectedDifficulties = Settings.getUpdateDifficulty()
                    )
                )
            }

            MaimaiDataTheme {
                SettingsScreen(
                    settingsState = settingsState,
                    aboutState = AboutUiState(
                        versionName = BuildConfig.VERSION_NAME,
                        dataVersion = SpUtil.getDataVersion(),
                        chartStatsUpdateTime = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(SpUtil.getLastUpdateChartStats()),
                        projectUrl = PROJECT_URL,
                        feedbackUrl = FEEDBACK_URL
                    ),
                    onBackClick = ::finish,
                    onShowAliasChanged = { enabled ->
                        Settings.setEnableShowAlias(enabled)
                        settingsState = settingsState.copy(showAlias = enabled)
                    },
                    onAliasSearchChanged = { enabled ->
                        Settings.setEnableAliasSearch(enabled)
                        settingsState = settingsState.copy(aliasSearch = enabled)
                    },
                    onCharterSearchChanged = { enabled ->
                        Settings.setEnableCharterSearch(enabled)
                        settingsState = settingsState.copy(charterSearch = enabled)
                    },
                    onUseDivingFishNicknameChanged = { enabled ->
                        Settings.setEnableDivingFishNickname(enabled)
                        settingsState = settingsState.copy(useDivingFishNickname = enabled)
                    },
                    onNicknameSaved = { nickname ->
                        Settings.setNickname(nickname)
                        settingsState = settingsState.copy(nickname = nickname)
                    },
                    onDifficultiesSaved = { selected ->
                        Settings.setUpdateDifficulty(selected)
                        settingsState = settingsState.copy(selectedDifficulties = selected)
                    },
                    onOpenUrl = ::openUrl
                )
            }
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }
}
