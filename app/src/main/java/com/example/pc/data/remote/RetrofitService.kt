package com.example.pc.data.remote

import com.example.pc.data.models.network.*
import okhttp3.FormBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface RetrofitService {

    // announces

    @DELETE("announces/{id}")
    fun deleteAnnonce(@Path("id") annonceId: String): Call<Annonce>

    @GET("announces")
    fun getAllAnnonces(@Query("c") category: String?, @Query("s") searchBody: String?): Call<List<Annonce>>

    @GET("announces/{id}")
    fun getAnnonceById(@Path("id") annonceId: String): Call<Annonce>

    @FormUrlEncoded
    @POST("announces")
    fun addAnnonce(
        @FieldMap annoncesFields: HashMap<String, String>,
//        @Field("pictures") files: RequestBody
    )
    :Call<IdResponse>

    @GET("users/{id}/announces")
    fun getAnnounces(@Path("id") userId: String): Call<List<Annonce>>

    //handle users admin ?

    @GET("users")
    fun getUsers(): Call<List<User>>

    // auth

    @POST("auth/signin")
    fun login(@Body userCredentials: UserCredentials): Call<Tokens>

    @POST("auth/refresh")
    fun getAccessToken(@Body refreshToken: RefreshToken): Call<AccessToken>

    @POST("auth")
    fun auth(@Body tokens: Tokens): Call<AuthBody?>


    //users
    @GET("users/{id}")
    fun getUserById(@Path("id") userId: String): Call<User>

    @POST("users")
    fun addUser(@Body user: User): Call<IdResponse>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") userId: String): Call<IdResponse>

    @PUT("users/{id}") //????
    fun updateAnnonces(@Path("id") userId: String, @Body annonceToAddId: NewAnnonceRequest): Call<User>

    //favourites
//    ???

    @GET("users/{id}/favourites")
    fun getFavourites(@Path("id") userId: String): Call<List<Annonce>>

    @PUT("users/{id}")
    fun updateFavourites(@Path("id") userId: String, @Body favouriteToAddId: NewFavouritesRequest): Call<User>

    @PUT("users/{id}")
    fun updateUserInfo(@Path("id") userId: String, @Body newUser: User): Call<User>

    @PUT("announces/{id}")
    fun updateAnnonceInfo(@Path("id") annonceId: String, @Body newAnnonce: Annonce): Call<Annonce>


    companion object {

        //to learn
        private const val BASE_URL = "https://pcfy.vercel.app/api/"
        private var retrofitService: RetrofitService? = null

        fun getInstance() : RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}