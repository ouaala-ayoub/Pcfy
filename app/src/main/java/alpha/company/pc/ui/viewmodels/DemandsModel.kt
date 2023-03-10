package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val TAG = "DemandsModel"

class DemandsModel(
    private val demandRepository: DemandRepository,
    private val errorMsg: MessageText
) : ViewModel() {
    var demandsList = MutableLiveData<MutableList<Demand>?>()
    var rvState: Parcelable? = null
//    var demandsToAdd = MutableLiveData<List<Demand>?>()
//    fun getDemandsList(): LiveData<List<Demand>?>? {
//        if (demandsList == null) {
//            demandsList = MutableLiveData()
//            getDemands()
//        }
//        return demandsList
//    }

    val messageTv = MutableLiveData<String>()
    val isTurning = MutableLiveData<Boolean>()

//    fun freeDemandsToAdd() {}

    fun getDemands(searchQuery: String? = null) {
        getDemandsGeneral(searchQuery, false)
    }

    fun addDemands() {
        getDemandsGeneral()
    }

    private fun getDemandsGeneral(
        searchQuery: String? = null,
        add: Boolean = true
    ) {

        Log.d(TAG, "getDemands request executed")
        isTurning.postValue(true)

        demandRepository.getDemands(searchQuery).enqueue(object : Callback<List<Demand>> {
            override fun onResponse(call: Call<List<Demand>>, response: Response<List<Demand>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse getDemands: ${response.body()}")
                    if (add) {
                        val demandsList = demandsList.value
                        demandsList?.addAll(response.body()!!)
                        this@DemandsModel.demandsList.postValue(demandsList)
                    } else {
                        demandsList.postValue(response.body()!!.toMutableList())
                        if (response.body()!!.isEmpty()) {
                            messageTv.postValue(errorMsg.listEmpty)
                        } else {
                            messageTv.postValue(errorMsg.empty)
                        }
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
    val empty: String,
    val error: String,
    val listEmpty: String
)