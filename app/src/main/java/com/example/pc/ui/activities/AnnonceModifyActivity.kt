package com.example.pc.ui.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceModifyRepository
import com.example.pc.databinding.ActivityAnnonceModifyBinding
import com.example.pc.ui.viewmodels.AnnonceModifyModel
import com.squareup.picasso.Picasso

class AnnonceModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnonceModifyBinding
    val viewModel =
        AnnonceModifyModel(AnnonceModifyRepository(RetrofitService.getInstance()))
    val picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding the action bar
        supportActionBar?.hide()
        binding = ActivityAnnonceModifyBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

}
