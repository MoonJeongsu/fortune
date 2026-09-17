package com.dakbit.fortune

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener

/**
 * 카울리 전면 광고. 공식 흐름: 이동 시점에 request → onReceive에서 즉시 show / 아니면 cancel.
 * 화면 이동은 광고 콜백을 기다리지 않는다.
 * - Debug: 테스트 AppCode `CAULY`, 쿨다운 없음
 * - Release: 실 AppCode + 240초 쿨다운
 * - [ACCEPT_WINDOW_MS] 안에 수신되면 노출, 그 이후·실패·만료는 폐기
 */
class InterstitialAdManager(
    private val activity: Activity,
) : CaulyInterstitialAdListener {
    private val prefs = activity.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var loading = false
    private var showing = false
    private var requestStartedAt = 0L

    fun showThenNavigate(onNavigate: () -> Unit) {
        onNavigate()
        if (activity.isFinishing || activity.isDestroyed) return
        if (!canShow()) return
        if (loading || showing) return
        requestAd()
    }

    private fun requestAd() {
        loading = true
        requestStartedAt = SystemClock.elapsedRealtime()
        val adInfo = CaulyAdInfoBuilder(BuildConfig.CAULY_APP_CODE).build()
        val ad = CaulyInterstitialAd()
        ad.setAdInfo(adInfo)
        ad.setInterstialAdListener(this)
        ad.requestInterstitialAd(activity)
    }

    override fun onReceiveInterstitialAd(ad: CaulyInterstitialAd, isChargeableAd: Boolean) {
        loading = false
        val tooLate = SystemClock.elapsedRealtime() - requestStartedAt > ACCEPT_WINDOW_MS
        if (tooLate || showing || activity.isFinishing || activity.isDestroyed) {
            ad.cancel()
            return
        }
        showing = true
        try {
            ad.show(activity)
            recordShown()
        } catch (_: Exception) {
            showing = false
            ad.cancel()
        }
    }

    override fun onFailedToReceiveInterstitialAd(
        ad: CaulyInterstitialAd,
        errorCode: Int,
        errorMsg: String,
    ) {
        loading = false
    }

    override fun onClosedInterstitialAd(ad: CaulyInterstitialAd) {
        showing = false
    }

    override fun onLeaveInterstitialAd(ad: CaulyInterstitialAd) = Unit

    override fun onClickInterstitialAd(ad: CaulyInterstitialAd) = Unit

    override fun onTimeout(ad: CaulyInterstitialAd, errorMsg: String) {
        loading = false
        showing = false
    }

    private fun canShow(): Boolean {
        if (BuildConfig.DEBUG) return true

        val lastShownAt = prefs.getLong(KEY_LAST_SHOWN_AT, 0L)
        if (lastShownAt <= 0L) return true

        val elapsed = System.currentTimeMillis() - lastShownAt
        return elapsed >= COOLDOWN_MS
    }

    private fun recordShown() {
        prefs.edit()
            .putLong(KEY_LAST_SHOWN_AT, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "dakbit_ads"
        private const val KEY_LAST_SHOWN_AT = "last_interstitial_shown_at"
        private const val COOLDOWN_MS = 240_000L
        private const val ACCEPT_WINDOW_MS = 3_000L
    }
}
