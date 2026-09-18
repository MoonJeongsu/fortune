package com.dakbit.fortune

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.fsn.cauly.CaulyAdInfoBuilder
import com.fsn.cauly.CaulyInterstitialAd
import com.fsn.cauly.CaulyInterstitialAdListener
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAds.UnityAdsLoadError
import com.unity3d.ads.UnityAds.UnityAdsShowCompletionState
import com.unity3d.ads.UnityAds.UnityAdsShowError

/**
 * 전면 워터폴: 카울리 → 팽글 → 유니티.
 * 화면 이동은 광고를 기다리지 않는다.
 */
class InterstitialAdManager(
    private val activity: Activity,
) : CaulyInterstitialAdListener {
    private val prefs = activity.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var loading = false
    private var showing = false
    private var network = NETWORK_IDLE

    fun showThenNavigate(onNavigate: () -> Unit) {
        onNavigate()
        if (activity.isFinishing || activity.isDestroyed) return
        if (!canShow()) return
        if (loading || showing) return
        loading = true
        network = NETWORK_CAULY
        Log.d(TAG, "request cauly")
        requestCauly()
    }

    private fun requestCauly() {
        val adInfo = CaulyAdInfoBuilder(BuildConfig.CAULY_APP_CODE).build()
        val ad = CaulyInterstitialAd()
        ad.setAdInfo(adInfo)
        ad.setInterstialAdListener(this)
        ad.requestInterstitialAd(activity)
    }

    override fun onReceiveInterstitialAd(ad: CaulyInterstitialAd, isChargeableAd: Boolean) {
        runOnMain {
            if (network != NETWORK_CAULY || showing || activity.isFinishing || activity.isDestroyed) {
                ad.cancel()
                return@runOnMain
            }
            showing = true
            try {
                ad.show(activity)
                recordShown()
                Log.d(TAG, "cauly shown")
            } catch (_: Exception) {
                showing = false
                ad.cancel()
                loadPangle("cauly show fail")
            }
        }
    }

    override fun onFailedToReceiveInterstitialAd(
        ad: CaulyInterstitialAd,
        errorCode: Int,
        errorMsg: String,
    ) {
        runOnMain { loadPangle("cauly fail $errorCode $errorMsg") }
    }

    override fun onClosedInterstitialAd(ad: CaulyInterstitialAd) {
        runOnMain { finishWaterfall("cauly closed") }
    }

    override fun onLeaveInterstitialAd(ad: CaulyInterstitialAd) = Unit

    override fun onClickInterstitialAd(ad: CaulyInterstitialAd) = Unit

    override fun onTimeout(ad: CaulyInterstitialAd, errorMsg: String) {
        runOnMain { loadPangle("cauly timeout $errorMsg") }
    }

    private fun loadPangle(reason: String) {
        if (network != NETWORK_CAULY) return
        network = NETWORK_PANGLE
        Log.d(TAG, "advance pangle: $reason")
        if (activity.isFinishing || activity.isDestroyed) {
            finishWaterfall("activity gone")
            return
        }
        if (!PAGSdk.isInitSuccess()) {
            loadUnity("pangle not initialized")
            return
        }
        PAGInterstitialAd.loadAd(
            BuildConfig.PANGLE_INTERSTITIAL_SLOT_ID,
            PAGInterstitialRequest(),
            object : PAGInterstitialAdLoadListener {
                override fun onError(code: Int, message: String) {
                    runOnMain { loadUnity("pangle fail $code $message") }
                }

                override fun onAdLoaded(ad: PAGInterstitialAd) {
                    runOnMain {
                        if (network != NETWORK_PANGLE || showing ||
                            activity.isFinishing || activity.isDestroyed
                        ) {
                            finishWaterfall("pangle discarded")
                            return@runOnMain
                        }
                        ad.setAdInteractionListener(object : PAGInterstitialAdInteractionListener {
                            override fun onAdShowed() = Unit
                            override fun onAdClicked() = Unit
                            override fun onAdDismissed() {
                                runOnMain { finishWaterfall("pangle closed") }
                            }
                        })
                        showing = true
                        try {
                            ad.show(activity)
                            recordShown()
                            Log.d(TAG, "pangle shown")
                        } catch (_: Exception) {
                            showing = false
                            loadUnity("pangle show fail")
                        }
                    }
                }
            },
        )
    }

    private fun loadUnity(reason: String) {
        if (network != NETWORK_PANGLE) return
        network = NETWORK_UNITY
        Log.d(TAG, "advance unity: $reason")
        if (activity.isFinishing || activity.isDestroyed) {
            finishWaterfall("activity gone")
            return
        }
        if (!UnityAds.isInitialized) {
            finishWaterfall("unity not initialized")
            return
        }
        UnityAds.load(
            BuildConfig.UNITY_INTERSTITIAL_PLACEMENT_ID,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    runOnMain {
                        if (network != NETWORK_UNITY || showing ||
                            activity.isFinishing || activity.isDestroyed
                        ) {
                            finishWaterfall("unity discarded")
                            return@runOnMain
                        }
                        showing = true
                        UnityAds.show(activity, placementId, unityShowListener)
                        recordShown()
                        Log.d(TAG, "unity shown")
                    }
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String,
                    error: UnityAdsLoadError,
                    message: String,
                ) {
                    runOnMain { finishWaterfall("unity fail $error $message") }
                }
            },
        )
    }

    private val unityShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowFailure(
            placementId: String,
            error: UnityAdsShowError,
            message: String,
        ) {
            runOnMain { finishWaterfall("unity show fail $error $message") }
        }

        override fun onUnityAdsShowStart(placementId: String) = Unit
        override fun onUnityAdsShowClick(placementId: String) = Unit
        override fun onUnityAdsShowComplete(
            placementId: String,
            state: UnityAdsShowCompletionState,
        ) {
            runOnMain { finishWaterfall("unity closed") }
        }
    }

    private fun finishWaterfall(reason: String) {
        Log.d(TAG, "finish $reason")
        loading = false
        showing = false
        network = NETWORK_IDLE
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun canShow(): Boolean {
        if (BuildConfig.DEBUG) return true
        val lastShownAt = prefs.getLong(KEY_LAST_SHOWN_AT, 0L)
        if (lastShownAt <= 0L) return true
        return System.currentTimeMillis() - lastShownAt >= COOLDOWN_MS
    }

    private fun recordShown() {
        prefs.edit()
            .putLong(KEY_LAST_SHOWN_AT, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val TAG = "DakbitAds"
        private const val PREFS_NAME = "dakbit_ads"
        private const val KEY_LAST_SHOWN_AT = "last_interstitial_shown_at"
        private const val COOLDOWN_MS = 240_000L
        private const val NETWORK_IDLE = 0
        private const val NETWORK_CAULY = 1
        private const val NETWORK_PANGLE = 2
        private const val NETWORK_UNITY = 3

        fun initialize(app: Application) {
            initPangle(app)
            initUnity(app)
        }

        private fun initPangle(app: Application) {
            if (PAGSdk.isInitSuccess()) return
            val config = PAGConfig.Builder()
                .appId(BuildConfig.PANGLE_APP_ID)
                .debugLog(BuildConfig.DEBUG)
                .build()
            PAGSdk.init(app, config, object : PAGSdk.PAGInitCallback {
                override fun success() {
                    Log.d(TAG, "pangle init ok")
                }

                override fun fail(code: Int, msg: String) {
                    Log.w(TAG, "pangle init fail $code $msg")
                }
            })
        }

        private fun initUnity(app: Application) {
            if (UnityAds.isInitialized) return
            UnityAds.initialize(
                app,
                BuildConfig.UNITY_GAME_ID,
                BuildConfig.DEBUG,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        Log.d(TAG, "unity init ok")
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError,
                        message: String,
                    ) {
                        Log.w(TAG, "unity init fail $error $message")
                    }
                },
            )
        }
    }
}
