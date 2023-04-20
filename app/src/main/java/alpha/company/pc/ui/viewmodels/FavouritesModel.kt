package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.repositories.FavouritesRepository
import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FavouritesModel"
private const val NO_ANNONCE = "Pas de favories"
private const val ERROR_MSG = "Erreur inattendue"

class FavouritesModel(
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val errorMessage = MutableLiveData<String>()
    private var _favouritesListLiveData = MutableLiveData<MutableList<Annonce>?>()
    private var _deletedWithSuccess = MutableLiveData<Boolean>()
    private val _emptyMsg = MutableLiveData<String>()
    private val _isProgressBarTurning = MutableLiveData<Boolean>()

    val favouritesListLiveData: LiveData<MutableList<Annonce>?>
        get() = _favouritesListLiveData
    val deletedWithSuccess: LiveData<Boolean>
        get() = _deletedWithSuccess
    val emptyMsg: LiveData<String>
        get() = _emptyMsg
    val isProgressBarTurning: LiveData<Boolean>
        get() = _isProgressBarTurning


    fun getFavourites(userId: String) {

        _isProgressBarTurning.postValue(true)

        favouritesRepository.getFavourites(userId).enqueue(object : Callback<List<Annonce>> {
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse getFavourites: ${response.body()}")
                    _favouritesListLiveData.postValue((response.body()!!).toMutableList())
                } else {
                    Log.e(TAG, "error body = ${response.errorBody()}")
                    Log.e(TAG, "error raw = ${response.raw()}")
                    _favouritesListLiveData.postValue(null)
                }
                _isProgressBarTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                Log.e(TAG, "onFailure getFavourites : ${t.message}")
                errorMessage.postValue(t.message)
                _favouritesListLiveData.postValue(null)
                _isProgressBarTurning.postValue(false)
            }
        })
    }


    fun deleteFavourite(userId: String, favouriteIdToDelete: String): MutableLiveData<Boolean> {

        _isProgressBarTurning.postValue(true)

        favouritesRepository.deleteFavourite(userId, favouriteIdToDelete)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        _deletedWithSuccess.postValue(true)
                        val favourites = _favouritesListLiveData.value
                        favourites?.removeAll { annonce ->
                            annonce.id == favouriteIdToDelete
                        }
                        _favouritesListLiveData.postValue(favourites)
                    } else {
//                        deletedWithSuccess.postValue(false)
                    }
                    _isProgressBarTurning.postValue(false)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "deleteFavourite onFailure: ${t.message}")
//                    deletedWithSuccess.postValue(false)
                    _isProgressBarTurning.postValue(false)
                }

            })
        return _deletedWithSuccess
    }

    fun updateIsEmpty() {
        if (_favouritesListLiveData.value?.isEmpty() == true) {
            _emptyMsg.postValue(NO_ANNONCE)
        } else if (_favouritesListLiveData.value == null) {
            _emptyMsg.postValue(ERROR_MSG)
        } else {
            _emptyMsg.postValue("")
        }
    }

}

