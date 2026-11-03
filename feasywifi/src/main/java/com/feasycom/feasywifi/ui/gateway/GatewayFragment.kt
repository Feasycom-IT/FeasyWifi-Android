package com.feasycom.feasywifi.ui.gateway

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentGatewayBinding
import com.feasycom.feasywifi.logic.BluetoothRepository
import com.feasycom.feasywifi.logic.interfaces.FeasyWiFiCallbacks
import com.feasycom.feasywifi.ui.BaseFragment
import com.feasycom.feasywifi.ui.dialog.BaseDialog
import com.feasycom.feasywifi.ui.dialog.LoadDialogFragment
import com.feasycom.feasywifi.ui.dialog.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

// 网关配置
class GatewayFragment : BaseFragment<FragmentGatewayBinding>(), FeasyWiFiCallbacks {

    private val mProtocolList: MutableList<String> by lazy {
        resources.getStringArray(R.array.gateway).toMutableList()
    }

    private val requests: Deque<ByteArray> = LinkedList()

    private lateinit var mWaitDialog: LoadDialogFragment

    private lateinit var mMessageDialog: BaseDialog

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGatewayBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        initView()
        initEvent()
    }

    fun initView() {
        requests.clear()
        mBinding.header.titleText.text = getString(R.string.title_gateway)
        mBinding.header.backGroup.visibility = View.VISIBLE

        //声明一个下拉列表的数组适配器
        val starAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), R.layout.item_select, mProtocolList)
        //设置数组适配器的布局样式
        starAdapter.setDropDownViewResource(R.layout.item_dropdown)
        //设置下拉框的数组适配器
        mBinding.spinner.adapter = starAdapter
        //设置下拉框默认的显示第一项
        mBinding.spinner.setSelection(0)
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mBinding.httpGroup.visibility = View.GONE
                mBinding.tcpGroup.visibility = View.GONE
                mBinding.mqttGroup.visibility = View.GONE
                when (position) {
                    0 -> {
                        mBinding.connectBtn.visibility = View.GONE

                    }
                    1 -> {
                        // MQTT
                        mBinding.mqttGroup.visibility = View.VISIBLE
                        mBinding.connectBtn.visibility = View.VISIBLE
                        BluetoothRepository.send("AT+PROFILE=1\r\n".toByteArray())
                    }
                    2 -> {
                        // TCP
                        mBinding.tcpGroup.visibility = View.VISIBLE
                        mBinding.connectBtn.visibility = View.VISIBLE
                        BluetoothRepository.send("AT+PROFILE=2\r\n".toByteArray())
                    }
                    3 -> {
                        // HTTP
                        mBinding.httpGroup.visibility = View.VISIBLE
                        mBinding.connectBtn.visibility = View.VISIBLE
                        BluetoothRepository.send("AT+PROFILE=3\r\n".toByteArray())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    fun initEvent() {

        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }

        mBinding.connectBtn.setOnClickListener {
            when (mBinding.spinner.selectedItemPosition) {
                1 -> {
                    if (mBinding.mqttBrokerEt.text.isNotBlank() && mBinding.mqttClientEt.text.isNotBlank()) {
                        LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+BROKER=${mBinding.mqttBrokerEt.text}\r\n")
                        requests.add("AT+BROKER=${mBinding.mqttBrokerEt.text}\r\n".toByteArray())
                        if (mBinding.mqttPortEt.text.isNotBlank()) {
                            val port = try {
                                mBinding.mqttPortEt.text.toString().toInt()
                            } catch (e: Exception) {
                                1883
                            }
                            LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+PORT=$port")
                            requests.add("AT+PORT=${port}\r\n".toByteArray())
                        }
                        LogUtils.d(TAG, "initEvent: AT+CLIENTID=${mBinding.mqttClientEt.text}")
                        requests.add("AT+CLIENTID=${mBinding.mqttClientEt.text}\r\n".toByteArray())
                        if (mBinding.mqttUserNameEt.text.isNotBlank()) {
                            LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+USERNAME=${mBinding.mqttUserNameEt.text}\r\n")
                            requests.add("AT+USERNAME=${mBinding.mqttUserNameEt.text}\r\n".toByteArray())
                        }
                        if (mBinding.mqttPasswordEt.text.isNotBlank()) {
                            LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+MQTTPWD=${mBinding.mqttPasswordEt.text}")
                            requests.add("AT+MQTTPWD=${mBinding.mqttPasswordEt.text}\r\n".toByteArray())
                        }
                        if (mBinding.mqttTopicEt.text.isNotBlank()) {
                            //requests.add("AT+PUBTPC=${mBinding.mqttTopicEt.text}\r\n".toByteArray()) // 支持旧版的BW固件
                            requests.add("AT+SUBTPC=${mBinding.mqttTopicEt.text}\r\n".toByteArray()) // 支持新的BW固件
                        }
                        requests.add("AT+REBOOT\r\n".toByteArray())
                    }
                }

                2 -> {
                    if (mBinding.tcpServerEt.text.toString()
                            .isNotBlank() && mBinding.tcpPortEt.text.toString().isNotBlank()
                    ) {
                        LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+TCPCFG=${mBinding.tcpServerEt.text},${mBinding.tcpPortEt.text}\n")
                        requests.add("AT+TCPCFG=${mBinding.tcpServerEt.text},${mBinding.tcpPortEt.text}\r\n".toByteArray())
                        requests.add("AT+REBOOT\r\n".toByteArray())
                    }
                }
                3 -> {
                    if (mBinding.httpEt.text.toString().isNotBlank()) {
                        LogUtils.d(TAG, "FeasyWiFi APP initEvent: AT+HTTPCFG=${mBinding.httpEt.text}")
                        requests.add("AT+HTTPCFG=${mBinding.httpEt.text}\r\n".toByteArray())
                        requests.add("AT+REBOOT\r\n".toByteArray())
                    }
                }
            }
            if (requests.isNotEmpty()) {
                mWaitDialog = LoadDialogFragment(getString(R.string.send_config))
                mWaitDialog.show(requireActivity().supportFragmentManager, "config")
                sendAt()
            }
        }
    }

    private fun sendAt() {
        if (requests.isNotEmpty()) {
            val byteArray = requests.remove()
            LogUtils.d(TAG, "FeasyWiFi APP sendAt: 发送")
            BluetoothRepository.send(byteArray)
        } else {
            LogUtils.d(TAG, "FeasyWiFi APP sendAt: 发送完成")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothRepository.unRegisterViewCallback(this)
    }

    override fun blePeripheralDisconnected() {
        super.blePeripheralDisconnected()
        // TODO: 断开连接
        lifecycleScope.launch(Dispatchers.Main) {

            if (::mWaitDialog.isInitialized && mWaitDialog.isVisible) {
                mWaitDialog.dismiss()

                mMessageDialog = MessageDialog.Builder(requireContext())
                    // 内容必须要填写
                    .setMessage(getString(R.string.config_success)) // 确定按钮文本
                    .setConfirm(getString(R.string.ok)) // 设置 null 表示不显示取消按钮
                    .setCancel(null) // 设置点击按钮后不关闭对话框
                    .setListener {
                        /*findNavController().navigate(GatewayFragmentDirections.actionGatewayFragmentToSettingFragment(false))*/
                        mMessageDialog.dismiss()
                        findNavController().navigate(R.id.action_gatewayFragment_to_deviceFragment)
                    }
                    .show()
            } else {
                findNavController().navigate(
                    GatewayFragmentDirections.actionBluetoothFragmentToSettingFragment(
                        true
                    )
                )
            }

        }
    }

    override fun packetReceived(strValue: String?) {
        super.packetReceived(strValue)
        LogUtils.d(TAG, "FeasyWiFi APP packetReceived: 收到数据  $strValue")
        sendAt()
    }

    override fun packetSend(strValue: String?) {
        super.packetSend(strValue)
        LogUtils.d(TAG, "FeasyWiFi packetSend: 发送数据  $strValue")
    }

    companion object {
        private const val TAG = "GatewayFragment"
    }
}