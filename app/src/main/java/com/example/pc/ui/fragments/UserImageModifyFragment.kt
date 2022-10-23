package com.example.pc.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pc.R
import com.example.pc.data.models.local.LoadPolicy
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserImageModifyBinding
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.activities.imageLoader
import com.example.pc.ui.viewmodels.UserInfoModel
import com.example.pc.utils.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "UserImageModifyFragment"
private const val IMAGE_MODIFY_SUCCESS = "Image Modifiée avec success"
private const val IMAGE_DELETED_SUCCESS = "Image supprimée avec success"
private const val IMAGE_DELETED_ERROR = "erreur inattendue pendant la suppression de l'image"

class UserImageModifyFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentUserImageModifyBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: UserInfoModel
    private lateinit var imageUri: Uri
    private lateinit var userId: String
    private lateinit var imageUrl: String
    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navArgs: UserImageModifyFragmentArgs by navArgs()

        userId = navArgs.userId
        imageUrl = navArgs.imageName
        picasso = (requireActivity() as MainActivity).picasso
        viewModel = UserInfoModel(UserInfoRepository(RetrofitService.getInstance()), picasso)

        Log.i(TAG, "userId: $userId and imageName: $imageUrl")

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

                Log.i(TAG, "isGranted: $isGranted")

                if (isGranted) {
                    setTheUploadImage()
                } else {
                    val snackBar = makeSnackBar(
                        requireView(),
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

                        val path = URIPathHelper().getPath(requireContext(), imageUri)
                        Log.i(TAG, "onClick: $path")
                        val file = File(path)
                        val requestFile: RequestBody =
                            file.asRequestBody("image/*".toMediaTypeOrNull())

                        val requestBody = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("picture", file.name, requestFile)
                            .build()

                        viewModel.apply {
                            isTurning.observe(viewLifecycleOwner) {
                                binding.userImageLoading.isVisible = it
                            }
                            updateImage(userId, requestBody)
                            updatedPicture.observe(viewLifecycleOwner) { updated ->
                                if (updated) {
                                    Log.i(TAG, "userId: $userId")
                                    Log.i(TAG, "imageLoader: $imageLoader")
                                    loadUserImageNoCache(imageUrl, binding.userImage)
                                    imageLoader?.loadingPolicy =
                                        LoadPolicy.Reload
                                    Log.i(
                                        TAG,
                                        "loading policy : ${imageLoader!!.loadingPolicy}"
                                    )
//                                    viewModel.getUserById(userId)
                                    requireContext().toast(
                                        IMAGE_MODIFY_SUCCESS,
                                        Toast.LENGTH_SHORT
                                    )

                                } else {
                                    requireContext().toast(
                                        ERROR_MSG,
                                        Toast.LENGTH_SHORT
                                    )
                                    reloadActivity()
                                }
                            }
                        }
                    }
                }
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserImageModifyBinding.inflate(inflater, container, false)

        binding.apply {

            if (imageUrl == "undefined") {
                userImage.setImageResource(
                    R.drawable.ic_baseline_no_photography_24
                )
                delete.isEnabled = false
            } else {
                viewModel.loadUserImageFromCache(imageUrl, userImage)
            }


            delete.setOnClickListener {
                makeDialog(
                    requireContext(),
                    object : OnDialogClicked {
                        override fun onPositiveButtonClicked() {
                            viewModel.apply {
                                val tokens = LocalStorage.getTokens(requireContext())
                                val requestBody = getRequestBody(tokens)
                                if (requestBody != null) {
                                    deleteProfilePicture(userId, requestBody)
                                    deletedPicture.observe(viewLifecycleOwner) { deleted ->
                                        if (deleted) {
                                            requireContext().toast(
                                                IMAGE_DELETED_SUCCESS,
                                                Toast.LENGTH_SHORT
                                            )
                                            getUserById(userId)
                                        } else {
                                            doOnFail(IMAGE_DELETED_ERROR)
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
                )
            }
            modify.setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        setTheUploadImage()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
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

        return binding.root
    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            requireContext(),
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
        requireContext().toast(message, Toast.LENGTH_SHORT)
        findNavController().popBackStack()
    }

    private fun reloadActivity() {
        val activity = requireActivity()
        val intent = Intent(activity, MainActivity::class.java)
        activity.finish()
        activity.overridePendingTransition(0, 0)
        startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}