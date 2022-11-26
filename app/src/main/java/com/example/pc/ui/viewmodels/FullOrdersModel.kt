package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pc.data.models.network.Order
import com.example.pc.data.repositories.OrdersRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FullOrdersModel"

class FullOrdersModel(private val ordersRepository: OrdersRepository) : ViewModel() {

    val isEmpty = MutableLiveData<Boolean>()
    val sellerOrders = MutableLiveData<List<Order>?>()
    val isTurning = MutableLiveData<Boolean>()

    fun getSellerOrders(sellerId: String) {

        isTurning.postValue(true)

        ordersRepository.getSellerOrders(sellerId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful && response.body() != null) {
                    sellerOrders.postValue(response.body())
                    updateIsEmpty(response.body()!!)
                } else {
                    sellerOrders.postValue(null)
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                sellerOrders.postValue(null)
                isTurning.postValue(false)
            }

        })
    }

    private fun updateIsEmpty(list: List<Order>) {
        isEmpty.postValue(list.isEmpty())
    }
}