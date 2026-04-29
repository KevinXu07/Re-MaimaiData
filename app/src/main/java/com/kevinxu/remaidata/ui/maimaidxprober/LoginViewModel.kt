package com.kevinxu.remaidata.ui.maimaidxprober

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.kevinxu.remaidata.R
import com.kevinxu.remaidata.model.ResponseErrorBody
import com.kevinxu.remaidata.network.MaimaiDataRequests
import com.kevinxu.remaidata.utils.SpUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val disposables = CompositeDisposable()

    private val _uiState = MutableStateFlow(
        LoginUiState(
            username = SpUtil.getUserName(),
            password = SpUtil.getPassword(),
            savedAccounts = SpUtil.getAccountHistory()
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun selectAccount(account: Pair<String, String>) {
        _uiState.update {
            it.copy(
                username = account.first,
                password = account.second
            )
        }
    }

    fun deleteAccount(username: String) {
        SpUtil.removeAccount(username)
        _uiState.update { current ->
            current.copy(savedAccounts = SpUtil.getAccountHistory())
        }
        _events.tryEmit(LoginEvent.ShowMessage("已删除账号：$username"))
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _events.tryEmit(LoginEvent.ShowMessageRes(R.string.type_in_account_pwd_hint))
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        val disposable = MaimaiDataRequests.login(state.username, state.password)
            .subscribe({ response ->
                _uiState.update { it.copy(isLoading = false) }

                if (response.code() == 200) {
                    val cookie = response.headers()["set-cookie"].orEmpty()
                    if (cookie.isNotBlank()) {
                        SpUtil.putLoginInfo(state.username, state.password, cookie)
                        _uiState.update { current ->
                            current.copy(savedAccounts = SpUtil.getAccountHistory())
                        }
                        _events.tryEmit(LoginEvent.NavigateToProber)
                    } else {
                        _events.tryEmit(LoginEvent.ShowMessageRes(R.string.login_failed))
                    }
                } else {
                    val errorString = response.errorBody()?.string()
                    val errorMessage = runCatching {
                        Gson().fromJson(errorString, ResponseErrorBody::class.java).message
                    }.getOrNull().orEmpty()
                    if (errorMessage.isBlank()) {
                        _events.tryEmit(LoginEvent.ShowMessageRes(R.string.login_failed))
                    } else {
                        _events.tryEmit(LoginEvent.ShowMessage(errorMessage))
                    }
                }
            }, { error ->
                _uiState.update { it.copy(isLoading = false) }
                error.printStackTrace()
                val message = error.message?.takeIf { it.isNotBlank() }
                if (message == null) {
                    _events.tryEmit(LoginEvent.ShowMessageRes(R.string.login_failed))
                } else {
                    _events.tryEmit(LoginEvent.ShowMessage(message))
                }
            })

        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val savedAccounts: List<Pair<String, String>> = emptyList()
)

sealed interface LoginEvent {
    data object NavigateToProber : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent
    data class ShowMessageRes(val resId: Int) : LoginEvent
}
