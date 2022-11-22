package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.R
import com.example.pc.data.models.local.OrderStatus
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Order
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.ActivityUserAnnoncesBinding
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.adapters.OrdersShortAdapter
import com.example.pc.ui.viewmodels.UserAnnoncesModel
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
import com.example.pc.utils.toast

private const val TAG = "UserAnnoncesActivity"
private const val ANNONCE_DELETED_SUCCESS = "Annonce suprimée avec succès"
private const val ANNONCE_ERROR_MSG = "Erreur inattendue"

class UserAnnoncesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAnnoncesBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userId: String
    private lateinit var userAnnoncesModel: UserAnnoncesModel
    private lateinit var currentTokens: Tokens

    override fun onCreate(savedInstanceState: Bundle?) {

        //to change with fragment ?

        binding = ActivityUserAnnoncesBinding.inflate(layoutInflater)
        currentTokens = LocalStorage.getTokens(this)
        userId = intent.getStringExtra("id")!!
        userAnnoncesModel = UserAnnoncesModel(
            UserInfoRepository(
                retrofitService,
            )
        )

        super.onCreate(savedInstanceState)

        val adapter = FavouritesAdapter(

            object : FavouritesAdapter.OnFavouriteClickListener {

                override fun onFavouriteClicked(annonceId: String) {
                    goToAnnonceModifyActivity(annonceId)
                }

                override fun onDeleteClickListener(annonceId: String) {
                    //to do
                    makeDialog(
                        this@UserAnnoncesActivity,
                        object : OnDialogClicked {
                            override fun onPositiveButtonClicked() {
                                userAnnoncesModel.apply {
                                    //delete then observe the deleted Boolean
                                    deleteAnnonce(currentTokens, annonceId)

                                    deletedAnnonce.observe(this@UserAnnoncesActivity) { deletedWithSuccess ->
                                        if (deletedWithSuccess) {
                                            getAnnoncesById(userId)
                                            this@UserAnnoncesActivity.toast(
                                                ANNONCE_DELETED_SUCCESS,
                                                Toast.LENGTH_SHORT
                                            )
                                        } else {
                                            this@UserAnnoncesActivity.toast(
                                                ANNONCE_ERROR_MSG,
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                                }
                            }

                            override fun onNegativeButtonClicked() {}
                        },
                        getString(R.string.annonce_delete_dialog_title),
                        getString(R.string.annonce_delete_dialog_message)
                    ).show()
                }
            },
            object : FavouritesAdapter.OnCommandsClicked {
                override fun onCommandClicked(annonceId: String, adapter: OrdersShortAdapter) {
                    //send the request
                    Log.i(TAG, "onCommandClicked: $annonceId")
                    userAnnoncesModel.apply {
                        getAnnonceOrders(annonceId)
                        ordersList.observe(this@UserAnnoncesActivity) { orders ->
                            Log.i(TAG, "onCommandClicked orders: $orders")
                            if (orders != null) {
                                adapter.setOrdersList(orders)
                            } else {
                                this@UserAnnoncesActivity.toast(
                                    "Erreur de chargement des commandes",
                                    Toast.LENGTH_SHORT
                                )
                            }
                        }
                    }
                }
            },
            object : OrdersShortAdapter.OnOrderClicked {
                override fun onOrderClicked(orderId: String) {
                    // go to order page

//                    goToOrderPage( orderId)
                }
            }
        )


        userAnnoncesModel.apply {
            getAnnoncesById(userId).observe(this@UserAnnoncesActivity) { annonces ->

                if (annonces == null) {
                    this@UserAnnoncesActivity.toast(ANNONCE_ERROR_MSG, Toast.LENGTH_SHORT)
                    returnToUserInfo()
                } else {
                    updateIsEmpty().observe(this@UserAnnoncesActivity) {
                        binding.isEmpty.isVisible = it
                    }
                    Log.i(TAG, "annonces : $annonces")
                    adapter.setList(annonces)
                }
            }
        }

        binding.apply {
            annoncesRv.adapter = adapter
            annoncesRv.layoutManager = LinearLayoutManager(this@UserAnnoncesActivity)

            userAnnoncesModel.isTurning.observe(this@UserAnnoncesActivity) {
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

}