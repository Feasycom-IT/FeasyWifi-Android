package com.feasycom.feasywifi.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.feasycom.feasywifi.logic.NetworkRepository

class SplashViewModel : ViewModel() {
    private val downloadSplashLiveData = MutableLiveData<String>()
    val splashLiveData = Transformations.switchMap(downloadSplashLiveData) {
        NetworkRepository.downloadSplash(it)
    }

    fun downloadSplash(app: String) {
        downloadSplashLiveData.value = app
    }
}