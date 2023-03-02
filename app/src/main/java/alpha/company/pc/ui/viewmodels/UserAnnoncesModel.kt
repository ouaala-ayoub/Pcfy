package alpha.company.pc.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.*
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.utils.INTERSTITIAL_ANNONCE_CLICKED_ID
import alpha.company.pc.utils.getError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "UserAnnoncesModel"

class UserAnnoncesModel(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {

    private var mInterstitialAd: InterstitialAd? = null
    val annoncesList = MutableLiveData<MutableList<Annonce>?>()
    private val isEmpty = MutableLiveData<Boolean>()
    val isTurning = MutableLiveData<Boolean>()
    val deletedAnnonce = MutableLiveData<Boolean>()

    fun getAnnoncesById(userId: String): MutableLiveData<MutableList<Annonce>?> {

        isTurning.postValue(true)

        userInfoRepository.getAnnonces(userId).enqueue(object : Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse body ${response.body()}")
                    annoncesList.postValue(response.body()!!.toMutableList())

                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    annoncesList.postValue(null)
                    Log.e(TAG, "onResponse error $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "getAnnoncesById onFailure: ${t.message}")
                annoncesList.postValue(null)
                isTurning.postValue(false)
            }

        })

        return annoncesList
    }

    fun deleteAnnonce(tokens: Tokens, annonceId: String) {

        isTurning.postValue(true)

        userInfoRepository.deleteAnnonce(tokens, annonceId).enqueue(object : Callback<IdResponse> {

            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "deleteAnnonce onResponse: ${response.body()}")
                    deletedAnnonce.postValue(true)

                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse delete annonce: ${error?.message}")
                    deletedAnnonce.postValue(false)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "onFailure deleteAnnonce : ${t.message}")
                deletedAnnonce.postValue(false)
                isTurning.postValue(false)
            }

        })

    }

    fun initialiseAdd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ANNONCE_CLICKED_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad ${adError.message}")
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded ${interstitialAd.responseInfo}")
                    mInterstitialAd = interstitialAd

                    showAdd(context)
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

    fun updateIsEmpty(): MutableLiveData<Boolean> {
        if (annoncesList.value.isNullOrEmpty()) {
            isEmpty.postValue(true)
        } else isEmpty.postValue(false)
        return isEmpty
    }



}