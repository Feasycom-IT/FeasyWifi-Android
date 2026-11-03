package com.feasycom.feasywifi.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.DialogFirstBinding

class FirstDialogFragment : DialogFragment() {

    var onAgree: (() -> Unit)? = null
    var onRefuse: (() -> Unit)? = null
    var onUserAgreement: (() -> Unit)? = null
    var onPrivacyAgreement: (() -> Unit)? = null

    private lateinit var mBinding: DialogFirstBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.setCanceledOnTouchOutside(false);
        dialog?.setOnKeyListener { _, keyCode, _ ->
            requireActivity().finish()
            keyCode == KeyEvent.KEYCODE_BACK
        }

        mBinding = DialogFirstBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val userSpan = SpannableString(getString(R.string.userAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onUserAgreement?.invoke()
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val privacySpan = SpannableString(getString(R.string.privacyAgreement)).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {

                    onPrivacyAgreement?.invoke()
                }
            }, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            setSpan(
                ForegroundColorSpan(Color.parseColor("#D1894A")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val stringBuilder =
            SpannableStringBuilder(getString(R.string.userAndPrivacyAgreementPrefix)).apply {
                append("《").append(userSpan).append("》")
                append("《").append(privacySpan).append("》")
                append(getString(R.string.userAndPrivacyAgreementSuffix))
            }
        with(mBinding.contentTextView) {
            movementMethod = LinkMovementMethod.getInstance()
            text = stringBuilder
            highlightColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        }

        mBinding.agreeTextView.isEnabled = false

        mBinding.isCheckedCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mBinding.agreeTextView.isEnabled = true
                mBinding.agreeTextView.setTextColor(resources.getColor(R.color.colorCancel))
            } else {
                mBinding.agreeTextView.isEnabled = false
                mBinding.agreeTextView.setTextColor(resources.getColor(R.color.rssi))
            }
        }

        mBinding.agreeTextView.setOnClickListener {
            onAgree?.invoke()
        }

        mBinding.refuseTextView.setOnClickListener {
            onRefuse?.invoke()
        }
    }

    override fun onResume() {
        val params: ViewGroup.LayoutParams = dialog?.window!!.attributes
        /**
         * remove the system dialog title and border background
         */
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
        super.onResume()
    }

    companion object {
        const val TAG = "FirstDialogFragment"
    }
}