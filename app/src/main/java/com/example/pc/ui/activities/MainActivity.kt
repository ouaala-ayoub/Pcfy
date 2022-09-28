package com.example.pc.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.ActivityMainBinding
import com.example.pc.ui.viewmodels.AuthModel
import com.google.android.material.bottomnavigation.BottomNavigationView


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var authModel: AuthModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
//        val requestPermissionLauncher =
//            registerForActivityResult(
//                ActivityResultContracts.RequestPermission()
//            ) { isGranted: Boolean ->
//                if (isGranted) {
//                    Log.i(TAG, "onCreate: permission granted : $isGranted")
//                } else {
//                    Log.i(TAG, "onCreate: permission granted : $isGranted")
//                    // Explain to the user that the feature is unavailable because the
//                    // features requires a permission that the user has denied. At the
//                    // same time, respect the user's decision. Don't link to system
//                    // settings in an effort to convince the user to change their
//                    // decision.
//
//                }
//            }

        setContentView(binding.root)

//        when (PackageManager.PERMISSION_GRANTED) {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) -> {
//                Log.i(TAG, "permission is aleardy granted: ")
//            }
//            //            shouldShowRequestPermissionRationale(...) -> {
//            //            // In an educational UI, explain to the user why your app requires this
//            //            // permission for a specific feature to behave as expected. In this UI,
//            //            // include a "cancel" or "no thanks" button that allows the user to
//            //            // continue using your app without granting the permission.
//            //            showInContextUI(...)
//            //        }
//            else -> {
//                Log.i(TAG, "handlePermission: asking for permission")
//                // You can directly ask for the permission.
//                // The registered ActivityResultCallback gets the result of this request.
//                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//            }
//        }

//        handlePermission()

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

            R.id.website -> {
                openTheWebsite()
                true
            }

            R.id.share -> {
                true
            }

            R.id.login -> {
                goToLoginActivity()
                true
            }

            R.id.logout -> {
                authModel.logout()
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

    private fun openTheWebsite() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(getString(R.string.pcfy_website))
        startActivity(openURL)
    }
}