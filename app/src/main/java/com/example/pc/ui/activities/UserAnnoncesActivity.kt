package com.example.pc.ui.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.R
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.ActivityUserAnnoncesBinding
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.viewmodels.UserAnnoncesModel
import com.example.pc.utils.toast

private const val TAG = "UserAnnoncesActivity"
private const val ANNONCE_DELETED_SUCCESS = ""
private const val ANNONCE_ERROR_MSG = ""

class UserAnnoncesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAnnoncesBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userId: String
    private lateinit var userAnnoncesModel: UserAnnoncesModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        //to change with fragment ?

        binding = ActivityUserAnnoncesBinding.inflate(layoutInflater)
        userId = intent.getStringExtra("id")!!
        userAnnoncesModel = UserAnnoncesModel(
            UserInfoRepository(
                retrofitService,
            ),
            LoginRepository(
                retrofitService,
                this.applicationContext
            )
        )

        super.onCreate(savedInstanceState)

        val adapter = FavouritesAdapter(
            object: FavouritesAdapter.OnFavouriteClickListener{

                override fun onFavouriteClicked(annonceId: String) {
//                    go to info annonce info change
//                    ??????????????
                }

                override fun onDeleteClickListener(annonceId: String) {
                    //to do
                    userAnnoncesModel.deleteAnnonce(userId, annonceId).observe(this@UserAnnoncesActivity){deletedWithSuccess ->
                        if(deletedWithSuccess) {
                            baseContext.toast(ANNONCE_DELETED_SUCCESS, Toast.LENGTH_SHORT)
                        }
                        else {
                            baseContext.toast(ANNONCE_ERROR_MSG, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        )
        adapter.setFavouritesList(listOf(
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
            Annonce("test",155, mutableListOf(),"test", "pc gamer", "neuf"),
        ))
        binding.annoncesRv.adapter = adapter
        binding.annoncesRv.layoutManager = LinearLayoutManager(this)

        userAnnoncesModel.isTurning.observe(this){
            binding.userAnnoncesProgressbar.isVisible = it
        }
        setContentView(binding.root)
    }

    private fun goToAnnonceActivity(userId: String) {
        val intent = Intent(this, AnnonceActivity::class.java)
        intent.putExtra("id", userId)
        startActivity(intent)
    }
}