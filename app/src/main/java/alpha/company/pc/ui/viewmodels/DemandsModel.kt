package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.utils.getError
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
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
    var rvState: Parcelable? = null
    private var _demandsList = MutableLiveData<MutableList<Demand>?>()
    private val _messageTv = MutableLiveData<String>()
    private val _isTurning = MutableLiveData<Boolean>()

    val demandsList: LiveData<MutableList<Demand>?>
        get() = _demandsList
    val messageTv: LiveData<String>
        get() = _messageTv
    val isTurning: LiveData<Boolean>
        get() = _isTurning

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
        _isTurning.postValue(true)

        demandRepository.getDemands(searchQuery).enqueue(object : Callback<List<Demand>> {
            override fun onResponse(call: Call<List<Demand>>, response: Response<List<Demand>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.i(TAG, "onResponse getDemands: ${response.body()}")
                    if (add) {
                        val demandsList = demandsList.value
                        demandsList?.addAll(response.body()!!)
                        this@DemandsModel._demandsList.postValue(demandsList)
                    } else {
                        _demandsList.postValue(response.body()!!.toMutableList())
                        if (response.body()!!.isEmpty()) {
                            _messageTv.postValue(errorMsg.listEmpty)
                        } else {
                            _messageTv.postValue(errorMsg.empty)
                        }
                    }
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    _demandsList.postValue(null)
                    _messageTv.postValue(errorMsg.error)
                    Log.e(TAG, "onResponse getDemands error: $error")
                }
                _isTurning.postValue(false)
            }

            override fun onFailure(call: Call<List<Demand>>, t: Throwable) {
                Log.e(TAG, "onFailure getDemands : ${t.message}")
                _demandsList.postValue(null)
                _isTurning.postValue(false)
                _messageTv.postValue(errorMsg.error)
            }

        })
    }
}

data class MessageText(
    val empty: String,
    val error: String,
    val listEmpty: String
)