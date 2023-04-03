package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.remote.CustomMessageResponse
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

private const val TAG = "DemandModifyMode"

class DemandModifyModel(private val demandRepository: DemandRepository) : ViewModel() {

    //mutable live data
    private val _demand = MutableLiveData<Demand?>()
    private val _isTurning = MutableLiveData<Boolean>()
    private val _demandUpdated = MutableLiveData<Boolean>()
    val titleLiveData = MutableLiveData<String>()
    private val _mediatorLiveData = MediatorLiveData<Boolean>().apply {
        addSource(titleLiveData) { title ->
            this.value = !title.isNullOrBlank()
        }
    }

    //live data
    val mediatorLiveData: LiveData<Boolean>
        get() = _mediatorLiveData
    val demand: LiveData<Demand?>
        get() = _demand
    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val demandUpdated: LiveData<Boolean>
        get() = _demandUpdated

    fun updateDemand(demandId: String, demandBody: RequestBody) {
        _isTurning.postValue(true)
        demandRepository.updateDemand(demandId, demandBody)
            .enqueue(object : Callback<CustomMessageResponse> {
                override fun onResponse(
                    call: Call<CustomMessageResponse>,
                    response: Response<CustomMessageResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.i(TAG, "updateDemand onResponse: ${response.body()}")
                        _demandUpdated.postValue(true)
                    } else {
                        val error = getError(response.errorBody(), response.code())
                        Log.e(TAG, "error onResponse: $error ")
                        _demandUpdated.postValue(false)
                    }
                    _isTurning.postValue(false)
                }

                override fun onFailure(call: Call<CustomMessageResponse>, t: Throwable) {
                    Log.e(TAG, "updateDemand onFailure: ${t.message}")
                    _demandUpdated.postValue(false)
                    _isTurning.postValue(false)
                }

            })
    }

    fun getDemandById(demandId: String) {
        _isTurning.postValue(true)
        demandRepository.getDemandById(demandId).enqueue(object : Callback<Demand> {
            override fun onResponse(call: Call<Demand>, response: Response<Demand>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getDemandById onResponse: ${response.code()}")
                    _demand.postValue(response.body())
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "error onResponse: $error ")
                    _demand.postValue(null)
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<Demand>, t: Throwable) {
                Log.e(TAG, "onFailure getDemandById: ${t.message}")
                _demand.postValue(null)
                _isTurning.postValue(false)
            }

        })
    }
}