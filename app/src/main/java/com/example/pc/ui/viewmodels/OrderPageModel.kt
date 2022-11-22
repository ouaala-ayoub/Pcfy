package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Order
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "OrderPageModel"

class OrderPageModel(private val ordersRepository: OrdersRepository) : ViewModel() {

    val isTurning = MutableLiveData<Boolean>()
    val order = MutableLiveData<Order?>()

    fun getOrderById(orderId: String) {

        isTurning.postValue(true)

        ordersRepository.getOrderById(orderId).enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful && response.body() != null) {
                    order.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.i(TAG, "onResponse error : $error")
                    order.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                order.postValue(null)
                isTurning.postValue(false)
            }

        })

    }

}