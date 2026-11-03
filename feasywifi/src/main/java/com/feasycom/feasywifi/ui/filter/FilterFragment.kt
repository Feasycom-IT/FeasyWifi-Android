package com.feasycom.feasywifi.ui.filter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentFilterBinding
import com.feasycom.feasywifi.logic.utils.*
import com.feasycom.feasywifi.ui.BaseFragment

// 过滤
class FilterFragment : BaseFragment<FragmentFilterBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFilterBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    fun initView() {
        mBinding.header.backGroup.visibility = View.VISIBLE
        mBinding.header.titleText.text = getString(R.string.filter)

        val rssi = requireContext().getInt("rssiValue", -100)
        mBinding.filterRssiSb.progress = rssi + 100

        mBinding.filterRssiTv.text = getString(R.string.filter_rssi, rssi)

        mBinding.nameSwitch.isChecked = requireContext().getBoolean("nameSwitch", false)

        mBinding.nameEv.setText(requireContext().getStr("nameValue", ""))

    }

    fun initEvent() {

        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }

        mBinding.filterRssiSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                LogUtils.d(TAG, "FeasyWiFi APP onProgressChanged: progress -> $progress")
                mBinding.filterRssiTv.text = getString(R.string.filter_rssi, progress - 100)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    requireContext().putInt("rssiValue", it.progress - 100)
                }
            }
        })

        mBinding.nameSwitch.setOnCheckedChangeListener { _, isChecked ->
            requireContext().putBoolean("nameSwitch", isChecked)
        }

        mBinding.nameEv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                requireContext().putStr("nameValue", s.toString())
            }

        })
    }

    companion object {
        const val TAG: String = "FilterFragment"
    }
}