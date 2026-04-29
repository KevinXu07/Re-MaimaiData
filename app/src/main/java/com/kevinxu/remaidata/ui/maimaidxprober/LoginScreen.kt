package com.kevinxu.remaidata.ui.maimaidxprober

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.ui.theme.MaimaiDataTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onBackClick: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onSelectAccount: (Pair<String, String>) -> Unit,
    onDeleteAccount: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val proberUrl = stringResource(R.string.prober_url)
    val focusManager = LocalFocusManager.current
    var showAccountSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.login)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = stringResource(R.string.left_arrow))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.username)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                painter = painterResource(
                                    id = if (uiState.isPasswordVisible) {
                                        R.drawable.eye_closed
                                    } else {
                                        R.drawable.eye_open
                                    }
                                ),
                                contentDescription = stringResource(R.string.password)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                FilledTonalButton(
                    onClick = {
                        focusManager.clearFocus()
                        showAccountSheet = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(text = stringResource(R.string.accountManagement))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(text = stringResource(R.string.login))
                }

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.prober_tips),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectionContainer {
                        Text(
                            text = proberUrl,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                uriHandler.openUri(proberUrl)
                            }
                        )
                    }
                }
            }
        }

        if (showAccountSheet) {
            AccountSheet(
                accounts = uiState.savedAccounts,
                onDismiss = { showAccountSheet = false },
                onSelectAccount = { account ->
                    showAccountSheet = false
                    onSelectAccount(account)
                },
                onDeleteAccount = { username ->
                    showAccountSheet = false
                    onDeleteAccount(username)
                },
                focusManager = focusManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSheet(
    accounts: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelectAccount: (Pair<String, String>) -> Unit,
    onDeleteAccount: (String) -> Unit,
    focusManager: FocusManager
) {
    var deleteMode by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        id = if (deleteMode) {
                            R.string.delete_account
                        } else {
                            R.string.select_account
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                FilledTonalButton(
                    onClick = { deleteMode = !deleteMode },
                    enabled = accounts.isNotEmpty()
                ) {
                    Text(
                        text = stringResource(
                            id = if (deleteMode) {
                                R.string.select_account
                            } else {
                                R.string.delete_account
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.saved_accounts_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(
                        items = accounts,
                        key = { it.first }
                    ) { account ->
                        ListItem(
                            headlineContent = { Text(text = account.first) },
                            supportingContent = {
                                Text(
                                    text = if (deleteMode) {
                                        stringResource(R.string.delete_account)
                                    } else {
                                        stringResource(R.string.select_account)
                                    }
                                )
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = account.first.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                if (deleteMode) {
                                    onDeleteAccount(account.first)
                                } else {
                                    onSelectAccount(account)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaimaiDataTheme {
        LoginScreen(
            uiState = LoginUiState(
                username = "demo",
                password = "password",
                savedAccounts = listOf("demo" to "password")
            ),
            onBackClick = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onSelectAccount = {},
            onDeleteAccount = {}
        )
    }
}
