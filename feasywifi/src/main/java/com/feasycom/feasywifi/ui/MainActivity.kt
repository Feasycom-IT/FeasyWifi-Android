package com.feasycom.feasywifi.ui

import com.feasycom.feasywifi.databinding.ActivityMainBinding

/**
 * TODO 程序入口界面
 * ViewBinding是Android的Jetpack的一部分，这个框架可以代替findViewById，这个框架会为每个xml布局生成一个绑定类
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

}

