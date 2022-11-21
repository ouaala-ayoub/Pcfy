package com.example.pc.ui.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.pc.R
import com.example.pc.data.models.local.ImageLoader
import com.example.pc.data.models.local.LoadPolicy
import com.example.pc.data.models.network.Message
import com.example.pc.data.models.network.MessageX
import com.example.pc.data.models.network.Notification
import com.example.pc.data.remote.Name
import com.example.pc.data.remote.RetrofitNotificationService
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.ActivityMainBinding
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.utils.toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.installations.remote.TokenResult.builder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "MainActivity"
const val LOGOUT_SUCCESS = "Deconnecté avec success"
var imageLoader: ImageLoader? = ImageLoader("no yet", LoadPolicy.Cache)

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private val retrofitService = RetrofitService.getInstance()
    var picasso: Picasso = Picasso.get()
    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        picasso = Picasso.get()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authModel = AuthModel(
            retrofitService,
            LoginRepository(retrofitService, this)
        )
        authModel.auth(this@MainActivity)

        bottomNav = findViewById(R.id.bottom_nav)
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHost.navController

        bottomNav.setupWithNavController(navController)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isNightTheme = prefs.getBoolean(getString(R.string.dark_mode), false)

        Log.i(TAG, "current theme: $isNightTheme")

//        var token: String = ""
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(object: OnCompleteListener<String>{
//            override fun onComplete(task: Task<String>) {
//                if(!task.isSuccessful) return
//
//                token = task.result
//                Log.i(TAG, "onComplete task: $token")
//                val message = Message(
//                    MessageX(
//                        Notification(
//                            "test",
//                            "test message"
//                        ),
//                        token
//                    )
//                )
//
//                handleMessage(message)
//            }
//        })



        when (isNightTheme) {
            false -> {
                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        (ContextCompat.getColor(
                            this,
                            R.color.white_darker
                        ))
                    )
                )
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            true -> {
                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        (ContextCompat.getColor(
                            this,
                            R.color.even_darker_grey
                        ))
                    )
                )
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater

        authModel.apply {
            auth.observe(this@MainActivity) {
                if (isAuth()) {
                    Log.i(TAG, "onCreateOptionsMenu: logged_in")
                    inflater.inflate(R.menu.logged_in_options_menu, menu)
                    return@observe
                } else {
                    Log.i(TAG, "onCreateOptionsMenu: logged_out")
                    inflater.inflate(R.menu.logged_out_options_menu, menu)
                    return@observe
                }
            }
        }



        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.login -> {
                goToLoginActivity()
                true
            }

            R.id.logout -> {
                authModel.logout()
                this.toast(LOGOUT_SUCCESS, Toast.LENGTH_SHORT)
                reloadActivity()
                true
            }

            R.id.settings -> {
                goToSettingsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun reloadActivity() {
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun handleMessage(message: Message) {

        Log.i(TAG, "handleMessage: $message")
        
        RetrofitNotificationService.getInstance().sendMessage(message).enqueue(object: Callback<Name>{
            override fun onResponse(call: Call<Name>, response: Response<Name>) {
                Log.i(TAG, "sendMessage onResponse: ${response.body()?.name}")
            }

            override fun onFailure(call: Call<Name>, t: Throwable) {
                Log.i(TAG, "sendMessage onFailure: ${t.message}")
            }

        })
    }

}