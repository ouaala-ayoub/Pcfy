package com.example.pc.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Order
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.utils.INTERSTITIAL_ORDER_CLICKED_ID
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FullOrdersModel"

class FullOrdersModel(private val ordersRepository: OrdersRepository) : ViewModel() {

    private var mInterstitialAd: InterstitialAd? = null
    val isEmpty = MutableLiveData<Boolean>()
    val sellerOrders = MutableLiveData<List<Order>?>()
    val isTurning = MutableLiveData<Boolean>()

    fun getSellerOrders(sellerId: String) {

        isTurning.postValue(true)

        ordersRepository.getSellerOrders(sellerId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful && response.body() != null) {
                    sellerOrders.postValue(response.body())
                    updateIsEmpty(response.body()!!)
                } else {
                    sellerOrders.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                sellerOrders.postValue(null)
                isTurning.postValue(false)
            }

        })
    }

    fun initialiseAdd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ORDER_CLICKED_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad ${adError.message}")
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded ${interstitialAd.responseInfo}")
                    mInterstitialAd = interstitialAd

//                    showAdd()
                }
            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                super.onAdClicked()
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content error ${p0.message}.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    fun showAdd(context: Context) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(context as Activity)
            Log.d(TAG, "mInterstitialAd showed")
            mInterstitialAd = null
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet or already shown .")
        }
    }


    private fun updateIsEmpty(list: List<Order>) {
        isEmpty.postValue(list.isEmpty())
    }
}