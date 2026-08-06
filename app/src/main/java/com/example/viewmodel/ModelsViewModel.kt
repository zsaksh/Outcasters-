package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModelsViewModel(private val modelRepository: ModelRepository) : ViewModel() {

    private val _models = MutableStateFlow<List<String>>(emptyList())
    val models: StateFlow<List<String>> = _models

    init {
        // Load models
    }

    class Factory(private val modelRepository: ModelRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ModelsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ModelsViewModel(modelRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
