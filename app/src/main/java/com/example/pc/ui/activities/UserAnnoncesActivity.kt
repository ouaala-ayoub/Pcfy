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


class UserAnnoncesActivity : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //to change with fragment ?
        userId = intent.getStringExtra("id")!!


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_annonces)
    }


}