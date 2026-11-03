package com.feasycom.feasywifi.ui.feedback

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.feasycom.feasywifi.logic.NetworkRepository
import com.feasycom.feasywifi.logic.model.FeedbackParams

class FeedbackViewModel : ViewModel() {

    private val mFeedbackLiveData = MutableLiveData<FeedbackParams.Params>()


    val mFeedbackResponseBodyLiveData = Transformations.switchMap(mFeedbackLiveData) {
        NetworkRepository.feedback(it.content, it.feedbackType)
    }

    fun feedback(content: String, feedbackType: Int) {
        mFeedbackLiveData.value = FeedbackParams.Params(content, feedbackType)
    }
}