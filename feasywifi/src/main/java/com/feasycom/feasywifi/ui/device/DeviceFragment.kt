package com.feasycom.feasywifi.ui.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentDeviceBinding
import com.feasycom.bean.BluetoothDeviceBwWrapper
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.logic.utils.getBoolean
import com.feasycom.feasywifi.logic.utils.putBoolean
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.FirstDialogFragment
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DeviceFragment : BaseFragment<FragmentDeviceBinding>(), FeasyWiFiCallbacks {

    private lateinit var mWaitDialog: LoadDialogFragment
    private lateinit var mMessageDialog: BaseDialog
    lateinit var mFirstDialogFragment: FirstDialogFragment

    private val mDevices = mutableListOf<BluetoothDeviceBwWrapper>()
    private lateinit var mDevice: BluetoothDeviceBwWrapper

    private val mDeviceAdapter: DeviceAdapter by lazy {
        DeviceAdapter(mDevices).apply {
            mOnItemClickListener = {
                BluetoothRepository.stopScan()
                if (BluetoothRepository.isBtEnabled()) {
                    mWaitDialog = LoadDialogFragment(getString(R.string.connecting))
                    mWaitDialog.show(requireActivity().supportFragmentManager, "connect")
                    mDevice = mDevices[it]
                    BluetoothRepository.connect(mDevice.address)
                } else {
                    mMessageDialog = MessageDialog.Builder(requireContext())
                        .setMessage(getString(R.string.open_bluetooth_tips)) // 确定按钮文本
                        .setConfirm(getString(R.string.ok)) // 设置点击按钮后不关闭对话框
                        .show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private var mHandler: Handler = Handler(Looper.myLooper() ?: Looper.getMainLooper()) {
        if (it.what == 0x01) {
            //    mDeviceAdapter.notifyDataSetChanged()
        }
        false
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentDeviceBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)// 注册FeasyWiFiCallbacks的回调
        if (BluetoothRepository.isConnect()) {
            BluetoothRepository.disconnect()
        }
        initView()
        initEvent()
        firstIn()// 判断是否第一次进入
    }

    private fun firstIn() {
        val data = requireContext().getBoolean("first", false)
        mFirstDialogFragment = FirstDialogFragment()
        LogUtils.e(TAG, "FeasyWiFi APP firstIn data -> $data  mFirstDialogFragment.isAdded -> ${mFirstDialogFragment.isAdded}")
        if (!mFirstDialogFragment.isAdded) {
            if (!data) {
                // 显示弹窗
                with(mFirstDialogFragment) {
                    onAgree = {
                        requireContext().putBoolean("first", true)
                        //initPermission()
                        mFirstDialogFragment.dismiss()
                    }
                    onRefuse = {
                        requireActivity().finish()
                    }
                    onUserAgreement = {// 用户协议
                        dismiss()
                        val action =
                            DeviceFragmentDirections.actionDeviceFragmentToAgreementFragment2(1)
                        findNavController().navigate(action)
                    }
                    onPrivacyAgreement = {// 隐私协议
                        dismiss()
                        val action =
                            DeviceFragmentDirections.actionDeviceFragmentToAgreementFragment2(2)
                        findNavController().navigate(action)
                    }
                    this@DeviceFragment.fragmentManager?.let { show(it, "first") }
                }
            } else {
                initPermission()
            }
        }
    }

    private fun initView() {
        mBinding.header.titleText.text = getString(R.string.setting)
        mBinding.deviceRecycler.layoutManager = LinearLayoutManager(context)
        mBinding.deviceRecycler.adapter = mDeviceAdapter
        mBinding.header.leftTv.visibility = View.VISIBLE
        mBinding.header.rightTv.visibility = View.VISIBLE
        mBinding.header.leftTv.text = getString(R.string.sort)
        mBinding.header.rightTv.text = getString(R.string.filter)
        mBinding.foot.settingBtn.setTextColor(Color.parseColor("#18A0CD"))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEvent() {
        mBinding.refreshableView.setOnRefreshListener {

            if (BluetoothRepository.isBtEnabled()) {
                if (mDevices.size < 1) {
                    BluetoothRepository.stopScan()
                }
                if (!BluetoothRepository.isScanner) {
                    mDevices.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    BluetoothRepository.stopScan()
                    mHandler.postDelayed(Runnable {
                        initPermission()
                    }, 300)
                } else {
                    Toast.makeText(context, getString(R.string.searching), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                // Android 12+ 需要运行时权限，Android 13+ 不能直接 enable()，
                // 统一走权限申请 + 系统开蓝牙弹窗
                initPermission()
            }

            it.closeHeaderOrFooter()

        }
        mBinding.foot.aboutBtn.setOnClickListener {// 关于点击事件
            findNavController().navigate(R.id.about)
        }
        mBinding.header.leftTv.setOnClickListener {// 排序点击事件
            mDevices.sortByDescending {
                it.rssi
            }
            mDeviceAdapter.notifyDataSetChanged()
        }

        mBinding.header.rightTv.setOnClickListener {// 筛选点击事件
            findNavController().navigate(R.id.action_deviceFragment_to_filterFragment)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(this).permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ).explainReasonBeforeRequest().onExplainRequestReason { scope, deniedList, _ ->
                scope.showRequestReasonDialog(
                    deniedList,
                    resources.getString(R.string.permission_prompt),
                    resources.getString(
                        R.string.ok
                    ),
                    resources.getString(R.string.close)
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.manually_enable_permission),
                    getString(R.string.open),
                    getString(R.string.cancel)
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    requireContext().putBoolean("isAllGranted", true)
                    if (!BluetoothRepository.isBtEnabled()) {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intent, 1)
                    }
                    mDevices.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    //mBinding.refreshableView.autoRefresh()
                    BluetoothRepository.startScan(0)
                } else {
                    requireContext().putBoolean("isAllGranted", false)
                }
            }
        } else {
            PermissionX.init(this).permissions(
                Manifest.permission.ACCESS_FINE_LOCATION
            ).explainReasonBeforeRequest().onExplainRequestReason { scope, deniedList, _ ->
                scope.showRequestReasonDialog(
                    deniedList,
                    resources.getString(R.string.permission_prompt),
                    resources.getString(
                        R.string.ok
                    ),
                    resources.getString(R.string.close)
                )

            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.manually_enable_permission),
                    getString(R.string.open),
                    getString(R.string.cancel)
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    requireContext().putBoolean("isAllGranted", true)
                    if (!BluetoothRepository.isBtEnabled()) {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intent, 1)
                    }
                    mDevices.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    //mBinding.refreshableView.autoRefresh() // 自动刷新
                    BluetoothRepository.startScan(0)
                } else {
                    requireContext().putBoolean("isAllGranted", false)
                }
            }
        }
    }

    override fun blePeripheralFound(
        device: BluetoothDeviceBwWrapper, record: ByteArray?
    ) {// 处理从BluetoothRepository.kt回调来的数据
        super.blePeripheralFound(device, record)
        try {
            if (mDevices.contains(device)) {
                /*   val index = mDevices.indexOf(device)
                   mDevices[index].let {
                       it.rssi = device.rssi
                       it.name = device.name
                       it.locationName = device.locationName
                   }
                   mDeviceAdapter.notifyItemChanged(index, "1")*/
            } else {
                mDevices.add(device)
                if (mDevices.size > 0) {
                    mDeviceAdapter.notifyItemInserted(mDevices.size)
                }
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "FeasyWiFi APP ========blePeripheralFound======== ${e.message}")
        }
    }

    override fun blePeripheralConnected() {// 接收从BluetoothRepository.kt回调成功的消息，并更新相应的UI
        lifecycleScope.launch(Dispatchers.Main) {//使用协程在主线程更新UI和跳转到其他界面
            mWaitDialog.dismiss()
            val action =
                DeviceFragmentDirections.actionDeviceFragmentToSettingFragment(mDevice.name)// 跳转到SettingFragment.kt
            findNavController().navigate(action)
        }
    }

    override fun blePeripheralDisconnected() {
        super.blePeripheralDisconnected()
        lifecycleScope.launch(Dispatchers.Main) {
            if (::mWaitDialog.isInitialized && mWaitDialog.isVisible) {
                mWaitDialog.dismiss()
                mMessageDialog = MessageDialog.Builder(requireContext())
                    .setMessage(getString(R.string.connection_failed)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置点击按钮后不关闭对话框
                    .show()
            }
        }
    }

    override fun onConnectTimeout() {// 30秒钟连接超时的处理
        super.onConnectTimeout()
        if (::mWaitDialog.isInitialized && mWaitDialog.isVisible) {
            mWaitDialog.dismiss()
            mMessageDialog = MessageDialog.Builder(requireContext())
                .setMessage(getString(R.string.connection_time_out)) // 确定按钮文本
                .setConfirm(getString(R.string.ok)) // 设置点击按钮后不关闭对话框
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.stopScan()
        BluetoothRepository.unRegisterViewCallback(this)
    }

    companion object {
        const val TAG: String = "DeviceFragment"
    }

}