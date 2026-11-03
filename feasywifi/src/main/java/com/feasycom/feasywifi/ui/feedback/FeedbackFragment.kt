package com.feasycom.feasywifi.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentFeedbackBinding
import com.feasycom.feasywifi.logic.NetworkRepository.FEEDBACK_ADVICE
import com.feasycom.feasywifi.logic.NetworkRepository.FEEDBACK_BUG
import com.feasycom.feasywifi.logic.NetworkRepository.FEEDBACK_COOPERATION
import com.feasycom.feasywifi.logic.NetworkRepository.FEEDBACK_UI
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>() {

    // private var mPreferenceKey: String = "FEEDBACK_ADVICE"

    private lateinit var mMessageDialog: BaseDialog

    private lateinit var mWaitDialog: LoadDialogFragment

    private val mViewModel by lazy {
        ViewModelProviders.of(this)[FeedbackViewModel::class.java]
    }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFeedbackBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initEvent()
    }

    fun initView() {
        mBinding.header.titleText.text = getString(R.string.feedback)
        mBinding.header.backGroup.visibility = View.VISIBLE
        mBinding.header.backTv.text = getString(R.string.about)
    }

    fun initEvent() {

        mBinding.submit.setOnClickListener {
            if (mBinding.dataEv.text.toString().isNotBlank()) {
                mWaitDialog = LoadDialogFragment(getString(R.string.feedback_status))
                mWaitDialog.show(requireActivity().supportFragmentManager, "feedback")

                mViewModel.feedback(
                    mBinding.dataEv.text.toString(),
                    when (mBinding.radioGroup.checkedRadioButtonId) {
                        R.id.proposal -> FEEDBACK_ADVICE
                        R.id.abnormal_function -> FEEDBACK_BUG
                        R.id.interface_abnormality -> FEEDBACK_UI
                        R.id.cooperation -> FEEDBACK_COOPERATION
                        else -> FEEDBACK_ADVICE
                    }
                )
            }
        }


        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }

        mViewModel.mFeedbackResponseBodyLiveData.observe(this as LifecycleOwner) {
            mWaitDialog.dismiss()
            mMessageDialog = MessageDialog.Builder(requireContext())
                // 内容必须要填写
                .setMessage(
                    if (it.isSuccess) {
                        mBinding.dataEv.text = null //反馈提交成功后，清除掉里面的内容
                        R.string.feedback_success

                    } else {
                        R.string.feedback_failure
                    }
                ) // 确定按钮文本
                .setConfirm(R.string.ok)
                .setCancel(null)
                .show()
        }
    }
}