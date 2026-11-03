package com.feasycom.feasywifi.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.feasycom.feasywifi.databinding.DialogLoadBinding

class LoadDialogFragment(message: String) : DialogFragment() {

    var mMessage: String = message
        set(value) {
            mBinding.messageTv.text = value
            field = value
        }

    private lateinit var mBinding: DialogLoadBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false);
        dialog?.setCanceledOnTouchOutside(false)
        mBinding = DialogLoadBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // dialog?.window?.attributes?.dimAmount = 0f
        dialog?.setCanceledOnTouchOutside(false)
        mBinding.messageTv.text = mMessage
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed) return
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            //同一实例使用不同的tag会异常，这里捕获一下
            e.printStackTrace()
        }
    }


}