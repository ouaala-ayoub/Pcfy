package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DemandsModel"

class DemandsModel(private val demandRepository: DemandRepository, private val errorMsg: MessageText) : ViewModel() {
    val demandsList = MutableLiveData<List<Demand>?>()
    val messageTv = MutableLiveData<String>()
    val isTurning = MutableLiveData<Boolean>()

    fun getDemands() {

        isTurning.postValue(true)

        demandRepository.getDemands().enqueue(object : Callback<List<Demand>> {
            override fun onResponse(call: Call<List<Demand>>, response: Response<List<Demand>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse getDemands: ${response.body()}")
                    demandsList.postValue(response.body())
                    if (response.body()!!.isEmpty()){
                        messageTv.postValue(errorMsg.listEmpty)
                    } else {
                        messageTv.postValue(errorMsg.empty)
                    }


                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    demandsList.postValue(null)
                    messageTv.postValue(errorMsg.error)
                    Log.e(TAG, "onResponse getDemands error: $error")
                }
                isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Demand>>, t: Throwable) {
                Log.e(TAG, "onFailure getDemands : ${t.message}")
                demandsList.postValue(null)
                isTurning.postValue(false)
                messageTv.postValue(errorMsg.error)
            }

        })
    }
}
data class MessageText(
    val empty: String ,
    val error: String,
    val listEmpty: String
)