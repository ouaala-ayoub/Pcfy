package com.example.pc.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Error
import com.example.pc.data.repositories.HomeRepository
import com.example.pc.utils.getError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "HomeModel"

class HomeModel(private val homeRepository: HomeRepository): ViewModel() {
    private val annoncesList = MutableLiveData<List<Annonce>>()
    private val errorMessage = MutableLiveData<Error>()
    val isProgressBarTurning = MutableLiveData<Boolean>()

    fun getAnnoncesList(): MutableLiveData<List<Annonce>>{

        val response = homeRepository.getAnnonces()
        isProgressBarTurning.postValue(true)

        response.enqueue(object :Callback<List<Annonce>>{
            override fun onResponse(call: Call<List<Annonce>>, response: Response<List<Annonce>>) {

                if (response.isSuccessful && response.body() != null){
                    Log.i(TAG, "response is successful = ${response.isSuccessful}")
                    Log.i(TAG, "response body is null = ${response.body() != null}")
                    Log.i(TAG, "response body ${response.body()} ")
                    isProgressBarTurning.postValue(false)
                    annoncesList.postValue(response.body())
                }
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    Log.e(TAG, "response error $error")
                    if (error != null){
                        errorMessage.postValue(error!!)
                    }
                    isProgressBarTurning.postValue(false)
                }
            }

            override fun onFailure(call: Call<List<Annonce>>, t: Throwable) {
                isProgressBarTurning.postValue(false)
                Log.e(TAG, "onFailure : ${t.message}")
            }
        })
        return annoncesList
    }

}
class HomeModelFactory constructor(private val repository: HomeRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeModel::class.java)) {
            HomeModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}