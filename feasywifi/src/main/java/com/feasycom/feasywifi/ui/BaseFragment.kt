package com.feasycom.feasywifi.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private lateinit var _binding: T

    protected val mBinding get() = _binding
    private val TAG = "BaseFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        LogUtils.d(TAG, "FeasyWiFi APP onCreateView: " + this::class.java.simpleName)
        return _binding.root
    }

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): T


}