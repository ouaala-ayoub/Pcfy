package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.repositories.HomeRepository
import alpha.company.pc.utils.getError
import android.os.Parcelable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeModel"
private const val NO_ANNONCE = "Pas d'annonces"
private const val ERROR_MSG = "Erreur inattendue"

class HomeModel(private val homeRepository: HomeRepository) : ViewModel() {

    var annoncesList = MutableLiveData<MutableList<Annonce>?>()
    val popularsList = MutableLiveData<List<Annonce>?>()
    val categoriesList = MutableLiveData<List<String>>()
    val emptyMsg = MutableLiveData<String>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getCategories() {
        homeRepository.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null)
                    categoriesList.postValue(response.body())
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
    }


    fun getAnnoncesGeneral(
        category: String? = null,
        searchQuery: String? = null,
        add: Boolean = true
    ) {
        val response = homeRepository.getAnnonces(category, searchQuery)
        isProgressBarTurning.postValue(true)

        response.enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "response is successful = ${response.body()}")
                    if (add) {
                        val annoncesList = annoncesList.value
                        annoncesList?.addAll(response.body()!!)
                        this@HomeModel.annoncesList.postValue(annoncesList)
                    } else {
                        annoncesList.postValue(response.body()!!.toMutableList())
                    }

                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "response error $error")

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

    }

    fun getAnnoncesListAll() {
        getAnnoncesGeneral(add = false)
    }

    fun addAnnoncesListAll() {
        getAnnoncesGeneral()
    }

    fun addAnnoncesByCategory(category: String) {
        getAnnoncesGeneral(category = category)
    }

    fun getAnnoncesByCategory(category: String) {
        getAnnoncesGeneral(category = category, add = false)
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
