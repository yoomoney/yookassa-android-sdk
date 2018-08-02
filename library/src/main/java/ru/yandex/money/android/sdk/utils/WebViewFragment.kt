/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yandex.money.android.sdk.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.annotation.Keep
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import ru.yandex.money.android.sdk.BuildConfig
import java.net.URL

private const val KEY_LOAD_URL = "loadUrl"
private const val KEY_REDIRECT_URL = "redirectUrl"

private val TAG = WebViewFragment::class.java.simpleName

@Keep
internal class WebViewFragment : Fragment() {

    private var listener: Listener? = null
    private var webView: WebView? = null

    private var loadUrl: String? = null
    private var redirectUrl: String? = null

    init {
        retainInstance = true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.apply {
            loadUrl = getString(KEY_LOAD_URL)
            redirectUrl = getString(KEY_REDIRECT_URL)
        }

        val context = checkNotNull(context) { "Context should be present here" }
        val appContext = context.applicationContext

        webView = WebView(appContext, null, 0).apply {
            isFocusableInTouchMode = true
            settings.javaScriptEnabled = true
            @Suppress("DEPRECATION")
            settings.saveFormData = false
            // set for Samsung Note 2, it asking save pass
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                @Suppress("DEPRECATION")
                settings.savePassword = false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
            }

            webViewClient = WebViewClientImpl()
            webChromeClient = WebChromeClient()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = webView

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.also { (it.parent as ViewGroup?)?.removeView(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString(KEY_LOAD_URL, loadUrl)
            putString(KEY_REDIRECT_URL, redirectUrl)
        }
    }

    fun attach(listener: Listener?) {
        this.listener = listener
    }

    fun onBackPressed(): Boolean {
        val canGoBack = webView?.canGoBack() == true

        if (canGoBack) {
            webView?.goBack()
        }

        return canGoBack
    }

    fun load(url: URL, redirectUrl: URL) {
        val webView = checkNotNull(webView) { "load should be called after fragment initialization" }


        val urlString = url.toString()
        val redirectUrlString = redirectUrl.toString()

        if (urlString != loadUrl || redirectUrlString != this.redirectUrl) {
            loadUrl = urlString
            this.redirectUrl = redirectUrlString
            webView.loadUrl(urlString)
        }
    }

    private inner class WebViewClientImpl : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            listener?.onShowProgress()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            listener?.onHideProgress()
        }

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val redirectUrl = checkNotNull(redirectUrl) { "redirectUrl should be present" }

            if (url?.startsWith(redirectUrl) == true) {
                Log.d(TAG, "success: $url")
                listener?.onSuccess()
                view?.stopLoading()
            } else {
                view?.loadUrl(url)
            }
            return true
        }

        @Suppress("OverridingDeprecatedMember")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            listener?.apply {
                onHideProgress()
                onError(errorCode, description, failingUrl)
            }
        }
    }

    interface Listener {
        fun onShowProgress()
        fun onHideProgress()
        fun onError(errorCode: Int, description: String?, failingUrl: String?)
        fun onSuccess()
    }
}
