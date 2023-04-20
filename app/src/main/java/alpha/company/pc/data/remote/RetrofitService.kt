package alpha.company.pc.data.remote

import alpha.company.pc.data.models.local.OrderStatusRequest
import alpha.company.pc.data.models.local.TokenRequest
import alpha.company.pc.data.models.network.*
import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface RetrofitService {

    @GET("ayoub")
    fun test(): Call<ResponseBody>

    //cities
    @GET("cities")
    fun getCities(): Call<List<String>>

    // numPages
    @GET("announces/pages")
    fun getNumPages(): Call<InfoResponse>

    //    @HTTP(method = "DELETE", path = "announces/{id}", hasBody = true)
    @DELETE("announces/{id}")
    fun deleteAnnonce(
        @Path("id") annonceId: String
    ): Call<IdResponse>


    @GET("announces/categories")
    fun getCategories(): Call<List<Category>>

    @GET("announces")
    fun getAllAnnonces(
        @Query("c") category: String? = null,
        @Query("s") searchBody: String? = null,
        @Query("p") price: Number? = null,
        @Query("st") status: String? = null,
        @Query("l") limit: Number? = null,
        @Query("n") filterByDate: String? = null,
        @Query("v") visited: String? = null,
        @Query("pg") page: String? = null
    ): Call<AnnoncesResponse>

    @GET("announces/{id}")
    fun getAnnonceById(@Path("id") annonceId: String): Call<Annonce>

    @POST("announces")
    fun addAnnonce(
        @Body annonceToAdd: RequestBody,
    ): Call<IdResponse>

    @GET("users/{id}/announces")
    fun getAnnounces(@Path("id") userId: String): Call<List<Annonce>>

    @PUT("announces/{id}")
    fun updateAnnonceInfo(
        @Path("id") annonceId: String,
        @Body newAnnonce: RequestBody
    ): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "announces/{id}/pictures", hasBody = true)
    fun deleteAnnonceImage(
        @Path("id") annonceId: String,
        @Body imageIndex: RequestBody
    ): Call<ResponseBody>

    @PATCH("announces/{id}/pictures")
    fun changePicture(
        @Path("id") annonceId: String,
        @Body imageToChange: RequestBody
    ): Call<ResponseBody>

//    @PUT("announces/{id}/pictures")
//    fun updatePictures(
//        @Path("id") annonceId: String,
//        @Body updatedPictures: RequestBody
//    ): Call<ResponseBody>

    //handle users admin ?
//
//    @GET("users")
//    fun getUsers(): Call<List<User>>

    // Auth

    @POST("auth/login")
    fun login(@Body userCredentials: UserCredentials): Call<CustomMessageResponse>

    @GET("auth")
    fun auth(): Call<User?>

    @POST("auth/refresh")
    fun refresh(
        @Body refreshToken: RefreshToken
    ): Call<AccessToken>

    // Users

    @GET("users/{id}")
    fun getUserById(@Path("id") userId: String): Call<User>

    @POST("users")
    fun addUser(@Body user: RequestBody): Call<IdResponse>

    @PUT("users/{id}")
    fun changeUserShippingInfos(
        @Path("id") userId: String,
        @Body userShippingInfos: UserShippingInfos
    ): Call<User>

    @PATCH("users/{id}/password")
    fun changePassword(
        @Path("id") userId: String,
        @Body passwords: PasswordRequest
    ): Call<ResponseBody>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") userId: String): Call<IdResponse>

    @PUT("users/{id}")
    fun updateUserInfo(@Path("id") userId: String, @Body newUser: User): Call<User>

    //update user profile picture
    @PATCH("users/{id}/profile")
    fun updateProfilePicture(
        @Path("id") userId: String,
        @Body newUserPicture: RequestBody
    ): Call<IdResponse>

    @PUT("users/{id}")
    fun putFireBaseToken(@Path("id") userId: String, @Body token: TokenRequest): Call<User>

    @HTTP(method = "DELETE", path = "users/{id}/profile", hasBody = true)
    fun deleteProfilePicture(
        @Path("id") userId: String,
        @Body tokens: RequestBody
    ): Call<ResponseBody>

    // Favourites

    @GET("users/{id}/favourites")
    fun getFavourites(@Path("id") userId: String): Call<List<Annonce>>

    @HTTP(method = "DELETE", path = "users/{id}/favourites", hasBody = true)
    fun deleteFavourite(
        @Path("id") userId: String,
        @Body favouriteId: NewFavouritesRequest
    ): Call<ResponseBody>

    @PATCH("users/{id}/favourites")
    fun addFavourite(
        @Path("id") userId: String,
        @Body favouriteId: NewFavouritesRequest
    ): Call<ResponseBody>

    // Orders

    @GET("users/{id}/orders")
    fun getSellerOrders(@Path("id") userId: String): Call<List<Order>>

    @GET("users/{id}/requests")
    fun getUserRequests(@Path("id") userId: String): Call<List<Order>>

    //maybe i will not use it
    @GET("orders")
    fun getOrders(): Call<List<Order>>

    @GET("announces/{id}/orders")
    fun getAnnonceOrders(@Path("id") annonceId: String): Call<List<Order>>

    //add order
    @POST("orders")
    fun addOrder(@Body orderToAdd: Order): Call<IdResponse>

    @GET("orders/{id}")
    fun getOrderById(@Path("id") orderId: String): Call<Order>

    @PUT("orders/{id}")
    fun changeOrderStatus(
        @Path("id") orderId: String,
        @Body orderStatus: OrderStatusRequest
    ): Call<IdResponse>

    //to change
    @DELETE("orders/{id}")
    fun deleteOrderById(@Path("id") orderToDeleteId: String): Call<IdResponse>

    //Demands
    @GET("demands")
    fun getDemands(@Query("s") searchBody: String? = null): Call<List<Demand>>

    @GET("demands/{id}")
    fun getDemandById(@Path("id") demandId: String): Call<Demand>

    @PUT("demands/{id}")
    fun updateDemand(
        @Path("id") demandId: String,
        @Body demand: RequestBody
    ): Call<CustomMessageResponse>

    @POST("demands")
    fun addDemand(@Body requestBody: RequestBody): Call<IdResponse>

    @GET("users/{id}/demands")
    fun getUserDemands(@Path("id") userId: String): Call<List<Demand>>

    @DELETE("demands/{id}")
    fun deleteDemand(@Path("id") demandId: String): Call<IdResponse>

    //tokens
    @GET("users/{id}/tokens")
    fun getUserTokens(@Path("id") userId: String): Call<List<String>>

    @PATCH("users/{id}/tokens")
    fun addFireBaseToken(
        @Path("id") userId: String,
        @Body tokenToAdd: TokenRequest
    ): Call<ResponseBody>

    @DELETE("users/{id}/tokens/{tokenToDelete}")
    fun deleteFireBaseToken(
        @Path("id") userId: String,
        @Path("tokenToDelete") tokenToDelete: String
    ): Call<ResponseBody>

    companion object {

        //to learn
        private const val BASE_URL = "https://pcfy.vercel.app/api/"
        private var retrofitService: RetrofitService? = null

        fun getInstance(context: Context): RetrofitService {

            val client = OkHttpClient
                .Builder()
                .addInterceptor(AddCookiesInterceptor(context))
                .addInterceptor(ReceivedCookiesInterceptor(context))


            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client.build())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}

data class CustomMessageResponse(
    @SerializedName("message")
    val message: String
)