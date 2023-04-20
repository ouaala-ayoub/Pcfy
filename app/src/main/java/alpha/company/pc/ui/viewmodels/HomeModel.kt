package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.models.network.AnnoncesResponse
import alpha.company.pc.data.models.network.Category
import alpha.company.pc.data.models.network.InfoResponse
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
    private val _pagesList = MutableLiveData<List<Int>>()
    var currentIndex = 0

    val annoncesList: LiveData<MutableList<Annonce>?>
        get() = _annoncesList
    val popularsList: LiveData<List<Annonce>?>
        get() = getPopularAnnonces()
    val categoriesList: LiveData<List<String>>
        get() = getCategories()
    val emptyMsg: LiveData<String>
        get() = _emptyMsg
    val isProgressBarTurning: LiveData<Boolean>
        get() = _isProgressBarTurning
    val newAnnoncesAdded: LiveData<Boolean>
        get() = _newAnnoncesAdded
    val pagesList: LiveData<List<Int>>
        get() = _pagesList

    fun getPagesList(numPages: Int): List<Int> {
        if (numPages == 0 || numPages == 1) return listOf(0)
        val list = (0 until numPages).asIterable().toList().shuffled()
        Log.i(TAG, "getPagesList: $list")
        return list
    }

    fun getNumPages() {
        homeRepository.getNumPages().enqueue(object : Callback<InfoResponse> {
            override fun onResponse(call: Call<InfoResponse>, response: Response<InfoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()
                    val pagesList = getPagesList(res!!.length)

                    _pagesList.postValue(pagesList)
                    getAnnoncesListAll(pagesList[0].toString())
                } else {
                    getError(response.errorBody(), response.code())
                }
            }

            override fun onFailure(call: Call<InfoResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }

        })
    }

    fun getCategories(): LiveData<List<String>> {
        homeRepository.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val categoriesList = response.body()?.map { category ->
                        category.category
                    }
                    _categoriesList.postValue(categoriesList!!)
                } else {
                    val error = getError(response.errorBody(), response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
        return _categoriesList
    }


    private fun getAnnoncesGeneral(
        category: String? = null,
        searchQuery: String? = null,
        page: String? = null,
        add: Boolean = true
    ) {
        Log.d(TAG, "page: $page")
        _isProgressBarTurning.postValue(true)

        val response = homeRepository.getAnnonces(category, searchQuery, page)
        response.enqueue(object : Callback<AnnoncesResponse> {
            override fun onResponse(
                call: Call<AnnoncesResponse>,
                response: Response<AnnoncesResponse>
            ) {

                if (response.isSuccessful && response.body() != null) {
                    if (add) {
                        //response list in not null or empty
                        if (!response.body()?.annonces.isNullOrEmpty()) {
                            _newAnnoncesAdded.postValue(true)
                            val annoncesList = _annoncesList.value
                            annoncesList?.addAll(response.body()!!.annonces.shuffled())
                            this@HomeModel._annoncesList.postValue(annoncesList)


                        } else {
                            _newAnnoncesAdded.postValue(false)
                        }
                    } else {
                        Log.d(TAG, "getAnnonces : setting")
                        _newAnnoncesAdded.postValue(true)
                        _annoncesList.postValue(
                            response.body()?.annonces?.shuffled()?.toMutableList()
                        )
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

            override fun onFailure(call: Call<AnnoncesResponse>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
                _isProgressBarTurning.postValue(false)
                _newAnnoncesAdded.postValue(false)
                _annoncesList.postValue(null)
            }
        })
        currentIndex++

    }

    fun getAnnoncesListAll(page: String): LiveData<MutableList<Annonce>?> {
        currentIndex = 0
        getAnnoncesGeneral(add = false, page = page)
        return _annoncesList
    }

    fun addAnnoncesListAll(page: String) {
        getAnnoncesGeneral(page = page)
    }

    fun addAnnoncesByCategory(category: String, page: String) {
        getAnnoncesGeneral(category = category, page = page)
    }

    fun getAnnoncesByCategory(category: String) {
        currentIndex = 0
        getAnnoncesGeneral(category = category, add = false)
    }

    fun getPopularAnnonces(): LiveData<List<Annonce>?> {

        _isProgressBarTurning.postValue(true)

        homeRepository.getPopularAnnonces().enqueue(object : Callback<AnnoncesResponse> {

            override fun onResponse(
                call: Call<AnnoncesResponse>,
                response: Response<AnnoncesResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _popularsList.postValue(response.body()?.annonces)
                } else {
                    val error = response.errorBody()?.let { getError(it, response.code()) }
                    Log.e(TAG, "getPopularAnnonces error : ${error?.message}")
                    _popularsList.postValue(null)
                }
                _isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<AnnoncesResponse>, t: Throwable) {
                Log.e(TAG, "onFailure getPopularAnnonces: ${t.message}")
                _popularsList.postValue(null)
                _isProgressBarTurning.postValue(false)
            }

        })
        return _popularsList
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
