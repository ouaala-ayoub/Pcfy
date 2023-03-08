package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.ActivityUserImageModifyBinding
import alpha.company.pc.ui.viewmodels.UserInfoModel
import alpha.company.pc.utils.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody

private const val TAG = "UserImageModifyActivity"

class UserImageModifyActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserImageModifyBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var viewModel: UserInfoModel
    private lateinit var imageUri: Uri
    private lateinit var userId: String
    private lateinit var imageUrl: String
    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityUserImageModifyBinding.inflate(layoutInflater)
        picasso = Picasso.get()
        userId = intent.getStringExtra("userId") as String
        imageUrl = intent.getStringExtra("imageName") as String
        viewModel = UserInfoModel(UserInfoRepository(RetrofitService.getInstance(this)), picasso)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

                Log.i(TAG, "isGranted: $isGranted")

                if (isGranted) {
                    setTheUploadImage()
                } else {
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
                    // There are no request codes
                    val data: Intent? = result.data
                    if (data?.data != null) {
                        imageUri = data.data!!

                        val file = getImageRequestBody(imageUri, this@UserImageModifyActivity)

                        if (file != null) {
                            val requestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("picture", file.imageName, file.imageReqBody)
                                .build()
                            viewModel.apply {
                                isTurning.observe(this@UserImageModifyActivity) {
                                    binding.userImageLoading.isVisible = it
                                }
                                updateImage(userId, requestBody)
                            }
                        }
                    }
                }
            }


        setContentView(binding.root)

        binding.apply {

            if (imageUrl == "undefined") {
                userImage.setImageResource(
                    R.drawable.ic_baseline_no_photography_24
                )
                delete.isEnabled = false
            } else {
                viewModel.loadUserImageFromCache(imageUrl, userImage)
            }

            viewModel.isTurning.observe(this@UserImageModifyActivity) { loading ->
                Log.i(TAG, "isTurning: $loading")
                userImageLoading.isVisible = loading

                changeUiEnabling(loading)

            }

            viewModel.updatedPicture.observe(this@UserImageModifyActivity) { updated ->
                if (updated) {
                    Log.i(TAG, "userId: $userId")
                    Log.i(TAG, "imageLoader: $imageLoader")
                    viewModel.loadUserImageNoCache(imageUrl, binding.userImage)
                    imageLoader?.loadingPolicy =
                        LoadPolicy.Reload
                    Log.i(
                        TAG,
                        "loading policy : ${imageLoader!!.loadingPolicy}"
                    )
//                                    viewModel.getUserById(userId)
                    this@UserImageModifyActivity.toast(
                        getString(R.string.image_modified_success),
                        Toast.LENGTH_SHORT
                    )

                } else {
                    this@UserImageModifyActivity.toast(
                        getString(R.string.error),
                        Toast.LENGTH_SHORT
                    )
                    reloadActivity()
                }
            }

            delete.setOnClickListener {
                makeDialog(
                    this@UserImageModifyActivity,
                    object : OnDialogClicked {
                        override fun onPositiveButtonClicked() {
                            viewModel.apply {

                                val tokens = LocalStorage.getTokens(this@UserImageModifyActivity)
                                val requestBody = getRequestBody(tokens)
                                if (requestBody != null) {
                                    deleteProfilePicture(userId, requestBody)
                                    deletedPicture.observe(this@UserImageModifyActivity) { deleted ->
                                        if (deleted) {
                                            this@UserImageModifyActivity.toast(
                                                getString(R.string.image_deleted_success),
                                                Toast.LENGTH_SHORT
                                            )
                                            getUserById(userId)
                                        } else {
                                            doOnFail(getString(R.string.image_deleted_fail))
                                        }
                                    }

                                } else {
                                    doOnFail(ERROR_MSG)
                                }
                            }
                        }

                        override fun onNegativeButtonClicked() {
                            //cancel == null
                        }

                    },
                    getString(R.string.user_image_delete_title),
                    getString(R.string.user_image_delete_message),
                ).show()
            }
            modify.setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        this@UserImageModifyActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        setTheUploadImage()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this@UserImageModifyActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
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
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {
            delete.isEnabled = !loading
            modify.isEnabled = !loading
        }
    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            this,
            onDialogClicked,
            getString(R.string.permission_required),
            getString(R.string.you_cant_user),
            negativeText = getString(R.string.no_thanks),
            positiveText = getString(R.string.authorise)

        ).show()
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    fun doOnFail(message: String) {
        this.toast(message, Toast.LENGTH_SHORT)
        finish()
    }

    private fun reloadActivity() {
        val activity = this
        val intent = Intent(activity, MainActivity::class.java)
        activity.finish()
        activity.overridePendingTransition(0, 0)
        startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}