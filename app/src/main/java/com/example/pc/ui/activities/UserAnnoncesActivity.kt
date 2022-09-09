package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.ActivityUserAnnoncesBinding
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.viewmodels.UserAnnoncesModel
import com.example.pc.utils.toast

private const val TAG = "UserAnnoncesActivity"
private const val ANNONCE_DELETED_SUCCESS = ""
private const val ANNONCE_ERROR_MSG = "Erreur Inatendue"

class UserAnnoncesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAnnoncesBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userId: String
    private lateinit var userAnnoncesModel: UserAnnoncesModel

    override fun onCreate(savedInstanceState: Bundle?) {

        //to change with fragment ?

        binding = ActivityUserAnnoncesBinding.inflate(layoutInflater)
        userId = intent.getStringExtra("id")!!
        userAnnoncesModel = UserAnnoncesModel(
            UserInfoRepository(
                retrofitService,
            )
        )

        super.onCreate(savedInstanceState)

        val adapter = FavouritesAdapter(

            object: FavouritesAdapter.OnFavouriteClickListener{

                override fun onFavouriteClicked(annonceId: String) {
                   goToAnnonceModifyActivity(annonceId)
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


        userAnnoncesModel.apply {
            getAnnoncesById(userId).observe(this@UserAnnoncesActivity) { annonces ->

                if (annonces == null) {
                    this@UserAnnoncesActivity.toast(ANNONCE_ERROR_MSG, Toast.LENGTH_SHORT)
                    returnToUserInfo()
                }

                if (annonces != null) {
                    updateIsEmpty().observe(this@UserAnnoncesActivity) {
                        binding.isEmpty.isVisible = it
                    }
                    Log.i(TAG, "favourites : $annonces")
                    adapter.setList(annonces)
                }
            }
        }

        binding.apply {
            annoncesRv.adapter = adapter
            annoncesRv.layoutManager = LinearLayoutManager(this@UserAnnoncesActivity)

            userAnnoncesModel.isTurning.observe(this@UserAnnoncesActivity){
                userAnnoncesProgressbar.isVisible = it
            }
        }
        setContentView(binding.root)
    }

    private fun returnToUserInfo() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToAnnonceModifyActivity(annonceId: String) {
        val intent = Intent(this, AnnonceModifyActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: activity destroyed")
    }

}