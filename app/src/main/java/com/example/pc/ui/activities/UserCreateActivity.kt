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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserRepository
import com.example.pc.databinding.ActivityUserCreateBinding
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

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: ActivityUserCreateBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var alertDialog: AppCompatDialog? = null
    private val viewModel = UserModel(
        UserRepository(
            RetrofitService.getInstance()
        )
    )
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.hide()

        binding = ActivityUserCreateBinding.inflate(layoutInflater)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

                Log.i(TAG, "isGranted: $isGranted")

                if (isGranted) {
                    setTheUploadImage()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                    val snackBar = makeSnackBar(
                        binding.root,
                        getString(R.string.permission),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar.setAction(R.string.ok) {
                        snackBar.dismiss()
                    }.show()

                }
            }

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    Log.i(TAG, "resultLauncher: ${data?.data}")
                    updateImageText(data?.clipData?.itemCount)
                    imageUri = data?.data
                }
            }


        super.onCreate(savedInstanceState)


        setUpTheTypeEditText()

        binding.apply {
            imageName.text = IMAGE_NOT_SELECTED
            signUpButton.isEnabled = false

            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    nameLiveData.value = text.toString()
                    nameHelperText.observe(this@UserCreateActivity) {
                        nameTextField.helperText = it
                    }
                }
            }

            phoneEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    phoneLiveData.value = text.toString()
                    phoneHelperText.observe(this@UserCreateActivity) {
                        phoneTextField.helperText = it
                    }
                }
            }

            emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    emailLiveData.value = text.toString()
                    emailHelperText.observe(this@UserCreateActivity) {
                        emailTextField.helperText = it
                    }
                }
            }

            passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    passwordLiveData.value = text.toString()
                    passwordHelperText.observe(this@UserCreateActivity) {
                        passwordTextField.helperText = it
                    }
                }
            }

            retypePasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    retypedPasswordLiveData.value = text.toString()
                    retypedPasswordHelperText.observe(this@UserCreateActivity) {
                        retypePasswordTextField.helperText = it
                    }
                }
            }


            imageSelection.setOnClickListener {

                when {
                    ContextCompat.checkSelfPermission(
                        this@UserCreateActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        setTheUploadImage()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this@UserCreateActivity, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected. In this UI,
                        // include a "cancel" or "no thanks" button that allows the user to
                        // continue using your app without granting the permission.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: true")
                        showInContextUI(
                            object : OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }

                                override fun onNegativeButtonClicked() {
                                    //cancel the dialog without doing nothing
                                }
                            }
                        )
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: false")
                        Log.i(TAG, "request Permission Launcher ")
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }



            signUpButton.setOnClickListener {
                // to add a dialog ??
                alertDialog = makeDialog(
                    this@UserCreateActivity,
                    object : OnDialogClicked {
                        override fun onPositiveButtonClicked() {

                            val imageBody = imageUri?.let { it1 -> getImagesRequestBody(it1) }

                            viewModel.apply {
                                val builder = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("name", binding.nameEditText.text.toString())
                                    .addFormDataPart("phone", binding.phoneEditText.text.toString())
                                    .addFormDataPart("email", binding.emailEditText.text.toString())
                                    .addFormDataPart(
                                        "password",
                                        binding.passwordEditText.text.toString()
                                    )
                                    .addFormDataPart("city", binding.cityEditText.text.toString())
                                    .addFormDataPart(
                                        "type",
                                        binding.userTypeEditText.text.toString()
                                    )
                                    .addFormDataPart(
                                        "brand",
                                        binding.organisationNameEditText.text.toString()
                                    )

                                if (imageBody != null) {
                                    builder.addFormDataPart(
                                        "picture",
                                        imageBody.imageName,
                                        imageBody.imageReqBody
                                    )
                                }

                                val userToAdd = builder.build()

                                signUp(userToAdd).observe(this@UserCreateActivity) {
                                    if (it.isNullOrBlank()) {
                                        //dialog ?
                                        Log.i(TAG, "return : $it")
                                        this@UserCreateActivity.toast(SGN_FAILED, Toast.LENGTH_LONG)
                                        goToHomeFragment()
                                    } else {
                                        Log.i(TAG, "return : $it")
                                        this@UserCreateActivity.toast(
                                            SGN_SUCCESS,
                                            Toast.LENGTH_LONG
                                        )
                                        goToLoginPage()
                                    }
                                }
                            }
                        }

                        override fun onNegativeButtonClicked() {
                        }
                    },
                    getString(R.string.confirm_user_title),
                    getString(R.string.confirm_user_message)
                )
                alertDialog!!.show()
            }

            viewModel.isValidInput.observe(this@UserCreateActivity) { isActive ->
                Log.i(TAG, "$isActive")
                binding.signUpButton.isEnabled = isActive
            }
        }

        setContentView(binding.root)

    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            this@UserCreateActivity,
            onDialogClicked,
            getString(R.string.permission_required),
            getString(R.string.you_cant),
            negativeText = getString(R.string.no_thanks),
            positiveText = getString(R.string.authorise)

        ).show()
    }

    private fun goToHomeFragment() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginPage() {
        finish()
    }

    private fun setUpTheTypeEditText() {

        binding.userTypeTextField.editText?.setText(SellerType.SOLO.type)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val values = SellerType.values().map { sellerType ->
            sellerType.type
        }

        val adapter = ArrayAdapter(this, R.layout.list_item, values)
        (binding.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun updateImageText(data: Any?) {
        if (data == null) binding.imageName.text = IMAGE_NOT_SELECTED
        else binding.imageName.text = IMAGE_SELECTED
    }

    private fun getImagesRequestBody(uri: Uri): ImageInfo {

        val file = File(URIPathHelper().getPath(this, uri)!!)
        Log.i(TAG, "file selected : ${file.name}")
        val requestFile: RequestBody =
            file.asRequestBody("image/*".toMediaTypeOrNull())

        return ImageInfo(
            file.name,
            requestFile
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }
}

class ImageInfo(val imageName: String, val imageReqBody: RequestBody)
