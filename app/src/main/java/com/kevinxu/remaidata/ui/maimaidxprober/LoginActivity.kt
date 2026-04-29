package com.kevinxu.remaidata.ui.maimaidxprober

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.ui.theme.MaimaiDataTheme
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LoginEvent.NavigateToProber -> {
                            startActivity(Intent(this@LoginActivity, ProberActivity::class.java))
                            finish()
                        }

                        is LoginEvent.ShowMessage -> {
                            Toast.makeText(this@LoginActivity, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        is LoginEvent.ShowMessageRes -> {
                            Toast.makeText(
                                this@LoginActivity,
                                getString(event.resId),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MaimaiDataTheme {
                LoginScreen(
                    uiState = uiState,
                    onBackClick = ::finish,
                    onUsernameChange = viewModel::updateUsername,
                    onPasswordChange = viewModel::updatePassword,
                    onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                    onLoginClick = viewModel::login,
                    onSelectAccount = viewModel::selectAccount,
                    onDeleteAccount = viewModel::deleteAccount
                )
            }
        }
    }
}
