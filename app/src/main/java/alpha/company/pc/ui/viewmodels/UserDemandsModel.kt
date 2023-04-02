package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UserDemandsModel"

class UserDemandsModel(private val demandsRepository: DemandRepository) : ViewModel() {

    private val _demands = MutableLiveData<List<Demand>?>()
    private val _isTurning = MutableLiveData<Boolean>()
    private val _deleted = MutableLiveData<Boolean>()

    val demands: LiveData<List<Demand>?>
        get() = _demands
    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val deleted: LiveData<Boolean>
        get() = _deleted

    fun getUserDemands(userId: String) {
        _isTurning.postValue(true)
        demandsRepository.getUserDemands(userId).enqueue(object : Callback<List<Demand>> {
            override fun onResponse(call: Call<List<Demand>>, response: Response<List<Demand>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getUserDemands : ${response.code()}")
                    _demands.postValue(response.body())
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "error: $error")
                    _demands.postValue(null)
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Demand>>, t: Throwable) {
                Log.e(TAG, "getUserDemands onFailure: ${t.message}")
                _isTurning.postValue(false)
                _demands.postValue(null)
            }

        })
    }

    fun deleteDemand(demandId: String) {
        _isTurning.postValue(true)
        demandsRepository.deleteDemand(demandId).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "deleted Demand with id : ${response.body()!!.objectId}")
                    _deleted.postValue(true)
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "error: $error")
                    _deleted.postValue(false)
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                Log.e(TAG, "deleteDemand onFailure: ${t.message}")
                _isTurning.postValue(false)
            }

        })

    }

}