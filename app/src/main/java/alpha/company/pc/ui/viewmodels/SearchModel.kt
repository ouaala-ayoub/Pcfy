package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.models.network.AnnoncesResponse
import alpha.company.pc.data.repositories.SearchRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "SearchModel"
private const val EMPTY_MSG = "Pas de resultats trouvés"
private const val ERROR_MSG = "Erreur inattendue"

class SearchModel(private val searchRepository: SearchRepository, private val maxPrice: Int) :
    ViewModel() {

    val currentMax = MutableLiveData<Float>()
    val searchResult = MutableLiveData<List<Annonce>?>()
    val searchMessage = MutableLiveData<String>()
    val isTurning = MutableLiveData<Boolean>()

    fun search(searchKey: String?, price: Number? = null, status: String? = null) {

        if (searchKey.isNullOrBlank()) {
            searchResult.postValue(listOf())
            return
        }

        isTurning.postValue(true)

        searchRepository.getSearchResult(searchKey, price, status)
            .enqueue(object : Callback<AnnoncesResponse> {
                override fun onResponse(
                    call: Call<AnnoncesResponse>,
                    response: Response<AnnoncesResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        searchResult.postValue(response.body()?.annonces)
                    } else {
                        searchResult.postValue(null)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<AnnoncesResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                    searchResult.postValue(null)
                    isTurning.postValue(false)
                }

            })

    }

    fun updateSearchMessage() {

        if (searchResult.value?.isEmpty() == true) {
            searchMessage.postValue(EMPTY_MSG)
        } else if (searchResult.value == null) {
            searchMessage.postValue(ERROR_MSG)
        } else {
            searchMessage.postValue("")
        }

    }

    fun getMaxPrice(percentage: Float): Float {
        val max = percentage * maxPrice
        currentMax.postValue(max)
        Log.i(TAG, "calculateThePrice max = $max ")
        return max
    }

}

