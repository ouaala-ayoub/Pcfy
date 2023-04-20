package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DemandCreateModel"

class DemandCreateModel(private val createDemandRepository: DemandRepository) : ViewModel() {

    private val _isTurning = MutableLiveData<Boolean>()
    private val _demandAdded = MutableLiveData<Boolean>()

    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val demandAdded: LiveData<Boolean>
        get() = _demandAdded

    val titleLiveData = MutableLiveData<String>()
    val isValidData = MediatorLiveData<Boolean>().apply {
        addSource(titleLiveData) { title ->
            this.value = !title.isNullOrBlank()
        }
    }

    fun addDemand(requestBody: RequestBody) {
        _isTurning.postValue(true)
        createDemandRepository.addDemand(requestBody).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse addDemand added demand id: ${response.body()}")
                    _demandAdded.postValue(true)
                } else {
                    _demandAdded.postValue(false)
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse error : $error")
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "onFailure addDemand: ${t.message}")
                _isTurning.postValue(false)
                _demandAdded.postValue(false)
            }

        })
    }

}