package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DemandCreateModel"

class DemandCreateModel(private val createDemandRepository: DemandRepository) : ViewModel() {

    val isTurning = MutableLiveData<Boolean>()
    val demandAdded = MutableLiveData<Boolean>()
    val titleLiveData = MutableLiveData<String>()
    val isValidData = MediatorLiveData<Boolean>().apply {
        addSource(titleLiveData) { title ->
            this.value = !title.isNullOrBlank()
        }
    }

    fun addDemand(requestBody: RequestBody) {
        isTurning.postValue(true)
        createDemandRepository.addDemand(requestBody).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse addDemand added demand id: ${response.body()}")
                    demandAdded.postValue(true)
                } else {
                    demandAdded.postValue(false)
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse error : $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "onFailure addDemand: ${t.message}")
                isTurning.postValue(false)
                demandAdded.postValue(false)
            }

        })
    }

}