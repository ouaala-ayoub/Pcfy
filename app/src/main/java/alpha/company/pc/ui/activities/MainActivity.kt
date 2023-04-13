package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.data.models.local.ImageLoader
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.databinding.ActivityMainBinding
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.utils.*
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso


private const val TAG = "MainActivity"
var imageLoader: ImageLoader? = ImageLoader("no yet", LoadPolicy.Cache)

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var authModel: AuthModel
    private var userId: String? = null
    private var errorMessage: String? = null
    var picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialise mobileAds
        //weird bug
        val retrofitService = RetrofitService.getInstance(this)
//        MobileAds.initialize(this)
        authModel = AuthModel(
            retrofitService,
            LoginRepository(this)
        ).apply {
            auth()
        }
//        picasso = Picasso.get()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.personal_space -> {
                    if (userId != null) {
                        goToPersonalSpaceActivity(userId!!)
                    } else {
                        goToLoginActivity()
                    }
                }
                R.id.settings -> {
                    goToSettingsActivity()
                }
                R.id.website -> {
                    openTheWebsite(getString(R.string.pcfy_website))
                }
                R.id.about -> {
                    openTheWebsite(getString(R.string.pcfy_website_regles))
                }
                R.id.abonnements -> {
                    if (userId != null) {
                        goToSubscriptionsActivity(userId!!)
                    } else {
                        goToLoginActivity()
                    }
                }
                R.id.email -> {
                    val email = getString(R.string.pcfy_customer_service_email)
                    openEmailSending(email)
                }
                R.id.instagram -> {
                    val appIns = getString(R.string.pcfy_instagram)
                    openInstagramPage(appIns)
                }
                R.id.twitter -> {
                    val twtUrl = getString(R.string.pcfy_twitter)
                    openTwitterPage(twtUrl)
                }
            }
            true
        }

        bottomNav = findViewById(R.id.bottom_nav)
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHost.navController

        bottomNav.setupWithNavController(navController)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                (ContextCompat.getColor(
                    this,
                    R.color.main_theme
                ))
            )
        )
        when (prefs.getBoolean(getString(R.string.dark_mode), false)) {
            false -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            true -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        authModel.apply {
            errorMessage.observe(this@MainActivity) { error ->
                this@MainActivity.errorMessage = error
            }

            user.observe(this@MainActivity) { user ->
                val view = binding.navView.getHeaderView(0)
                val userImage = view.findViewById<ImageView>(R.id.user_picture)
                val userName = view.findViewById<TextView>(R.id.user_name)
                val userEmail = view.findViewById<TextView>(R.id.user_email)

                if (user != null) {
                    userId = user.userId
                    val imageName = user.imageUrl
                    val circularProgressDrawable = circularProgressBar(this@MainActivity)

                    if (imageName != null) {
                        imageLoader?.imageUrl = imageName
                    }
                    if (imageName.isNullOrBlank()) {
                        userImage.setOnClickListener {
                            goToUserImageModify(
                                user.userId!!,
                                "undefined"
                            )
                        }
                    } else {
                        userImage.setOnClickListener {
                            Log.d(TAG, "userImage.setOnClickListener: ")
                            goToUserImageModify(
                                user.userId!!,
                                imageName
                            )
                        }
                    }

                    if (imageLoader?.loadingPolicy == LoadPolicy.Cache) {
                        Log.i(TAG, "loading image with cache")
                        loadUserImageFromCache(
                            circularProgressDrawable,
                            imageLoader!!.imageUrl,
                            userImage
                        )
                    } else if (imageLoader?.loadingPolicy == LoadPolicy.Reload) {
                        Log.i(TAG, "loading image with no cache")
                        loadUserImageNoCache(
                            circularProgressDrawable,
                            imageLoader!!.imageUrl,
                            userImage
                        )
                        imageLoader?.loadingPolicy = LoadPolicy.Cache
                    }

                    userName.text = user.name
                    userEmail.text = user.email
                } else {
                    userImage.apply {
                        setImageResource(R.drawable.ic_baseline_no_photography_24)
                        setOnClickListener {
                            goToLoginActivity()
                        }
                    }
                    userName.text = getString(R.string.no_auth)
                    userEmail.text = ""

                }
            }
        }

    }

    private fun openAppWithPackage(pkg: String, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.setPackage("com.twitter.android")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "openAppWithPackage $pkg: ${e.message}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }

    private fun openTwitterPage(twtUrl: String) {
        openAppWithPackage("com.twitter.android", twtUrl)
    }

    private fun openInstagramPage(appIns: String) {
        openAppWithPackage("com.instagram.android", appIns)
    }

    private fun openEmailSending(email: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.apply {
            putExtra(Intent.EXTRA_EMAIL, email)
            putExtra(Intent.EXTRA_SUBJECT, "feedback")
            type = "message/rfc822"
        }
        startActivity(Intent.createChooser(intent, "Send Email using:"))
    }

    private fun goToSubscriptionsActivity(userId: String) {
        val intent = Intent(this, SubscriptionsActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun goToPersonalSpaceActivity(userId: String) {
        val intent = Intent(this, PersonalSpaceActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun goToUserImageModify(loggedInUser: String, imageName: String) {
        val intent = Intent(this, UserImageModifyActivity::class.java)
        intent.putExtra("userId", loggedInUser)
        intent.putExtra("imageName", imageName)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater

        authModel.apply {
            inflater.inflate(R.menu.main_bar_menu, menu)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            val userStatus = menu!!.findItem(R.id.user_status)

            handleThemeSwitch(menu, sharedPreferences)

            user.observe(this@MainActivity) { user ->

                if (user != null) {
                    userStatus.title = getString(R.string.logout)
                    return@observe
                } else {
                    userStatus.title = getString(R.string.se_connecter)
                    return@observe
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun handleThemeSwitch(menu: Menu?, sharedPreferences: SharedPreferences?) {
        val editor = sharedPreferences?.edit()
        val itemSwitch = menu!!.findItem(R.id.light_dark)
        itemSwitch.setActionView(R.layout.light_dark_switch)
        val switch =
            itemSwitch.actionView?.findViewById<SwitchCompat>(R.id.light_dark_switch)
        switch?.isChecked =
            sharedPreferences!!.getBoolean(getString(R.string.dark_mode), false)
        switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor?.putBoolean(getString(R.string.dark_mode), true)?.apply()
                updateUI(sharedPreferences, switch)
            } else {
                editor?.putBoolean(getString(R.string.dark_mode), false)?.apply()
                updateUI(sharedPreferences, switch)
            }
        }
    }

    private fun updateUI(sharedPreferences: SharedPreferences?, switchCompat: SwitchCompat?) {
        switchCompat.apply {
            val isChecked = sharedPreferences!!.getBoolean(getString(R.string.dark_mode), false)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val view = binding.navView.getHeaderView(0)
        val userImage = view.findViewById<ImageView>(R.id.user_picture)
        val circularProgressDrawable = circularProgressBar(this)

        if (imageLoader?.loadingPolicy == LoadPolicy.Cache) {
            Log.i(TAG, "onResume loading image with cache")
            loadUserImageFromCache(
                circularProgressDrawable,
                imageLoader!!.imageUrl,
                userImage
            )
        } else if (imageLoader?.loadingPolicy == LoadPolicy.Reload) {
            Log.i(TAG, "onResume loading image with no cache")
            loadUserImageNoCache(
                circularProgressDrawable,
                imageLoader!!.imageUrl,
                userImage
            )
            imageLoader?.loadingPolicy = LoadPolicy.Cache
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "actionBarDrawerToggle.onOptionsItemSelected: ")
            return true
        }

        return when (item.title) {

            getString(R.string.se_connecter) -> {
                goToLoginActivity()
                true
            }

            getString(R.string.logout) -> {
                Firebase.messaging.token.addOnCompleteListener { task ->
                    val token = task.result
                    if (userId != null) {
                        authModel.logout(userId!!, token)
                        this.toast(getString(R.string.dissconected), Toast.LENGTH_SHORT)
                        reloadActivity()
                    }
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun auth() {
        authModel.auth()
    }

    fun isAuthRequestTimeout(): Boolean = errorMessage == REQUEST_TIME_OUT

    private fun goToSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun openTheWebsite(link: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(link)
        startActivity(openURL)
    }

    private fun reloadActivity() {
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun loadUserImageNoCache(
        circularProgressDrawable: CircularProgressDrawable,
        imageName: String,
        imageView: ImageView
    ) {
        picasso
            .load("$USERS_AWS_S3_LINK$imageName")
            .fit()
            .placeholder(circularProgressDrawable)
            .error(R.drawable.ic_baseline_no_photography_24)
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .into(imageView)
    }

    private fun loadUserImageFromCache(
        circularProgressDrawable: CircularProgressDrawable,
        imageName: String,
        imageView: ImageView
    ) {
        picasso
            .load("$USERS_AWS_S3_LINK$imageName")
            .fit()
            .error(R.drawable.ic_baseline_no_photography_24)
            .placeholder(circularProgressDrawable)
            .into(imageView)
    }

}