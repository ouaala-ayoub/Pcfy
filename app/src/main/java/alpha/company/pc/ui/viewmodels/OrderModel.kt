package alpha.company.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import alpha.company.pc.data.models.local.MessageResponse
import alpha.company.pc.data.models.network.Message
import alpha.company.pc.data.remote.RetrofitNotificationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "OrderModel"

class OrderModel(
    private val individualPrice: Float,

    ) : ViewModel() {
    val quantity = MutableLiveData(1)
    val price = MutableLiveData(1 * individualPrice)

    val name = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val address = MutableLiveData<String>()

    val isValidData = MediatorLiveData<Boolean>().apply {
        addSource(name) { name ->
            this.value = validateTheData(
                name,
                phoneNumber.value.toString(),
                address.value.toString()
            )
        }
        addSource(phoneNumber) { phoneNumber ->
            this.value = validateTheData(
                name.value.toString(),
                phoneNumber,
                address.value.toString()
            )
        }
        addSource(address) { address ->
            this.value = validateTheData(
                name.value.toString(),
                phoneNumber.value.toString(),
                address
            )
        }
    }

    fun notifySeller(message: Message, fireBaseKey: String) {
        RetrofitNotificationService.getInstance()
            .sendMessage(message, fireBaseKey = "key=$fireBaseKey")
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    Log.d(TAG, "onResponse : ${response.body()}")
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                }

            })
    }

    private fun validateTheData(name: String, phoneNumber: String, address: String): Boolean {
        val validPhone = phoneNumber.length == 10 && phoneNumber.matches(".*[0-9].*".toRegex())
        return name.isNotBlank() && validPhone && address.isNotBlank()
    }

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
        price.postValue(updatedPrice)
    }
}