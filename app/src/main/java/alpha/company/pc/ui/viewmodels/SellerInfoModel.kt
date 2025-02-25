package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.repositories.AnnonceRepository
import alpha.company.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "SellerInfoModel"

class SellerInfoModel(private val annonceRepository: AnnonceRepository) : ViewModel() {

    val isTurning = MutableLiveData<Boolean>()
    private val isEmpty = MutableLiveData<Boolean>()
    private val seller = MutableLiveData<User>()
    private val annoncesList = MutableLiveData<List<Annonce>>()

    fun getUserById(userId: String): LiveData<User> {

        isTurning.postValue(true)

        annonceRepository.getUserById(userId).enqueue(object : Callback<User> {

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "getUserById onResponse: ${response.body()}")
                    seller.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "error body : $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                isTurning.postValue(false)
            }
        })

        return seller
    }

    fun getSellerAnnonces(userId: String): LiveData<List<Annonce>> {

        isTurning.postValue(true)

        annonceRepository.getUserAnnonces(userId).enqueue(object : Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getSellerAnnonces onResponse body ${response.body()}")
                    annoncesList.postValue(response.body())
                    isTurning.postValue(false)
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse error $error")
                    isTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "getAnnoncesById onFailure: ${t.message}")
                isTurning.postValue(false)
            }

        })

        return annoncesList
    }

    fun updateIsEmpty(): MutableLiveData<Boolean> {
        if (annoncesList.value.isNullOrEmpty()) {
            isEmpty.postValue(true)
        } else isEmpty.postValue(false)
        return isEmpty
    }

}