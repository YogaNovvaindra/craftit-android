package com.sevenexp.craftit.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sevenexp.craftit.domain.usecase.RegisterUseCase
import com.sevenexp.craftit.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    data class RegisterViewState(
        val resultRegister: ResultState<String> = ResultState.Idle()
    )

    private val _registerState = MutableStateFlow(RegisterViewState())
    val registerState = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        registerUseCase(name, email, password).onEach { result ->
            _registerState.value = _registerState.value.copy(resultRegister = result)
        }.launchIn(viewModelScope)
    }

    class Factory(
        private val registerUseCase: RegisterUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(registerUseCase) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}