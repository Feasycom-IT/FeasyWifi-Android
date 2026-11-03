package com.feasycom.feasywifi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentAboutDetailsBinding

class AboutDetailsFragment : BaseFragment<FragmentAboutDetailsBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAboutDetailsBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    fun initView() {
        mBinding.header.titleText.text = getString(R.string.company_profile)
        mBinding.header.backGroup.visibility = View.VISIBLE
        mBinding.contentTv.text = getString(R.string.about_1)
    }

    fun initEvent() {
        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}