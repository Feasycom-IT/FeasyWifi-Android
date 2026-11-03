package com.feasycom.feasywifi.ui.splash

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.databinding.FragmentHomeBinding
import com.feasycom.feasywifi.logic.utils.putBoolean
import com.feasycom.feasywifi.logic.utils.putInt
import com.feasycom.feasywifi.logic.utils.putStr

// 闪屏界面
class SplashFragment : BaseFragment<FragmentHomeBinding>() {

    private val mViewModel: SplashViewModel by lazy {
        ViewModelProviders.of(this)[SplashViewModel::class.java]
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.toMain.text = getString(R.string.countdown, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                findNavController().navigate(R.id.action_homeFragment_to_setting)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    private fun clearFilter() {
        requireContext().putBoolean("nameSwitch", false)
        requireContext().putStr("nameValue", "")
        requireContext().putInt("rssiValue", -100)
    }

    private fun initView() {
        clearFilter()
        Glide.with(this)
            .load(R.drawable.loading)
            .into(mBinding.lanchImg)

        countDownTimer.start()
    }

    private fun initEvent() {
        mViewModel.downloadSplash("wifi")

        mViewModel.splashLiveData.observe(this as LifecycleOwner) {
            val orNull = it.getOrNull()
            if (orNull != null) {
                /*Glide.with(requireActivity())
                    .load(orNull)
                    .into(mBinding.lanchImg)*/
            }
        }
        mBinding.toMain.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_setting)
            countDownTimer.cancel()
        }
    }

}
