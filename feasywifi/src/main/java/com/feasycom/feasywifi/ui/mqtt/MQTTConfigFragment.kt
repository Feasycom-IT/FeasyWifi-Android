package com.feasycom.feasywifi.ui.mqtt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentMqttConfigBinding
import com.feasycom.feasywifi.logic.MQTTRepository
import com.feasycom.feasywifi.logic.interfaces.MQTTCallbacks
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttToken

class MQTTConfigFragment : BaseFragment<FragmentMqttConfigBinding>(), MQTTCallbacks {

    val IP_REGEX =
        "^([1-9]\\d?|1\\d{2}|2[0-4]\\d|25[0-5])(\\.([1-9]?\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}\$".toRegex()

    private val mEditTextList = mutableListOf<EditText>()

    private lateinit var mWaitDialog: LoadDialogFragment

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMqttConfigBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MQTTRepository.registerViewCallback(this)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.backGroup.visibility = View.VISIBLE
        mBinding.header.titleText.text = "MQTT"
        mBinding.portEv.setText("61613")
        mBinding.passwordEv.setText("password1")

        mEditTextList.add(mBinding.clientIdEv)
        mEditTextList.add(mBinding.userNameEv)
        mEditTextList.add(mBinding.passwordEv)
        mEditTextList.add(mBinding.portEv)
        mEditTextList.add(mBinding.brokerEv)
    }

    private fun initEvent() {
        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.connectBtn.setOnClickListener {
            if (mBinding.clientIdEv.text.isNullOrEmpty()) {
                mBinding.clientIdInputLayout.error = "请输入Client ID"
            }
            if (mBinding.userNameEv.text.isNullOrEmpty()) {
                mBinding.userNameInputLayout.error = "请输入User Name"
            }

            if (mBinding.passwordEv.text.isNullOrEmpty()) {
                mBinding.userNameInputLayout.error = "请输入Password"
            }
            try {
                if (mBinding.portEv.text.isNullOrEmpty() || mBinding.portEv.text.toString()
                        .toInt() > 65535
                ) {
                    mBinding.portInputLayout.error = "端口号错误"
                }
            } catch (e: Exception) {
                mBinding.portInputLayout.error = "端口号错误"
            }
            if (mBinding.brokerEv.text.toString().isEmpty() || !mBinding.brokerEv.text.toString()
                    .contains(IP_REGEX)
            ) {
                mBinding.brokerInputLayout.error = "地址错误"
            }

            if (mBinding.brokerInputLayout.isErrorEnabled || mBinding.portInputLayout.isErrorEnabled || mBinding.clientIdInputLayout.isErrorEnabled || mBinding.userNameInputLayout.isErrorEnabled || mBinding.passwordInputLayout.isErrorEnabled) {
                return@setOnClickListener
            }
            mEditTextList.forEach {
                it.clearFocus()
                hideKeyboard(it)
            }

            /*(context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS);//关闭输入法*/
            mWaitDialog = LoadDialogFragment("connect")
            mWaitDialog.show(requireActivity().supportFragmentManager, "connect")
            MQTTRepository.connect(
                mBinding.brokerEv.text.toString(),
                mBinding.portEv.text.toString(),
                mBinding.clientIdEv.text.toString(),
                mBinding.userNameEv.text.toString(),
                mBinding.passwordEv.text.toString()
            )
        }
        mBinding.brokerEv.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    v as EditText
                    if (!v.text.contains(IP_REGEX)) {
                        mBinding.brokerInputLayout.error = getString(R.string.format_error)
                    }
                } else {
                    mBinding.brokerInputLayout.isErrorEnabled = false
                }
            }

        mBinding.portEv.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v as EditText
                try {
                    if (v.text.isNullOrEmpty() || v.text.toString().toInt() !in 0..65535) {
                        mBinding.portInputLayout.error = getString(R.string.port_number_error)
                    }
                } catch (e: NumberFormatException) {
                    mBinding.portInputLayout.error = getString(R.string.port_number_error)
                }
            } else {
                mBinding.portInputLayout.isErrorEnabled = false
            }
        }
        mBinding.passwordEv.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.passwordInputLayout.isErrorEnabled = false
            }
        }
        mBinding.clientIdEv.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mBinding.clientIdInputLayout.isErrorEnabled = false
            }
        }

        mBinding.userNameEv.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mBinding.userNameInputLayout.isErrorEnabled = false
            }
        }

        mBinding.passwordEv.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mBinding.passwordInputLayout.isErrorEnabled = false
            }
        }
        mBinding.passwordEv.doAfterTextChanged {
            mBinding.passwordInputLayout.isErrorEnabled = false
        }
    }

    private fun hideKeyboard(v: View) {
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MQTTRepository.unRegisterViewCallback(this)
    }

    override fun onConnect() {
        LogUtils.d("MQTTConfigFragment", "FeasyWiFi APP onConnect: 连接成功")
        lifecycleScope.launch(Dispatchers.Main) {
            mWaitDialog.dismiss()
            findNavController().navigate(R.id.action_MQTTConfigFragment_to_MQTTFragment)
        }
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        super.onFailure(asyncActionToken, exception)
        LogUtils.e("MQTTConfigFragment", "FeasyWiFi APP onFailure: asyncActionToken ->  $asyncActionToken  exception  ->  ${exception!!::class.java} $exception   ${exception?.message}",)
        mWaitDialog.dismiss()
        when (exception.message) {
            "错误的用户名或密码" -> mBinding.passwordInputLayout.error = exception?.message
            "Bad user name or password" -> mBinding.passwordInputLayout.error = exception?.message
            "无法连接至服务器" -> mBinding.brokerInputLayout.error = exception?.message
            "Unable to connect to server" -> mBinding.brokerInputLayout.error = exception?.message
        }
        AlertDialog.Builder(requireContext())
            .setMessage(exception?.message)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}