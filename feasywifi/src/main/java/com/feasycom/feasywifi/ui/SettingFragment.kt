package com.feasycom.feasywifi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentSettingBinding
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 从DeviceFragment.kt 跳转到 SettingFragment.kt界面
class SettingFragment : BaseFragment<FragmentSettingBinding>(), FeasyWiFiCallbacks {

    private val nav: SettingFragmentArgs by navArgs()

    private val mDeviceName by lazy {
        nav.deviceName
    }

    private val mDisconnect by lazy {
        nav.disconnect
    }

    companion object {
        const val TAG: String = "SettingFragment"
    }

    private lateinit var mMessageDialog: BaseDialog

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentSettingBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        if (mDisconnect) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    mMessageDialog = MessageDialog.Builder(requireContext())
                        .setMessage(getString(R.string.disconnet))
                        .setConfirm(getString(R.string.ok)).setCancel(null)
                        .setListener(object : MessageDialog.OnListener {
                            override fun onConfirm(dialog: BaseDialog?) {
                                findNavController().popBackStack()
                            }

                            override fun onCancel(dialog: BaseDialog?) {
                            }
                        }).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        initView()
        initEvent()
        BluetoothRepository.getDNS()
    }

    private fun initView() {
        mBinding.header.titleText.text = if (mDeviceName.length > 10) {
            "${mDeviceName.substring(0, 10)}..."
        } else {
            mDeviceName
        }
        mBinding.header.backGroup.visibility = View.VISIBLE
    }

    private fun initEvent() {
        mBinding.bluetoothCrv.setOnClickListener {// 蓝牙配网点击事件
            findNavController().navigate(R.id.action_settingFragment_to_bluetoothFragment)
        }
        mBinding.otaCrv.setOnClickListener {// 空中升级点击事件
            findNavController().navigate(R.id.action_settingFragment_to_otaFragment)
        }
        mBinding.gatewayCrv.setOnClickListener {// 网关配置点击事件
            findNavController().navigate(R.id.action_settingFragment_to_gatewayFragment)
        }
        mBinding.header.backImg.setOnClickListener {// 返回上一层图片点击事件
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {// 返回上一层文件点击事件
            findNavController().popBackStack()
        }
    }

    override fun blePeripheralDisconnected() {
        lifecycleScope.launch(Dispatchers.Main) {

            if (isVisible) {
                mMessageDialog = MessageDialog.Builder(requireContext())
                    .setMessage(getString(R.string.disconnet)).setConfirm(getString(R.string.ok))
                    .setCancel(null).setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            findNavController().popBackStack()
                        }

                        override fun onCancel(dialog: BaseDialog?) {
                        }
                    }).show()
            }

        }
    }

    override fun packetReceived(strValue: String?) {
        LogUtils.d(TAG, "FeasyWiFi APP packetReceived strValue => $strValue")
        BluetoothRepository.mGoneDNS = strValue?.contains("ERR") != true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.unRegisterViewCallback(this)
    }
}