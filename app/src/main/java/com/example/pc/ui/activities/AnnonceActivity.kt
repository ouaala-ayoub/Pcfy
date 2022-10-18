package com.example.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pc.R
import com.squareup.picasso.Picasso

class AnnonceActivity : AppCompatActivity() {

    val picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_annonce)
    }

}