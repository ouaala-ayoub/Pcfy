package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.data.repositories.UserRepository
import alpha.company.pc.utils.getError
import android.util.Log
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

    val isTurning = MutableLiveData<Boolean>()
    val creator = MutableLiveData<User?>()
    val demand = MutableLiveData<Demand?>()

    fun getUserById(userId: String) {
        isTurning.postValue(true)
        userInfoRepository.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getUserById onResponse: ${response.body()}")
                    creator.postValue(response.body())
                } else {
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse: $error")
                    creator.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                creator.postValue(null)
                isTurning.postValue(false)
            }

        })
    }

    fun getDemandById(demandId: String) {
        isTurning.postValue(true)
        demandRepository.getDemandById(demandId).enqueue(object : Callback<Demand> {
            override fun onResponse(call: Call<Demand>, response: Response<Demand>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getDemandById Demand retrieved : ${response.body()}")
                    demand.postValue(response.body())
                } else {
                    demand.postValue(null)
                    val error = getError(response.errorBody(), response.code())
                    Log.e(TAG, "onResponse: $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<Demand>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                demand.postValue(null)
                isTurning.postValue(false)
            }

        })
    }

}