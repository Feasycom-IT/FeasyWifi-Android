package com.feasycom.feasywifi.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.feasycom.feasywifi.R
import com.feasycom.feasywifi.databinding.LyWebViewBinding

class LyWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    var onUrlChangeListener: ((b: Boolean) -> Unit)? = null
    private lateinit var mUrl: String
    private lateinit var mBinding: LyWebViewBinding

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LyWebView)
        a.getString(R.styleable.LyWebView_url)?.let {
            mUrl = it
        }

        a.recycle()
        initView(context)
        if (::mUrl.isInitialized) {
            initWebView()
        }
    }


    private fun initView(context: Context) {
        mBinding = LyWebViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        mBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onUrlChangeListener?.let {
                    try {
                        if (url != mUrl) {
                            it.invoke(false)
                        } else {
                            it.invoke(true)
                        }
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        mBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                try {
                    mBinding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        mBinding.progressBar.visibility = GONE
                    } else {
                        if (mBinding.progressBar.visibility == GONE) {
                            mBinding.progressBar.visibility = VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        with(mBinding.webView.settings) {
            javaScriptEnabled = true;
            javaScriptCanOpenWindowsAutomatically = true;
            allowFileAccess = true;// 设置允许访问文件数据
            setSupportZoom(true);
            builtInZoomControls = true;
            javaScriptCanOpenWindowsAutomatically = true;
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
            domStorageEnabled = true;
            databaseEnabled = true;
        }
//        mBinding.webView.setInitialScale(80)
        mBinding.webView.loadUrl(mUrl)
    }


    fun goBack() {
        mBinding.webView.goBack()
    }

    fun reload() {
        mBinding.webView.reload()
    }

    fun setUrl(url: String) {
        mUrl = url
        initWebView()
    }
}