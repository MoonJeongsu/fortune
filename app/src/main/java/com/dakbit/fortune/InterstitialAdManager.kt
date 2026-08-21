package com.dakbit.fortune

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * 탭 선택 시 전면 광고.
 * - Debug: Google 테스트 광고 단위 (쿨다운 없음 — 검증 편의)
 * - Release: 실 광고 ID + 240초 쿨다운
 * 광고 미준비/실패 시 즉시 다음 화면으로 이동.
 */
class InterstitialAdManager(
    private val activity: Activity,
) {
    private val prefs = activity.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var interstitialAd: InterstitialAd? = null
    private var loading = false
    private var pendingNavigation: (() -> Unit)? = null

    fun preload() {
        if (activity.isFinishing || activity.isDestroyed) return
        if (loading || interstitialAd != null) return

        loading = true
        InterstitialAd.load(
            activity,
            BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitialAd = null
                }
            },
        )
    }

    /**
     * 쿨다운/미로드면 바로 [onNavigate].
     * 광고를 띄우면 dismiss/실패 후 [onNavigate] 호출.
     */
    fun showThenNavigate(onNavigate: () -> Unit) {
        if (!canShow()) {
            onNavigate()
            preload()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            onNavigate()
            preload()
            return
        }

        interstitialAd = null
        pendingNavigation = onNavigate
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                recordShown()
            }

            override fun onAdDismissedFullScreenContent() {
                finishPendingNavigation()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                finishPendingNavigation()
                preload()
            }
        }
        ad.show(activity)
    }

    private fun finishPendingNavigation() {
        pendingNavigation?.invoke()
        pendingNavigation = null
    }

    private fun canShow(): Boolean {
        // Debug는 테스트 광고 — 쿨다운 없이 검증 가능
        if (BuildConfig.DEBUG) return true

        val lastShownAt = prefs.getLong(KEY_LAST_SHOWN_AT, 0L)
        if (lastShownAt <= 0L) return true

        val elapsed = System.currentTimeMillis() - lastShownAt
        // 기기 시각이 뒤로 간 경우도 보수적으로 차단
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
    }
}
