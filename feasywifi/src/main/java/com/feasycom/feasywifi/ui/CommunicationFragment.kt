package com.feasycom.feasywifi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentCommunicationBinding

class CommunicationFragment : BaseFragment<FragmentCommunicationBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCommunicationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.titleText.text = getString(R.string.communication)
        // mBinding.foot.navigationBar.check(R.id.communication_rbt)
    }

    private fun initEvent() {
        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.foot.settingBtn.setOnClickListener {
            findNavController().navigate(R.id.setting)
        }
        mBinding.foot.aboutBtn.setOnClickListener {
            findNavController().navigate(R.id.about)
        }

        mBinding.mqttBtn.setOnClickListener {
            findNavController().navigate(R.id.action_communicationFragment_to_MQTTConfigFragment)
        }

    }

}
