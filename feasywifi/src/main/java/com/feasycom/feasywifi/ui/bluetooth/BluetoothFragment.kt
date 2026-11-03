package com.feasycom.feasywifi.ui.bluetooth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentBluetoothBinding
import com.feasycom.feasywifi.logic.AtCommand
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.logic.utils.getStr
import com.feasycom.feasywifi.logic.utils.putStr
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 上次配网使用的 WiFi 密码（本地保存，下次配网自动回填）
private const val KEY_LAST_WIFI_PASSWORD = "last_wifi_password"

// 蓝牙配网界面
class BluetoothFragment : BaseFragment<FragmentBluetoothBinding>(), FeasyWiFiCallbacks {

    private val mViewModel by lazy {
        ViewModelProviders.of(this)[BluetoothViewModel::class.java]
    }

    private lateinit var mWaitDialog: LoadDialogFragment

    private lateinit var mMessageDialog: BaseDialog

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentBluetoothBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.titleText.text = getString(R.string.title_bluetooth)
        mBinding.header.backGroup.visibility = View.VISIBLE
        // 回填上次配网输入的 WiFi 密码，避免每次重复输入
        val lastPassword = requireContext().getStr(KEY_LAST_WIFI_PASSWORD)
        if (lastPassword.isNotEmpty()) {
            mBinding.passwordEv.setText(lastPassword)
            mBinding.passwordEv.setSelection(lastPassword.length)
        }
    }

    private fun initEvent() {
        mViewModel.registerReceiver()// 注册网络发生改变的广播
        mBinding.header.backImg.setOnClickListener {// 图片返回点击事件
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {// 文件返回点击事件
            findNavController().popBackStack()
        }

        mBinding.dhcpSwitch.setOnCheckedChangeListener { _, isChecked ->
            BluetoothRepository.setDhcp(isChecked)
            LogUtils.d(TAG, "FeasyWiFi APP initEvent isChecked -> $isChecked")
            if (isChecked) {
                mBinding.dhcpGroup.visibility = View.GONE
                mBinding.dnsGroup.visibility = View.GONE
            } else {
                mBinding.dhcpGroup.visibility = View.VISIBLE
                LogUtils.e(TAG, "FeasyWiFi APP initEvent BluetoothRepository.mGoneNDS -> ${BluetoothRepository.mGoneDNS}")
                if (BluetoothRepository.mGoneDNS) {
                    mBinding.dnsGroup.visibility = View.VISIBLE
                }
            }
        }

        mBinding.configNetwork.setOnClickListener {// 开始配网

            if (mBinding.ssidEv.text.isBlank()/* && password.text.isNotBlank()*/) {

                mMessageDialog = MessageDialog.Builder(requireContext())
                    // 内容必须要填写
                    .setMessage(getString(R.string.ssid_empty)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                    .setCancel(null) // 设置点击按钮后不关闭对话框
                    .show()
                return@setOnClickListener
            }
            // 记住本次输入的密码，下次配网自动回填
            requireContext().putStr(KEY_LAST_WIFI_PASSWORD, mBinding.passwordEv.text.toString())

            lifecycleScope.launch(Dispatchers.IO) {
                if (!mBinding.dhcpSwitch.isChecked) {
                    if (mBinding.ipEv.text.toString().contains(IP_REGEX)) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mWaitDialog = LoadDialogFragment(getString(R.string.configure_network))
                            mWaitDialog.show(requireActivity().supportFragmentManager, "config")
                        }
                        BluetoothRepository.setIp(mBinding.ipEv.text.toString())
                        delay(200)
                        if (mBinding.gwEv.text.toString().contains(IP_REGEX)) {
                            BluetoothRepository.setGw(mBinding.gwEv.text.toString())
                            delay(200)
                        }
                        if (mBinding.maskEv.text.toString().contains(IP_REGEX)) {
                            BluetoothRepository.setMask(mBinding.maskEv.text.toString())
                            delay(200)
                        }
                        if (mBinding.dnsEv.text.toString().contains(IP_REGEX)) {
                            BluetoothRepository.setDns(mBinding.dnsEv.text.toString())
                            delay(200)
                        }
                        BluetoothRepository.setNetwork(
                            mBinding.ssidEv.text.toString(), mBinding.passwordEv.text.toString()
                        )
                    } else {
                        withContext(Dispatchers.Main) {
                            // 静态IP错误
                            mMessageDialog = MessageDialog.Builder(requireContext())
                                // 内容必须要填写
                                .setMessage(R.string.ip_error) // 确定按钮文本
                                .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                                .setCancel(null) // 设置点击按钮后不关闭对话框
                                .show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        mWaitDialog = LoadDialogFragment(getString(R.string.configure_network))
                        mWaitDialog.show(requireActivity().supportFragmentManager, "config")
                    }
                    BluetoothRepository.setNetwork(
                        mBinding.ssidEv.text.toString(), mBinding.passwordEv.text.toString()
                    )
                }
            }
        }
        BluetoothRepository.getDhcp()
        mViewModel.mBroadcastData.observe(this as LifecycleOwner) {
            mBinding.ssidEv.setText(it)
        }
    }

    private fun showDialog(isSuccess: Boolean, ip: String) {
        lifecycleScope.launch {
            if (::mWaitDialog.isInitialized) {
                mWaitDialog.dismiss()
            }

            mMessageDialog = (if (isSuccess) {
                MessageDialog.Builder(requireContext())
                    // 标题可以不用填写
                    .setTitle(getString(R.string.configure_result_success))
                    // 内容必须要填写
                    .setMessage(getString(R.string.configure_result_success_item, ip)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                    .setCancel(null) // 设置点击按钮后不关闭对话框
                //.setAutoDismiss(false)n

            } else {
                MessageDialog.Builder(requireContext())
                    // 内容必须要填写
                    .setMessage(getString(R.string.configure_result_failed)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                    .setCancel(null) // 设置点击按钮后不关闭对话框
            }).show()
        }
    }

    companion object {
        private const val TAG = "BluetoothFragment"
        val IP_REGEX =
            "^([1-9]\\d?|1\\d{2}|2[0-4]\\d|25[0-5])(\\.([1-9]?\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}\$".toRegex()
    }

    override fun success(ip: String) {
        LogUtils.d(TAG, "FeasyWiFi APP success: 配网成功 IP地址 => $ip")
        showDialog(true,ip)
    }

    override fun failure() {
        LogUtils.e(TAG, "FeasyWiFi APP failure: 配网失败")
        showDialog(false,"")
    }

    override fun blePeripheralConnected() {

    }

    override fun blePeripheralDisconnected() {
        // TODO: 断开连接
        lifecycleScope.launch(Dispatchers.Main) {

            if (::mMessageDialog.isInitialized && mMessageDialog.isShowing) {
                mMessageDialog.dismiss()
            }

            if (::mWaitDialog.isInitialized && mWaitDialog.isVisible) {
                mWaitDialog.dismiss()
                mMessageDialog = MessageDialog.Builder(requireContext())
                    // 内容必须要填写
                    .setMessage(getString(R.string.configure_result_failed)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                    .setCancel(null) // 设置点击按钮后不关闭对话框
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            findNavController().navigate(
                                BluetoothFragmentDirections.actionBluetoothFragmentToSettingFragment(
                                    true
                                )
                            )
                        }

                        override fun onCancel(dialog: BaseDialog?) {
                        }
                    }).show()
                return@launch
            }

            findNavController().navigate(
                BluetoothFragmentDirections.actionBluetoothFragmentToSettingFragment(
                    true
                )
            )
        }
    }

    override fun packetReceived(strValue: String?) {
        Log.e(TAG, "packetReceived: $strValue")
        strValue?.let {
            when {
                it.contains("+DHCP") -> {
                    val s = it.split("=").toTypedArray()[1].split("\r\n").toTypedArray()[0]

                    lifecycleScope.launch(Dispatchers.Main) {
                        mBinding.dhcpSwitch.isChecked = s == "1"
                        if (s == "0") {
                            mBinding.dhcpGroup.visibility = View.VISIBLE
                        } else {
                            mBinding.dhcpGroup.visibility = View.GONE
                        }
                    }
                }
                it.contains("ERR") -> {
                    when (BluetoothRepository.lostAtCommand) {
                        AtCommand.GET_DHCP -> {
                            Log.e(TAG, "packetReceived: 获取DHCP 失败")
                        }
                        AtCommand.SET_DHCP -> {
                            Log.e(TAG, "packetReceived: 设置DHCP 失败")
                        }
                        AtCommand.GET_DNS -> {
                            Log.e(TAG, "packetReceived: 获取DNS 失败")
                        }
                        AtCommand.SET_DNS -> {
                            Log.e(TAG, "packetReceived: 设置DNS 失败")
                        }
                        AtCommand.GET_IP -> {
                            Log.e(TAG, "packetReceived: 获取IP 失败")
                        }
                        AtCommand.SET_IP -> {
                            Log.e(TAG, "packetReceived: 设置IP 失败")
                        }
                        AtCommand.SET_GW -> {
                            Log.e(TAG, "packetReceived: 设置GW 失败")
                        }
                        AtCommand.SET_MASK -> {
                            Log.e(TAG, "packetReceived: 设置MASK 失败")
                        }
                        AtCommand.SET_NETWORK -> {
                            Log.e(TAG, "packetReceived: 设置网络 失败")
                        }
                        else -> {

                        }
                    }
                }
                it.contains("OK") -> {
                    when (BluetoothRepository.lostAtCommand) {
                        AtCommand.GET_DHCP -> {
                            Log.e(TAG, "packetReceived: 获取DHCP 成功")
                        }
                        AtCommand.SET_DHCP -> {
                            Log.e(TAG, "packetReceived: 设置DHCP 成功")
                        }
                        AtCommand.GET_DNS -> {
                            Log.e(TAG, "packetReceived: 获取DNS 成功")
                        }
                        AtCommand.SET_DNS -> {
                            Log.e(TAG, "packetReceived: 设置DNS 成功")
                        }
                        AtCommand.GET_IP -> {
                            Log.e(TAG, "packetReceived: 获取IP 成功")
                        }
                        AtCommand.SET_IP -> {
                            Log.e(TAG, "packetReceived: 设置IP 成功")
                        }
                        AtCommand.SET_GW -> {
                            Log.e(TAG, "packetReceived: 设置GW 成功")
                        }
                        AtCommand.SET_MASK -> {
                            Log.e(TAG, "packetReceived: 设置MASK 成功")
                        }
                        AtCommand.SET_NETWORK -> {
                            Log.e(TAG, "packetReceived: 设置网络 成功")
                        }
                        else -> {

                        }
                    }
                }
                else -> {
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.unregisterReceiver()
        BluetoothRepository.unRegisterViewCallback(this)
    }
}
