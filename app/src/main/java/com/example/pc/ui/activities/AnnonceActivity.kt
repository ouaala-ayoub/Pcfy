package com.example.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.squareup.picasso.Picasso

class AnnonceActivity : AppCompatActivity() {

    val picasso: Picasso = Picasso.get()
    private val retrofitService = RetrofitService.getInstance()
    val viewModel = AnnonceModel(AnnonceRepository(retrofitService))
    val authModel = AuthModel(retrofitService, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        authModel.apply {
            auth(this@AnnonceActivity)
        }
        supportActionBar?.hide()

        setContentView(R.layout.activity_annonce)
    }

}