package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.data.models.local.ImageLoader
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.databinding.ActivityMainBinding
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.utils.USERS_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import alpha.company.pc.utils.toast
import android.content.Context
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
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso


private const val TAG = "MainActivity"
val freeUser = User(
    "Non Authentifié",
    "",
    "",
    imageUrl = ""
)
var globalUserObject: User = freeUser
var imageLoader: ImageLoader? = ImageLoader("no yet", LoadPolicy.Cache)

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var authModel: AuthModel
    private var userId: String? = null
    var picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialise mobileAds
        //weird bug
        val retrofitService = RetrofitService.getInstance(this)
        MobileAds.initialize(this)
        authModel = AuthModel(
            retrofitService,
            LoginRepository(this)
        ).apply {
            auth(this@MainActivity)
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
                    Log.d(TAG, "onOptionsItemSelected clicked personal_space ")

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
                    openTheWebsite(getString(R.string.pcfy_website))
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
        //
//        Log.i(TAG, "current theme: $isNightTheme")
//
//
        authModel.apply {
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
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)

            val editor = sharedPreferences.edit()
            user.observe(this@MainActivity) { user ->
                if (user != null) {
                    Log.i(TAG, "onCreateOptionsMenu: logged_in")
                    inflater.inflate(R.menu.logged_in_options_menu, menu)
                    val itemSwitch = menu!!.findItem(R.id.light_dark)
                    itemSwitch.setActionView(R.layout.light_dark_switch)
                    val switch =
                        itemSwitch.actionView?.findViewById<SwitchCompat>(R.id.light_dark_switch)

                    switch?.isChecked =
                        sharedPreferences!!.getBoolean(getString(R.string.dark_mode), false)
                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        Log.d(TAG, "switch: $isChecked")
                        if (isChecked) {
                            editor.putBoolean(getString(R.string.dark_mode), true).apply()
                            updateUI(sharedPreferences, switch)
                        } else {
                            editor.putBoolean(getString(R.string.dark_mode), false).apply()
                            updateUI(sharedPreferences, switch)
                        }
                    }
                    return@observe
                } else {
                    Log.i(TAG, "onCreateOptionsMenu: logged_out")
                    inflater.inflate(R.menu.logged_out_options_menu, menu)
                    val itemSwitch = menu!!.findItem(R.id.light_dark)
                    itemSwitch.setActionView(R.layout.light_dark_switch)
                    val switch =
                        itemSwitch.actionView?.findViewById<SwitchCompat>(R.id.light_dark_switch)
                    switch?.isChecked =
                        sharedPreferences!!.getBoolean(getString(R.string.dark_mode), false)
                    Log.d(TAG, "switch?.isChecked: ${switch?.isChecked}")
                    switch?.setOnCheckedChangeListener { _, isChecked ->
                        Log.d(TAG, "switch: $isChecked")
                        if (isChecked) {
                            editor.putBoolean(getString(R.string.dark_mode), true).apply()
                            updateUI(sharedPreferences, switch)
                        } else {
                            editor.putBoolean(getString(R.string.dark_mode), false).apply()
                            updateUI(sharedPreferences, switch)
                        }
                    }
                    return@observe
                }
            }

        }


        return super.onCreateOptionsMenu(menu)
    }

    private fun updateUI(sharedPreferences: SharedPreferences?, switchCompat: SwitchCompat?) {
        switchCompat.apply {
            val isChecked = sharedPreferences!!.getBoolean(getString(R.string.dark_mode), false)
            Log.d(TAG, "isChecked from updateUI: $isChecked")
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

        return when (item.itemId) {

            R.id.login -> {
                goToLoginActivity()
                true
            }

            R.id.logout -> {
                authModel.logout()
                this.toast(getString(R.string.dissconected), Toast.LENGTH_SHORT)
                reloadActivity()
                true
            }

            R.id.light_dark -> {

                Log.d(TAG, "light_dark: clicked")
//                Log.d(TAG, "light_dark_switch: clicked")
//                val isDark = PreferenceManager.getDefaultSharedPreferences(this)
//                    .getBoolean(getString(R.string.dark_mode), false)
//                Log.d(TAG, "isDark before: $isDark")
//                val test =
//                    this.getSharedPreferences(getString(R.string.dark_mode), Context.MODE_PRIVATE)
//                with(test!!.edit()) {
//                    putBoolean(getString(R.string.dark_mode), !isDark)
//                    commit()
//                }
//
//                when (isDark) {
//
//                    true -> {
//                        Log.i(TAG, "onCreatePreferences: switching to dark mode")
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                    }
//                    else -> {
//                        Log.i(TAG, "onCreatePreferences: switching to light mode")
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                    }
//                }

                true

//                val itemSwitch = menu!!.findItem(R.id.light_dark_switch)
//                itemSwitch.setActionView(R.layout.light_dark_switch)

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