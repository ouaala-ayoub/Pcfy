package com.example.pc.ui.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.ActivityAnnonceBinding
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.utils.toast
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceActivity"
private const val ERROR_TEXT = "test erreur"
private const val SUCCESS_TEXT = "annonce ajoutée au favories avec succes"

class AnnonceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnonceBinding
    private val retrofitService = RetrofitService.getInstance()
    private val viewModel = AnnonceModel(
        AnnonceRepository(
            retrofitService
        )
    )
    private lateinit var userId: String
    private val picasso = Picasso.get()

    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var loginRepository: LoginRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAnnonceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        val id = intent.getSerializableExtra("id") as String

        binding.apply {
            viewModel.apply {

                getAnnonceById(id)

                annonceToShow.observe(this@AnnonceActivity) { annonce ->
                    if (annonceToShow.value != null) {
                        //bind the data to the views

                        //get the sellers name
                        getSellerById(annonce.sellerId)

                        //images first
                        if (annonce.pictures.isNotEmpty()) {
                            picasso
                                .load(annonce.pictures[0])
                                .fit()
                                .into(productImages)
                        }

                        productTitle.text = annonce.title
                        productPrice.text = getString(R.string.price, annonce.price.toInt())
                        productStatus.text = getString(R.string.status, annonce.status)

                        //the seller name
                        seller.observe(this@AnnonceActivity){
                            productSeller.text = it
                        }
                        productDescription.text = annonce.description

                        Log.i(TAG, "annonce = ${annonceToShow.value}")
                    } else {
                        applicationContext.toast(ERROR_TEXT, Toast.LENGTH_SHORT)
                        Log.e(TAG, "error: something went wrong")
                        goToMainActivity()
                    }

                    addToFav.setOnClickListener {
                        if (annonce != null){

                            loginRepository = LoginRepository(
                                retrofitService,
                                this@AnnonceActivity.applicationContext
                            )

                            val isLoggedIn = loginRepository.isLoggedIn
                            isLoggedIn.observe(this@AnnonceActivity){ isLogged ->

                                Log.i(TAG, "isLogged: $isLogged")

                                if (isLogged) {
                                    userId = loginRepository.user!!.userId
                                }

                                else if (!isLogged){
                                    goToLoginActivity()
                                }
                            }


                            addToFavourites(userId, annonce)
                            addedFavouriteToUser.observe(this@AnnonceActivity){
                                if (it)
                                    doOnSuccess()
                                else
                                    doOnFail()
                            }
                        }
                        else {
                            //consider Fail
                            doOnFail()
                        }
                    }

                }

                isProgressBarTurning.observe(this@AnnonceActivity) {
                    annonceProgressBar.isVisible = it
                }

            }
        }
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail() {
        applicationContext.toast(ERROR_TEXT, Toast.LENGTH_SHORT)
        goToMainActivity()
    }

    private fun doOnSuccess() {
        applicationContext.toast(SUCCESS_TEXT, Toast.LENGTH_SHORT)
    }

    private fun goToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

}