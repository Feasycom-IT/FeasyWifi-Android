package com.feasycom.feasywifi.ui.ota

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentOtaBinding
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.logic.utils.getStr
import com.feasycom.feasywifi.logic.utils.putStr
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 上次升级使用的 DFU 名称（本地保存，下次进入自动回填）
private const val KEY_LAST_OTA_FILE = "last_ota_file"

// 空中升级
class OtaFragment : BaseFragment<FragmentOtaBinding>(), FeasyWiFiCallbacks {

    private lateinit var mWaitDialog: LoadDialogFragment

    private lateinit var mMessageDialog: BaseDialog

    private val mHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOtaBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.titleText.text = getString(R.string.title_ota)
        mBinding.header.backGroup.visibility = View.VISIBLE
        // 回填上次升级输入的 DFU 名称，避免每次重复输入
        val lastOtaFile = requireContext().getStr(KEY_LAST_OTA_FILE)
        if (lastOtaFile.isNotEmpty()) {
            mBinding.ota.setText(lastOtaFile)
            mBinding.ota.setSelection(lastOtaFile.length)
        }
    }

    private fun initEvent() {

        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }

        mBinding.queryIp.setOnClickListener {// 查询IP
            clearFocus()
            BluetoothRepository.getIp()

            mHandler.postDelayed(Runnable {
                BluetoothRepository.getNetName()
            },1000)

        }
        mBinding.queryVer.setOnClickListener {// 查询版本
            clearFocus()
            BluetoothRepository.getVersion()

        }

        mBinding.upgrade.setOnClickListener {// 升级
            if (mBinding.ota.text.toString().isNotBlank()) {
                clearFocus()
                // 记住本次使用的 DFU 名称，下次进入自动回填
                requireContext().putStr(KEY_LAST_OTA_FILE, mBinding.ota.text.toString())
                BluetoothRepository.startOTA(mBinding.ota.text.toString())
            }
        }

        mBinding.reset.setOnClickListener {// 恢复出厂设置
            clearFocus()
            mMessageDialog = MessageDialog.Builder(requireContext())
                .setMessage(R.string.reset_tips) // 确定按钮文本
                .setConfirm(R.string.pin_cancel) // 设置 null 表示不显示取消按钮
                .setCancel(R.string.reset) // 设置点击按钮后不关闭对话框
                .setListener(
                    object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {

                        }

                        override fun onCancel(dialog: BaseDialog?) {
                            // findNavController().popBackStack()
                            mWaitDialog = LoadDialogFragment(getString(R.string.reset_wait))
                            mWaitDialog.show(requireActivity().supportFragmentManager, "reset")
                            BluetoothRepository.reset()
                        }
                    })
                .show()
        }

        /*mBinding.ota.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()){
                    mBinding.ota.gravity = Gravity.LEFT
                }else {
                    mBinding.ota.gravity = Gravity.CENTER
                }
            }
        })*/
    }

    private fun clearFocus() {
        mBinding.ota.clearFocus()
        mBinding.ip.clearFocus()
        mBinding.ver.clearFocus()
        hideKeyboard(mBinding.ota)
        hideKeyboard(mBinding.ip)
        hideKeyboard(mBinding.ver)
    }


    private fun hideKeyboard(v: View) {
        val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
        }
    }


    override fun blePeripheralDisconnected() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (::mMessageDialog.isInitialized && mMessageDialog.isShowing) {
                mMessageDialog.dismiss()
            }
            if (::mWaitDialog.isInitialized && mWaitDialog.isVisible) {
                mWaitDialog.dismiss()
                if (mWaitDialog.tag != "reset") {
                    /*mMessageDialog = MessageDialog.Builder(requireContext())
                        .setMessage(R.string.update_fail)
                        .setConfirm(R.string.ok)
                        .setCancel(null)
                        .setListener(
                            object : MessageDialog.OnListener{
                                override fun onConfirm(dialog: BaseDialog?) {
                                    findNavController().navigate(OtaFragmentDirections.actionOtaFragmentToSettingFragment(true))
                                }
                                override fun onCancel(dialog: BaseDialog?) {
                                }
                            })
                        .show()*/
                    findNavController().navigate(
                        OtaFragmentDirections.actionOtaFragmentToSettingFragment(
                            true
                        )
                    )
                    return@launch
                }
            }
            findNavController().navigate(
                OtaFragmentDirections.actionOtaFragmentToSettingFragment(
                    true
                )
            )
        }
    }



    override fun ipInformation(ip: String?) {
        super.ipInformation(ip)
        lifecycleScope.launch(Dispatchers.Main) {
            mBinding.ip.text = ip
        }
    }

    override fun NetInformation(apName: String?) {
        super.NetInformation(apName)
        lifecycleScope.launch(Dispatchers.Main) {
            mBinding.apName.text = apName
        }

    }

    override fun versionInformation(version: String?) {
        super.versionInformation(version)
        try {
            lifecycleScope.launch(Dispatchers.Main) {
                mBinding.ver.text = version
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onOtaSuccess() {
        Log.e(TAG, "onOtaSuccess: 升级成功")
        lifecycleScope.launch(Dispatchers.Main) {
            mWaitDialog.dismiss()

            mMessageDialog = MessageDialog.Builder(requireContext())
                // 内容必须要填写
                .setMessage(R.string.update_success) // 确定按钮文本
                .setConfirm(R.string.ok) // 设置 null 表示不显示取消按钮
                .setCancel(null) // 设置点击按钮后不关闭对话框
                .show()
        }
    }

    override fun onOtaFailure() {
        super.onOtaFailure()
        Log.e(TAG, "onOtaFailure:")
        lifecycleScope.launch(Dispatchers.Main) {
            mWaitDialog.dismiss()
            mMessageDialog = MessageDialog.Builder(requireContext())
                // 内容必须要填写
                .setMessage(R.string.update_fail) // 确定按钮文本
                .setConfirm(R.string.ok) // 设置 null 表示不显示取消按钮
                .setCancel(null) // 设置点击按钮后不关闭对话框
                .show()
        }
    }

    override fun onOtaProgress(progress: String?) {
        Log.e(TAG, "onOtaProgress progress -> $progress")
        lifecycleScope.launch(Dispatchers.Main) {
            mWaitDialog.mMessage = "${getString(R.string.upgrade_message)}  ( ${progress}% )"
        }
    }

    override fun onOtaNetworkIsNotConfigured() {
        lifecycleScope.launch(Dispatchers.Main) {
            mMessageDialog = MessageDialog.Builder(requireContext())
                .setMessage(getString(R.string.ota_tips_dialog))
                .setConfirm(android.R.string.ok) // 设置 null 表示不显示取消按钮
                .setCancel(null) // 设置点击按钮后不关闭对话框
                .show()
            mMessageDialog.setCanceledOnTouchOutside(false)
        }
    }

    override fun onOtaStartConfig() {
        Log.e(TAG, "onOtaStartConfig:")
        lifecycleScope.launch(Dispatchers.Main) {
            mWaitDialog = LoadDialogFragment(getString(R.string.upgrade_message))
            mWaitDialog.show(requireActivity().supportFragmentManager, "ota")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.unRegisterViewCallback(this)
    }

    companion object {
        const val TAG: String = "OtaFragment"
    }
}