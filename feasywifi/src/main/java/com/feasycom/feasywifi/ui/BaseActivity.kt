package com.feasycom.feasywifi.ui

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    private lateinit var _binding: T

    protected val mBinding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = getViewBinding()
        setContentView(_binding.root)
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configuration = resources.configuration
        configuration.fontScale = 1.0f
        val newResources = createConfigurationContext(configuration).resources
        return newResources ?: super.getResources()
    }

    abstract fun getViewBinding(): T
}