package com.example.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.pc.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val navHost = supportFragmentManager.findFragmentById(R.id.login_fragment_container) as NavHostFragment
        val navController = navHost.navController

    }
}