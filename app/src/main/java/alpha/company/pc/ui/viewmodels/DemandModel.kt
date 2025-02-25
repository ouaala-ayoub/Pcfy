package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.data.repositories.UserRepository
import alpha.company.pc.utils.getError
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DemandModel"

class DemandModel(
    private val demandRepository: DemandRepository,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val _isTurning = MutableLiveData<Boolean>()
    private val _creator = MutableLiveData<User?>()
    private val _demand = MutableLiveData<Demand?>()

    val isTurning: LiveData<Boolean>
        get() = _isTurning
    val creator: LiveData<User?>
        get() = _creator
    val demand: LiveData<Demand?>
        get() = _demand

    fun getUserById(userId: String) {
        _isTurning.postValue(true)
        userInfoRepository.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getUserById onResponse: ${response.body()}")
                    _creator.postValue(response.body())
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse: $error")
                    _creator.postValue(null)
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                _creator.postValue(null)
                _isTurning.postValue(false)
            }

        })
    }

    fun getDemandById(demandId: String) {
        _isTurning.postValue(true)
        demandRepository.getDemandById(demandId).enqueue(object : Callback<Demand> {
            override fun onResponse(call: Call<Demand>, response: Response<Demand>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getDemandById Demand retrieved : ${response.body()}")
                    _demand.postValue(response.body())
                } else {
                    _demand.postValue(null)
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse: $error")
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<Demand>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                _demand.postValue(null)
                _isTurning.postValue(false)
            }

        })
    }

}