package com.example.pc.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserRepository
import com.example.pc.databinding.ActivityCreateUserBinding
import com.example.pc.databinding.ActivityUserCreateBinding
import com.example.pc.ui.adapters.FragmentsAdapter
import com.example.pc.ui.fragments.UserStepOne
import com.example.pc.ui.fragments.UserStepThree
import com.example.pc.ui.fragments.UserStepTwo
import com.example.pc.ui.viewmodels.UserModel
import com.example.pc.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


private const val TAG = "UserCreateActivity"
private const val SGN_SUCCESS = "Compte Créé avec succès"
private const val SGN_FAILED = "Erreur lors du creation du compte"
private const val IMAGE_NOT_SELECTED = "Aucune image selectionnée"
private const val IMAGE_SELECTED = "Une image selectionnée"

class UserCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var fragmentsList: List<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {

        //hiding the support bar
        supportActionBar?.hide()

        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        fragmentsList = listOf(
            UserStepOne(),
            UserStepTwo(),
            UserStepThree()
        )

        super.onCreate(savedInstanceState)

        binding.apply {
            fragmentsViewPager.apply {
                adapter = FragmentsAdapter(
                    this@UserCreateActivity,
                    fragmentsList
                )
                isUserInputEnabled = false

                next.setOnClickListener {
                    if (currentItem == fragmentsList.lastIndex) {
                        // submit logic
                    } else {
                        currentItem++
                    }
                }
                back.setOnClickListener {
                    if (currentItem == 0) {
                        makeDialog(
                            this@UserCreateActivity,
                            object : OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    this@UserCreateActivity.onBackPressed()
                                }

                                override fun onNegativeButtonClicked() {}
                            },
                            getString(R.string.quit_signup_title),
                            getString(R.string.quit_signup_message)
                        ).show()
                    } else {
                        currentItem--
                    }
                }

            }


            setContentView(binding.root)
        }

    }

    class ImageInfo(val imageName: String, val imageReqBody: RequestBody)

}
