package com.feasycom.feasywifi.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentAboutBinding
import com.feasycom.feasywifi.logic.utils.packageCode
import android.content.Intent
import android.net.Uri
import com.feasycom.utils.FileUtil

// 关于界面
class AboutFragment : BaseFragment<FragmentAboutBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentAboutBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.titleText.text = getString(R.string.about)
        mBinding.ver.text = requireContext().packageCode()
        mBinding.sdkVer.text = FileUtil.sdkVersion
        mBinding.header.rightTv.visibility = View.VISIBLE
        mBinding.header.rightTv.text = getString(R.string.feedback)
        mBinding.foot.aboutBtn.setTextColor(Color.parseColor("#18a0cd"))
    }

    private fun initEvent() {
        mBinding.foot.settingBtn.setOnClickListener {
            findNavController().navigate(R.id.setting)
        }
        mBinding.userAgreementTv.setOnClickListener {
            val action = AboutFragmentDirections.actionAboutFragmentToAgreementFragment(1)
            findNavController().navigate(action)

        }
        mBinding.privacyAgreementTv.setOnClickListener {
            val action = AboutFragmentDirections.actionAboutFragmentToAgreementFragment(2)
            findNavController().navigate(action)
        }
        mBinding.header.rightTv.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_feedbackFragment)
        }
        mBinding.more.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_aboutDetailsFragment)
        }
        mBinding.relativeLayout.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_qrFragment)
        }

        mBinding.telNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL);
            val data = Uri.parse("tel:${mBinding.telNumber.text}")
            intent.data = data
            startActivity(intent)
        }
    }
}

