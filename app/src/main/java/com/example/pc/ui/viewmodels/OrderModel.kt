package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val TAG = "OrderModel"

class OrderModel(private val individualPrice: Float) : ViewModel() {
    val quantity = MutableLiveData(1)
    val price = MutableLiveData(1 * individualPrice)

    fun quantityAdd() {
        val quantityVal = quantity.value!!
        quantity.postValue(quantityVal + 1)
        updatePrice(quantityVal + 1)
    }

    fun quantitySub() {
        val quantityVal = quantity.value!!
        if (quantityVal <= 1) {
            return
        } else {
            quantity.postValue(quantityVal - 1)
            updatePrice(quantityVal - 1)
        }
    }

    private fun updatePrice(quantityVal: Int) {
        val updatedPrice = individualPrice * quantityVal
        Log.i(TAG, "updatePrice: $updatedPrice")
        price.postValue(updatedPrice)
    }
}