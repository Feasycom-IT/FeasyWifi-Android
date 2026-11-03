package com.feasycom.feasywifi.ui.agreement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentAgreementBinding
import com.feasycom.feasywifi.ui.BaseFragment

class AgreementFragment : BaseFragment<FragmentAgreementBinding>() {

    private val mViewModel: AgreementViewModel by lazy {
        ViewModelProviders.of(this)[AgreementViewModel::class.java]
    }
    private val args: AgreementFragmentArgs by navArgs()

    private val mAgreementType by lazy {
        args.agreementType
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentAgreementBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    fun initView() {
        mBinding.header.titleText.text = when (mAgreementType) {
            1 -> getString(R.string.userAgreement)
            else -> getString(R.string.privacyAgreement)
        }
        mBinding.header.backGroup.visibility = View.VISIBLE
    }

    fun initEvent() {
        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }


        mViewModel.agreementUrl.observe(this as LifecycleOwner) {
            LogUtils.d(TAG, "FeasyWiFi APP initEvent it.isSuccess -> ${it.isSuccess}")
            if (it.isSuccess) {
                it.getOrNull()?.let { it1 -> mBinding.webView.setUrl(it1) }
                LogUtils.d(TAG, "FeasyWiFi APP initEvent: $it")
            }
        }
        mViewModel.getAgreementUrl(mAgreementType)
    }

    companion object {
        const val TAG: String = "AgreementFragment"
    }
}