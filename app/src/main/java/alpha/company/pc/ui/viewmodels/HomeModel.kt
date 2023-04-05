package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.repositories.HomeRepository
import alpha.company.pc.utils.getError
import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeModel"
private const val NO_ANNONCE = "Pas d'annonces"
private const val ERROR_MSG = "Erreur inattendue"

class HomeModel(private val homeRepository: HomeRepository) : ViewModel() {

    private val _annoncesList = MutableLiveData<MutableList<Annonce>?>()
    private val _popularsList = MutableLiveData<List<Annonce>?>()
    private val _categoriesList = MutableLiveData<List<String>>()
    private val _emptyMsg = MutableLiveData<String>()
    private val _isProgressBarTurning = MutableLiveData<Boolean>()
    private val _newAnnoncesAdded = MutableLiveData<Boolean>()
    private var currentPage = 0

    val annoncesList: LiveData<MutableList<Annonce>?>
        get() = _annoncesList
    val popularsList: LiveData<List<Annonce>?>
        get() = _popularsList
    val categoriesList: LiveData<List<String>>
        get() = _categoriesList
    val emptyMsg: LiveData<String>
        get() = _emptyMsg
    val isProgressBarTurning: LiveData<Boolean>
        get() = _isProgressBarTurning
    val newAnnoncesAdded: LiveData<Boolean>
        get() = _newAnnoncesAdded

    fun getCategories() {
        homeRepository.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null)
                    _categoriesList.postValue(response.body())
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


    private fun getAnnoncesGeneral(
        category: String? = null,
        searchQuery: String? = null,
        page: String? = null,
        add: Boolean = true
    ) {
        _isProgressBarTurning.postValue(true)

        val response = homeRepository.getAnnonces(category, searchQuery, page)
        response.enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "response is successful = ${response.body()!!.size}")
                    if (add) {
                        if (response.body()!!.isNotEmpty()) {
                            _newAnnoncesAdded.postValue(true)
                            val annoncesList = _annoncesList.value
                            annoncesList?.addAll(response.body()!!)
                            this@HomeModel._annoncesList.postValue(annoncesList)
                        } else {
                            Log.i(TAG, "getAnnonces onResponse: empty list")
                            _newAnnoncesAdded.postValue(false)
                        }
                    } else {
                        Log.d(TAG, "getAnnonces : setting")
                        _newAnnoncesAdded.postValue(true)
                        _annoncesList.postValue(response.body()!!.toMutableList())
                    }

                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "response error $error")
                    _newAnnoncesAdded.postValue(false)
                    _annoncesList.postValue(null)
                }
                _isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
                _isProgressBarTurning.postValue(false)
                _newAnnoncesAdded.postValue(false)
                _annoncesList.postValue(null)
            }
        })
        currentPage++

    }

    fun getAnnoncesListAll() {
        getAnnoncesGeneral(add = false)
    }

    fun addAnnoncesListAll() {
        getAnnoncesGeneral(page = currentPage.toString())
    }

    fun addAnnoncesByCategory(category: String) {
        getAnnoncesGeneral(category = category, page = currentPage.toString())
    }

    fun getAnnoncesByCategory(category: String) {
        getAnnoncesGeneral(category = category, add = false)
    }

    fun getPopularAnnonces() {

        _isProgressBarTurning.postValue(true)

        homeRepository.getPopularAnnonces().enqueue(object : Callback<List<Annonce>> {

            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    _popularsList.postValue(response.body())
                } else {
                    val error = response.errorBody()?.let { getError(it, response.code()) }
                    Log.e(TAG, "getPopularAnnonces error : ${error?.message}")
                    _popularsList.postValue(null)
                }
                _isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure getPopularAnnonces: ${t.message}")
                _popularsList.postValue(null)
                _isProgressBarTurning.postValue(false)
            }

        })
    }

    fun updateIsEmpty() {
        if (_annoncesList.value?.isEmpty() == true) {
            _emptyMsg.postValue(NO_ANNONCE)
        } else if (_annoncesList.value == null) {
            _emptyMsg.postValue(ERROR_MSG)
        } else {
            _emptyMsg.postValue("")
        }
    }

}
