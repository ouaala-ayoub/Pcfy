package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.repositories.HomeRepository
import alpha.company.pc.utils.getError
import alpha.company.pc.data.models.network.Error
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeModel"
private const val NO_ANNONCE = "Pas d'annonces"
private const val ERROR_MSG = "Erreur inattendue"

class HomeModel(private val homeRepository: HomeRepository) : ViewModel() {

    val annoncesList = MutableLiveData<List<Annonce>?>()
    val popularsList = MutableLiveData<List<Annonce>?>()
    val emptyMsg = MutableLiveData<String>()
    val isProgressBarTurning = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<Error?>()

    fun getAnnoncesListAll(): MutableLiveData<List<Annonce>?> {

        val response = homeRepository.getAnnonces(null, null)
        isProgressBarTurning.postValue(true)

        response.enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "response is successful = ${response.isSuccessful}")
                    Log.i(TAG, "response body ${response.body()} ")
                    annoncesList.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "response error $error")
                    if (error != null) {
                        errorMessage.postValue(error)
                    }
                    annoncesList.postValue(null)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
                isProgressBarTurning.postValue(false)
                annoncesList.postValue(null)
            }
        })
        return annoncesList
    }

    fun getAnnoncesByCategory(category: String) {

        isProgressBarTurning.postValue(true)

        homeRepository.getAnnoncesByCategory(category).enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "response body ${response.body()} ")
                    annoncesList.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "response error $error")
                    if (error != null) {
                        errorMessage.postValue(error)
                    }
                    annoncesList.postValue(null)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                isProgressBarTurning.postValue(false)
                annoncesList.postValue(null)
            }

        })


    }

    fun getPopularAnnonces() {

        isProgressBarTurning.postValue(true)

        homeRepository.getPopularAnnonces().enqueue(object : Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    popularsList.postValue(response.body())
                } else {
                    val error = response.errorBody()?.let { getError(it, response.code()) }
                    Log.e(TAG, "getPopularAnnonces error : ${error?.message}")
                    popularsList.postValue(null)
                }
                isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure getPopularAnnonces: ${t.message}")
                popularsList.postValue(null)
                isProgressBarTurning.postValue(false)
            }

        })
    }

    fun updateIsEmpty() {
        if (annoncesList.value?.isEmpty() == true) {
            emptyMsg.postValue(NO_ANNONCE)
        } else if (annoncesList.value == null) {
            emptyMsg.postValue(ERROR_MSG)
        } else {
            emptyMsg.postValue("")
        }
    }

}
