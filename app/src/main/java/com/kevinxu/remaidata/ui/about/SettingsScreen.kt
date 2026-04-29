package com.kevinxu.remaidata.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.kevinxu.remaidata.R

private enum class SettingsDestination {
    SETTINGS,
    ABOUT
}

data class SettingsUiState(
    val showAlias: Boolean,
    val aliasSearch: Boolean,
    val charterSearch: Boolean,
    val useDivingFishNickname: Boolean,
    val nickname: String,
    val selectedDifficulties: Set<Int>
)

data class AboutUiState(
    val versionName: String,
    val dataVersion: String,
    val chartStatsUpdateTime: String,
    val projectUrl: String,
    val feedbackUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsState: SettingsUiState,
    aboutState: AboutUiState,
    onBackClick: () -> Unit,
    onShowAliasChanged: (Boolean) -> Unit,
    onAliasSearchChanged: (Boolean) -> Unit,
    onCharterSearchChanged: (Boolean) -> Unit,
    onUseDivingFishNicknameChanged: (Boolean) -> Unit,
    onNicknameSaved: (String) -> Unit,
    onDifficultiesSaved: (Set<Int>) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var destination by rememberSaveable { mutableStateOf(SettingsDestination.SETTINGS) }
    var showNicknameDialog by rememberSaveable { mutableStateOf(false) }
    var showDifficultyDialog by rememberSaveable { mutableStateOf(false) }

    val title = when (destination) {
        SettingsDestination.SETTINGS -> stringResource(R.string.settings)
        SettingsDestination.ABOUT -> stringResource(R.string.about)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            if (destination == SettingsDestination.ABOUT) {
                                destination = SettingsDestination.SETTINGS
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.left_arrow))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (destination) {
            SettingsDestination.SETTINGS -> {
                SettingsContent(
                    state = settingsState,
                    contentPadding = innerPadding,
                    onShowAliasChanged = onShowAliasChanged,
                    onAliasSearchChanged = onAliasSearchChanged,
                    onCharterSearchChanged = onCharterSearchChanged,
                    onUseDivingFishNicknameChanged = onUseDivingFishNicknameChanged,
                    onNicknameClick = { showNicknameDialog = true },
                    onDifficultyClick = { showDifficultyDialog = true },
                    onAboutClick = { destination = SettingsDestination.ABOUT }
                )
            }

            SettingsDestination.ABOUT -> {
                AboutContent(
                    state = aboutState,
                    contentPadding = innerPadding,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }

    if (showNicknameDialog) {
        NicknameDialog(
            initialValue = settingsState.nickname,
            onDismiss = { showNicknameDialog = false },
            onConfirm = { nickname ->
                showNicknameDialog = false
                onNicknameSaved(nickname)
            }
        )
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            selectedDifficulties = settingsState.selectedDifficulties,
            onDismiss = { showDifficultyDialog = false },
            onConfirm = { selected ->
                showDifficultyDialog = false
                onDifficultiesSaved(selected)
            }
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    contentPadding: PaddingValues,
    onShowAliasChanged: (Boolean) -> Unit,
    onAliasSearchChanged: (Boolean) -> Unit,
    onCharterSearchChanged: (Boolean) -> Unit,
    onUseDivingFishNicknameChanged: (Boolean) -> Unit,
    onNicknameClick: () -> Unit,
    onDifficultyClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        )
    ) {
        item {
            SettingSwitchItem(
                title = stringResource(R.string.settings_enable_show_alias),
                checked = state.showAlias,
                onCheckedChange = onShowAliasChanged
            )
        }
        item { HorizontalDivider() }
        item {
            SettingSwitchItem(
                title = stringResource(R.string.settings_enable_alias_search),
                checked = state.aliasSearch,
                onCheckedChange = onAliasSearchChanged
            )
        }
        item { HorizontalDivider() }
        item {
            SettingSwitchItem(
                title = stringResource(R.string.settings_enable_charter_search),
                checked = state.charterSearch,
                onCheckedChange = onCharterSearchChanged
            )
        }
        item { HorizontalDivider() }
        item {
            SettingSwitchItem(
                title = stringResource(R.string.settings_use_diving_fish_nickname),
                checked = state.useDivingFishNickname,
                onCheckedChange = onUseDivingFishNicknameChanged
            )
        }
        item { HorizontalDivider() }
        item {
            SettingActionItem(
                title = stringResource(R.string.settings_nickname),
                summary = state.nickname.ifBlank { "-" },
                onClick = onNicknameClick
            )
        }
        item { HorizontalDivider() }
        item {
            SettingActionItem(
                title = stringResource(R.string.settings_update_difficulty),
                summary = formatDifficultySummary(state.selectedDifficulties),
                onClick = onDifficultyClick
            )
        }
        item { HorizontalDivider() }
        item {
            SettingActionItem(
                title = stringResource(R.string.about),
                summary = null,
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun AboutContent(
    state: AboutUiState,
    contentPadding: PaddingValues,
    onOpenUrl: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        )
    ) {
        item {
            AboutItem(
                title = stringResource(R.string.app_name),
                summary = state.versionName,
                clickable = false,
                onClick = {}
            )
        }
        item { HorizontalDivider() }
        item {
            AboutItem(
                title = stringResource(R.string.local_data_version),
                summary = state.dataVersion,
                clickable = false,
                onClick = {}
            )
        }
        item { HorizontalDivider() }
        item {
            AboutItem(
                title = stringResource(R.string.fit_diff_update_date),
                summary = state.chartStatsUpdateTime,
                clickable = false,
                onClick = {}
            )
        }
        item { HorizontalDivider() }
        item {
            AboutItem(
                title = stringResource(R.string.project_url),
                summary = state.projectUrl,
                clickable = true,
                onClick = { onOpenUrl(state.projectUrl) }
            )
        }
        item { HorizontalDivider() }
        item {
            AboutItem(
                title = stringResource(R.string.feedback),
                summary = state.feedbackUrl,
                clickable = true,
                onClick = { onOpenUrl(state.feedbackUrl) }
            )
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun SettingActionItem(
    title: String,
    summary: String?,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = summary?.let {
            {
                Text(
                    text = it,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun AboutItem(
    title: String,
    summary: String,
    clickable: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = {
            Text(
                text = summary,
                color = if (clickable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier
    )
}

@Composable
private fun NicknameDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nickname by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_nickname)) },
        text = {
            Column {
                Text(text = stringResource(R.string.settings_nickname_dialog_title))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nickname) }) {
                Text(text = stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun DifficultyDialog(
    selectedDifficulties: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    val options = stringArrayResource(R.array.difficulty_options)
    val selected = remember(selectedDifficulties) {
        mutableStateListOf<Int>().apply {
            addAll(selectedDifficulties.sorted())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_difficulty_pick)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = selected.contains(index),
                                onValueChange = { checked ->
                                    if (checked) {
                                        if (!selected.contains(index)) {
                                            selected.add(index)
                                        }
                                    } else {
                                        selected.remove(index)
                                    }
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected.contains(index),
                            onCheckedChange = null
                        )
                        Text(text = option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selected.toSet())
                }
            ) {
                Text(text = stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

@Composable
private fun formatDifficultySummary(selected: Set<Int>): String {
    val options = stringArrayResource(R.array.difficulty_options)
    return selected.sorted().mapNotNull(options::getOrNull).joinToString(", ")
}
