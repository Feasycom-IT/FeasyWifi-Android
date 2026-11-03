package com.feasycom.feasywifi.ui

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.FragmentQrBinding
import com.feasycom.feasywifi.logic.utils.FileUtils
import com.permissionx.guolindev.PermissionX

class QrFragment : BaseFragment<FragmentQrBinding>() {

    private lateinit var mResultDialog: AlertDialog

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentQrBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()

    }

    fun initView() {
        mBinding.header.titleText.text = getString(R.string.Facebook)
        mBinding.header.backGroup.visibility = View.VISIBLE
    }

    fun initEvent() {
        mBinding.save.setOnClickListener {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request { allGranted, _, _ ->
                    LogUtils.d(TAG, "FeasyWiFi APP initEvent allGranted -> $allGranted")
                    if (allGranted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()) {
                            // if (!query()){
                            val imgName = "wx.jpg"
                            val contentValues = ContentValues()
                            contentValues.put(
                                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES
                            )
                            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, imgName)
                            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg")
                            contentValues.put(MediaStore.Downloads.TITLE, imgName)
                            val insert = requireActivity().contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                            insert?.let {
                                try {
                                    val bitmap =
                                        BitmapFactory.decodeResource(resources, R.drawable.wx);
                                    val outputStream =
                                        requireActivity().contentResolver.openOutputStream(it)
                                    outputStream?.let {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                                        it.close()
                                        mResultDialog = AlertDialog.Builder(requireContext())
                                            .setMessage(getString(R.string.qr_success))
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    mResultDialog = AlertDialog.Builder(requireContext())
                                        .setMessage(getString(R.string.qr_failed))
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show()
                                }
                            } ?: let {
                                mResultDialog = AlertDialog.Builder(requireContext())
                                    .setMessage(getString(R.string.qr_failed))
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                            }
                            /*}else {
                                mResultDialog = AlertDialog.Builder(requireContext())
                                    .setMessage(getString(R.string.qr_success))
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                            }*/
                        } else {
                            FileUtils().saveBitmap(
                                requireContext(),
                                BitmapFactory.decodeResource(resources, R.drawable.wx),
                                "wechat.jpg"
                            ) {
                                mResultDialog = AlertDialog.Builder(requireContext())
                                    .setMessage(
                                        getString(
                                            if (it) {
                                                R.string.qr_success
                                            } else {
                                                R.string.qr_failed
                                            }
                                        )
                                    )
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                                mResultDialog.setCanceledOnTouchOutside(false)
                            }
                        }
                    }
                }
        }

        mBinding.header.backImg.setOnClickListener {
            findNavController().popBackStack()
        }
        mBinding.header.backTv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun query(): Boolean {
        val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Images.Media.DISPLAY_NAME + "=?"
        val arg = arrayOf("wx.jpg")
        val cursor = requireActivity().contentResolver.query(
            externalContentUri,
            null,
            selection,
            arg,
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val columnIndexOrThrow =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            var long = cursor.getLong(columnIndexOrThrow)

            cursor.close()
            long != 0L
        } else {
            LogUtils.e("TAG", "FeasyWiFi APP query: 查询失败")
            false
        }
    }

    companion object {
        const val TAG: String = "QrFragment"
    }
}