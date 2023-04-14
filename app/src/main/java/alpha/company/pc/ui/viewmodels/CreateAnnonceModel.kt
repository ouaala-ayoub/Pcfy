package alpha.company.pc.ui.viewmodels

import alpha.company.pc.data.models.network.Category
import android.util.Log
import androidx.lifecycle.*
import alpha.company.pc.data.models.network.IdResponse
import alpha.company.pc.data.repositories.CreateAnnonceRepository
import alpha.company.pc.utils.getError
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//temporary

private const val TAG = "CreateAnnonceModel"

class CreateAnnonceModel(private val createAnnonceRepository: CreateAnnonceRepository) :
    ViewModel() {

    private val errorMessage = MutableLiveData<String>()
    val requestSuccessful = MutableLiveData<Boolean>()
    val categoriesList = MutableLiveData<List<Category>>()
    val citiesList = MutableLiveData<List<String>>()
    private val form = FormData()
    val titleLiveData = form.titleLiveData
    val priceLiveData = form.priceLiveData
    val imagesLiveData = form.imagesLiveData
    val categoryLiveData = form.categoryLiveData
    val subCategoryLiveData = form.subCategoryLiveData
    val citiesLiveData = form.citiesLiveData
    val statusLiveData = form.statusLiveData
    val isTurning = MutableLiveData<Boolean>()
    val isValidInput = form.isValidInput

    //get the user id ??

    fun triggerLoading() {
        isTurning.postValue(true)
    }

    fun getCities() {
        createAnnonceRepository.getCities().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null)
                    citiesList.postValue(response.body())
                else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
    }

    fun getCategories() {
        createAnnonceRepository.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    categoriesList.postValue(response.body())
                } else {
                    val error = getError(response.errorBody()!!, response.code())
                    if (error != null)
                        Log.e(TAG, "getCategories response error $error")
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.e(TAG, "getCategories onFailure : ${t.message}")
            }

        })
    }

    fun addAnnonce(
        annonce: RequestBody,
    ): LiveData<Boolean> {

        isTurning.postValue(true)

        createAnnonceRepository.addAnnonce(
            annonce
        )
            .enqueue(object : Callback<IdResponse> {
                override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                    if (response.isSuccessful && response.body()?.objectId != null) {

                        Log.i(TAG, "addAnnonce response body is ${response.body()}")
                        requestSuccessful.postValue(true)

                    } else {
                        val error = response.errorBody()?.let { getError(it, response.code()) }
                        Log.e(TAG, "onResponse addAnnonce : $error")
                        requestSuccessful.postValue(false)
                    }
                    isTurning.postValue(false)
                }

                override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure addAnnonce ${t.message!!}")
                    errorMessage.postValue(t.message)
                    requestSuccessful.postValue(false)
                    isTurning.postValue(false)
                }
            })

        return requestSuccessful
    }

}

class FormData {
    val titleLiveData = MutableLiveData<String>()
    val priceLiveData = MutableLiveData<String>()
    val imagesLiveData = MutableLiveData<String>()
    val categoryLiveData = MutableLiveData<String>()
    val subCategoryLiveData = MutableLiveData<String>()
    val citiesLiveData = MutableLiveData<String>()
    val statusLiveData = MutableLiveData<String>()

    val isValidInput = MediatorLiveData<Boolean>().apply {
        addSource(titleLiveData) { title ->
            val price = priceLiveData.value
            val images = imagesLiveData.value
            val category = categoryLiveData.value
            val subCategory = subCategoryLiveData.value
            val city = citiesLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(priceLiveData) { price ->
            val title = titleLiveData.value
            val images = imagesLiveData.value
            val category = categoryLiveData.value
            val subCategory = subCategoryLiveData.value
            val city = citiesLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(imagesLiveData) { images ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            val category = categoryLiveData.value
            val subCategory = subCategoryLiveData.value
            val city = citiesLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(categoryLiveData) { category ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            val subCategory = subCategoryLiveData.value
            val images = imagesLiveData.value
            val city = citiesLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(subCategoryLiveData) { subCategory ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            val category = categoryLiveData.value
            val images = imagesLiveData.value
            val city = citiesLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(citiesLiveData) { city ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            val images = imagesLiveData.value
            val category = categoryLiveData.value
            val subCategory = subCategoryLiveData.value
            val status = statusLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
        addSource(statusLiveData) { status ->
            val title = titleLiveData.value
            val price = priceLiveData.value
            val images = imagesLiveData.value
            val category = categoryLiveData.value
            val subCategory = subCategoryLiveData.value
            val city = citiesLiveData.value
            this.value = validateData(title, price, images, category, subCategory, city, status)
        }
    }

//    fun getValues(): List<String> {
//        return listOf(
//            titleLiveData.value.toString(),
//            priceLiveData.value.toString(),
//            imagesLiveData.value.toString(),
//            categoryLiveData.value.toString(),
//            citiesLiveData.value.toString(),
//            statusLiveData.value.toString()
//        )
//    }

    private fun validateData(
        title: String?,
        price: String?,
        images: String?,
        category: String?,
        subCategory: String?,
        city: String?,
        status: String?
    ): Boolean {

        val isValidTitle = !title.isNullOrBlank()
        val isValidPrice = !price.isNullOrBlank()
        val isValidImagesInput = !images.isNullOrBlank()
        val isValidCategory = !category.isNullOrBlank()
        val isValidSubCategory = !subCategory.isNullOrBlank() && subCategory != "-"
        val isValidCity = !city.isNullOrBlank()
        val isValidStatus = !status.isNullOrBlank()

        return isValidTitle && isValidPrice && isValidImagesInput && isValidCategory && isValidSubCategory && isValidCity && isValidStatus
    }
}