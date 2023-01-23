package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.data.repositories.OrdersRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "RequestsModel"

class RequestsModel(private val ordersRepository: OrdersRepository) {

    val isEmpty = MutableLiveData<Boolean>()
    val userRequests = MutableLiveData<List<Order>?>()
    val isTurning = MutableLiveData<Boolean>()

    fun getUserRequests(usersId: String) {

        isTurning.postValue(true)

        ordersRepository.getUserRequests(usersId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "getUserRequests onResponse: ${response.headers()}")
                    userRequests.postValue(response.body())
                    updateIsEmpty(response.body()!!)
                } else {
                    userRequests.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                userRequests.postValue(null)
                isTurning.postValue(false)
            }

        })
    }

    private fun updateIsEmpty(list: List<Order>) {
        isEmpty.postValue(list.isEmpty())
    }

}