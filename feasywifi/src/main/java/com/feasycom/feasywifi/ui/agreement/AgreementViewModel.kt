package com.feasycom.feasywifi.ui.agreement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.logic.NetworkRepository

class AgreementViewModel : ViewModel() {

    private val _agreementType = MutableLiveData<Int>()

    val agreementUrl = Transformations.switchMap(_agreementType) {
        LogUtils.d(TAG, "FeasyWiFi APP agreementUrl -> $it")
        NetworkRepository.getAgreementUrl(it)
    }

    fun getAgreementUrl(type: Int) {
        _agreementType.postValue(type)
    }

    companion object {
        const val TAG: String = "AgreementViewModel"
    }

}