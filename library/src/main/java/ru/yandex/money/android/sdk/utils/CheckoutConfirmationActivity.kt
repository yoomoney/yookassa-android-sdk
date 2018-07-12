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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.ViewGroup
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.Checkout
import ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_CODE
import ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_DESCRIPTION
import ru.yandex.money.android.sdk.Checkout.EXTRA_ERROR_FAILING_URL
import ru.yandex.money.android.sdk.Checkout.RESULT_ERROR
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.extensions.visible
import ru.yandex.money.android.sdk.impl.logging.ReporterLogger
import ru.yandex.money.android.sdk.impl.metrics.YandexMetricaReporter
import java.net.URL

const val EXTRA_URL = "ru.yandex.money.android.extra.URL"
const val EXTRA_REDIRECT_URL = "ru.yandex.money.android.extra.REDIRECT_URL"

class CheckoutConfirmationActivity : AppCompatActivity(), WebViewFragment.Listener {

    private lateinit var webViewFragment: WebViewFragment

    private var progress: ContentLoadingProgressBar? = null
    private var showProgress: Boolean = false
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getSerializableExtra(EXTRA_URL) as URL

        if (url.protocol == "https" || BuildConfig.DEBUG && url.protocol == "file") {
            if (savedInstanceState == null) {
                ReporterLogger(YandexMetricaReporter(this)).report("screen3ds")
            }

            setContentView(R.layout.ym_activity_web_view)
            title = null

            webViewFragment = supportFragmentManager.findFragmentById(R.id.web_view) as WebViewFragment
            webViewFragment.attach(this)

            val redirectUrl = intent.getSerializableExtra(EXTRA_REDIRECT_URL) as URL

            webViewFragment.load(url, redirectUrl)
        } else {
            onError(Checkout.ERROR_NOT_HTTPS_URL, "Not https:// url", url.toString())
        }
    }

    override fun setTitle(title: CharSequence?) {
        toolbar?.also {
            val parent = it.parent
            if (parent is CollapsingToolbarLayout) {
                parent.title = title
            } else {
                super.setTitle(title)
            }
        } ?: super.setTitle(title)
    }

    override fun onContentChanged() {
        toolbar = findViewById<Toolbar>(R.id.toolbar)?.also {
            setSupportActionBar(it)
            it.setNavigationOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ym_ic_close)
        }
    }

    override fun onDestroy() {
        if (this::webViewFragment.isInitialized) {
            webViewFragment.attach(null)
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val mi = menuInflater
        mi.inflate(R.menu.ym_menu_activity_webview, menu)

        val actionView = menu.findItem(R.id.progress).actionView
        progress = ((actionView as ViewGroup).getChildAt(0) as ContentLoadingProgressBar?)?.apply {
            val size = resources.getDimensionPixelSize(R.dimen.ym_checkout_confirmation_activity_progress_size)
            with(layoutParams) {
                width = size
                height = size
            }

            val padding = resources.getDimensionPixelSize(R.dimen.ym_space_m)
            setPadding(padding, padding, padding, padding)

            post(when (showProgress) {
                true -> Runnable { onShowProgress() }
                false -> Runnable { onHideProgress() }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (!(this::webViewFragment.isInitialized && webViewFragment.onBackPressed())) {
            super.onBackPressed()
        }
    }

    override fun onShowProgress() {
        showProgress = true

        if (progress?.visible == false) {
            progress?.show()
        }
    }

    override fun onHideProgress() {
        showProgress = false

        if (progress?.visible == true) {
            progress?.hide()
        }
    }

    override fun onError(errorCode: Int, description: String?, failingUrl: String?) {
        setResult(
            RESULT_ERROR,
            Intent()
                .putExtras(intent)
                .putExtra(EXTRA_ERROR_CODE, errorCode)
                .putExtra(EXTRA_ERROR_DESCRIPTION, description)
                .putExtra(EXTRA_ERROR_FAILING_URL, failingUrl)
        )
        finish()
    }

    override fun onSuccess() {
        setResult(
            RESULT_OK,
            Intent().putExtras(intent)
        )
        finish()
    }
}
