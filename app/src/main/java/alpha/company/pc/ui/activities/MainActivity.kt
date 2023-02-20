package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.data.models.local.ImageLoader
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.databinding.ActivityMainBinding
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.utils.USERS_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import alpha.company.pc.utils.toast
import android.content.Intent
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
const val LOGOUT_SUCCESS = "Deconnecté avec success"
var imageLoader: ImageLoader? = ImageLoader("no yet", LoadPolicy.Cache)
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private var userId: String? = null
    private lateinit var authModel: AuthModel
    var picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialise mobileAds
        //weird bug
        MobileAds.initialize(this)
        authModel = AuthModel(
            retrofitService,
            LoginRepository(retrofitService, this)
        ).apply { auth(this@MainActivity) }

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

                    if (userId != null){
                        goToPersonalSpaceActivity(userId!!)
                    } else {
                        goToLoginActivity()
                    }

                }
                R.id.website -> {
                    openTheWebsite()
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
            auth.observe(this@MainActivity) {
                val view = binding.navView.getHeaderView(0)
                val userImage = view.findViewById<ImageView>(R.id.user_picture)
                val userName = view.findViewById<TextView>(R.id.user_name)
                val userEmail = view.findViewById<TextView>(R.id.user_email)
                userId = getUserId()

                if (isAuth()) {
                    getUserById(userId!!)
                    user.observe(this@MainActivity) { user ->
                        if (user != null) {
                            val imageName = user.imageUrl
                            val circularProgressDrawable = circularProgressBar(this@MainActivity)

                            if (imageName != null) {
                                imageLoader?.imageUrl = imageName
                            }
                            if (imageName.isNullOrBlank()){
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
                                        user.imageUrl
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

                            userImage.setOnClickListener {
                                goToLoginActivity()
                            }
                            userName.text = getString(R.string.error)
                            userEmail.text = getString(R.string.error)

                        }
                    }
                } else {
                    userImage.setOnClickListener {
                        goToLoginActivity()
                    }
                    userName.text = getString(R.string.no_auth)
                    userEmail.text = getString(R.string.no_auth)
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
//                supportActionBar?.setBackgroundDrawable(
//                    ColorDrawable(
//                        (ContextCompat.getColor(
//                            this,
//                            R.color.white_darker
//                        ))
//                    )
//                )
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            true -> {
//                supportActionBar?.setBackgroundDrawable(
//                    ColorDrawable(
//                        (ContextCompat.getColor(
//                            this,
//                            R.color.even_darker_grey
//                        ))
//                    )
//                )
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

    private fun openTheWebsite() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(getString(R.string.pcfy_website))
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