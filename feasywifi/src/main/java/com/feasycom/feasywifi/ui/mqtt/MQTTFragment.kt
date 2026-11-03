package com.feasycom.feasywifi.ui.mqtt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentMqttBinding
import com.feasycom.feasywifi.logic.MQTTRepository
import com.feasycom.feasywifi.logic.interfaces.MQTTCallbacks
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

class MQTTFragment : BaseFragment<FragmentMqttBinding>(), MQTTCallbacks {

    private lateinit var mWaitDialog: LoadDialogFragment
    private lateinit var mMessageDialog: BaseDialog

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMqttBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MQTTRepository.registerViewCallback(this)
        initView()
        initEvent()
    }

    private fun initView() {
        mBinding.header.backGroup.visibility = View.VISIBLE
        mBinding.header.titleText.text = "MQTT"
    }

    private fun initEvent() {
        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.button2.setOnClickListener {
            MQTTRepository.subscribe(mBinding.textView19.text.toString())
        }
        mBinding.button3.setOnClickListener {
            MQTTRepository.publish(
                mBinding.textView21.text.toString(),
                mBinding.textView22.text.toString()
            )
        }
    }

    override fun onMessageArrived(topic: String?, message: MqttMessage?) {
        message?.payload?.let {
            mBinding.textView20.text.append("${String(it)}\r\n")
        }
    }

    override fun onDisconnect() {
        super.onDisconnect()
        // 消息对话框
        mMessageDialog = MessageDialog.Builder(requireContext()) // 标题可以不用填写
            .setMessage(getString(R.string.connection_disconnected)) // 确定按钮文本
            .setConfirm(getString(R.string.reconnect)) // 设置 null 表示不显示取消按钮
            .setCancel(getString(R.string.cancel)) // 设置点击按钮后不关闭对话框
            //.setAutoDismiss(false)
            .setListener(
                object : MessageDialog.OnListener {
                    override fun onConfirm(dialog: BaseDialog?) {
                        MQTTRepository.connect()
                        mWaitDialog = LoadDialogFragment("connect")
                        mWaitDialog.show(requireActivity().supportFragmentManager, "connect")
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        findNavController().popBackStack()
                    }
                })
            .show()
    }

    override fun onConnect() {
        super.onConnect()
        mWaitDialog.dismiss()
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        super.onFailure(asyncActionToken, exception)
        mWaitDialog.dismiss()
        mMessageDialog = MessageDialog.Builder(requireContext())
            // 标题可以不用填写
            // .setTitle("我是标题")
            // 内容必须要填写
            .setMessage(getString(R.string.connect_failed)) // 确定按钮文本
            .setConfirm(getString(R.string.close)) // 设置 null 表示不显示取消按钮
            .setCancel(null) // 设置点击按钮后不关闭对话框
            //.setAutoDismiss(false)
            .setListener(
                object : MessageDialog.OnListener {
                    override fun onConfirm(dialog: BaseDialog?) {
                        findNavController().popBackStack()
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                    }
                })
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MQTTRepository.disconnect()
        MQTTRepository.unRegisterViewCallback(this)
    }
}