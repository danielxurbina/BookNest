package com.danielurbina.booknest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val refreshTrigger = MutableLiveData<Boolean>()

    fun requestRefresh() {
        refreshTrigger.value = true
    }
}