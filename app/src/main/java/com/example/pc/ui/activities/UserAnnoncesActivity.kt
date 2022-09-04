package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pc.R
import com.example.pc.databinding.ActivityUserAnnoncesBinding
import com.example.pc.databinding.FragmentFavouritesBinding
import com.example.pc.databinding.NoUserConnectedBinding

private const val TAG = "UserAnnoncesActivity"

class UserAnnoncesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAnnoncesBinding
    private lateinit var bindingNoUser: NoUserConnectedBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityUserAnnoncesBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)

        if(null == null){
            bindingNoUser = NoUserConnectedBinding.inflate(layoutInflater)
            bindingNoUser.loginFromUserInfo.setOnClickListener {
                goToLoginActivity()
            }
            setContentView(bindingNoUser.root)

        }else {
            binding = ActivityUserAnnoncesBinding.inflate(layoutInflater)
            setContentView(binding.root)
        }


    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}