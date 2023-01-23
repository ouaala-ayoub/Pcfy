package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "SingleAnnounceCommand"

class SingleAnnounceCommandModel(private val userInfoRepository: UserInfoRepository) :
    ViewModel() {

        val ordersList = MutableLiveData<List<Order>?>()
        val isTurning = MutableLiveData<Boolean>()

    fun getAnnonceOrders(annonceId: String) {

        isTurning.postValue(true)

        userInfoRepository.getAnnonceOrders(annonceId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful && response.body() != null) {

                    ordersList.postValue(response.body())
                } else {
                    ordersList.postValue(null)
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "onResponse getAnnonceOrders $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e(TAG, "getAnnonceOrders onFailure: ${t.message}")
                ordersList.postValue(null)
                isTurning.postValue(false)
            }
        })
    }
}