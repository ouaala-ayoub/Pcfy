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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserInfoBinding
import com.example.pc.ui.activities.*
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.UserInfoModel
import com.example.pc.utils.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "UserInfoFragment"

class UserInfoFragment : Fragment(), View.OnClickListener {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private lateinit var authModel: AuthModel
    private var binding: FragmentUserInfoBinding? = null
    private lateinit var loggedInUser: LoggedInUser
    private val picasso = Picasso.get()
    private var imageUri: Uri? = null
    var times = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        userInfoModel = UserInfoModel(
            UserInfoRepository(retrofitService),
        )

        authModel = AuthModel(retrofitService, LoginRepository(retrofitService, requireContext()))

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


        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.hide()

        binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    if (data?.data != null) {
                        imageUri = data.data!!

                        val path = URIPathHelper().getPath(requireContext(), imageUri!!)
                        Log.i(TAG, "onClick: $path")
                        val file = File(path)
                        val requestFile: RequestBody =
                            file.asRequestBody("image/*".toMediaTypeOrNull())

                        val requestBody = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("picture", file.name, requestFile)
                            .build()

                        userInfoModel.apply {
                            updateImage(loggedInUser.userId, requestBody)
                            updatedPicture.observe(viewLifecycleOwner) { updated ->
                                if (updated) {
                                    Log.i(TAG, "userId: ${loggedInUser.userId}")


                                } else {
                                    requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                                    reloadActivity()
                                }
                            }
                        }
                    }
                }
            }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authModel.apply {
            auth(requireContext())
            auth.observe(viewLifecycleOwner) {

                isTurning.observe(viewLifecycleOwner) {
                    binding!!.userInfoProgressbar.isVisible = it
                }

                if (isAuth()) {

                    Log.i(TAG, " auth from auth true : ${isAuth()}")

                    showForm()

                    userInfoModel.apply {
                        binding!!.apply {

                            val payload = getPayload()!!
                            loggedInUser = LoggedInUser(payload.id, payload.name)
                            Log.i(TAG, "current user: $loggedInUser")

                            getUserById(loggedInUser.userId)
                            userRetrieved.observe(viewLifecycleOwner) { user ->
                                if (user != null) {
                                    userName.text = user.name
                                    if (!user.userType.isNullOrBlank()) {
                                        userType.text = requireContext().getString(
                                            R.string.user_type,
                                            user.userType
                                        )
                                    }

                                    Log.i(TAG, "image : ${user.imageUrl}")

                                    if (user.imageUrl.isNullOrBlank()) {
                                        Log.i(TAG, "image is null: yes")
                                        userImage.setImageResource(
                                            R.drawable.ic_baseline_no_photography_24
                                        )
                                    } else {
                                        //for test purposes
                                        picasso
                                            .load("${USERS_AWS_S3_LINK}${user.imageUrl}")
                                            .networkPolicy(NetworkPolicy.NO_CACHE)
                                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                                            .fit()
                                            .into(userImage)
                                    }

                                    this@UserInfoFragment.apply {
                                        userImage.setOnClickListener(this)
                                        userInfo.setOnClickListener(this)
                                        userAnnounces.setOnClickListener(this)
                                        website.setOnClickListener(this)
                                        about.setOnClickListener(this)
                                        share.setOnClickListener(this)
                                        logout.setOnClickListener(this)
                                    }
                                    isTurning.observe(viewLifecycleOwner) {
                                        Log.i(TAG, "isTurning: $it")
                                        userInfoProgressbar.isVisible = it
                                    }
                                } else {
                                    requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                                    reloadActivity()
                                }
                            }
                        }
                    }
                } else {
                    errorMessage.observe(viewLifecycleOwner) { error ->
                        if (error == ERROR_MSG) {
                            showError()
                        } else if (error == NON_AUTHENTICATED) {
                            showNoUser()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.user_image -> {

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

            R.id.user_info -> {
                goToUserInfoModify(loggedInUser.userId)
            }

            R.id.about -> {
                goToAboutFragment()
            }

            R.id.share -> {
                // share the app play store link
            }

            R.id.user_announces -> {
                goToUserAnnonces(loggedInUser.userId)
            }

            R.id.website -> {
                openTheWebsite()
            }

            R.id.logout -> {
                authModel.logout()
                requireContext().toast(LOGOUT_SUCCESS, Toast.LENGTH_SHORT)
                reloadActivity()
            }
        }
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

    private fun showForm() {
        binding!!.apply {
            userInfoAppBar.apply {
                isVisible = true
                isActivated = true
            }
            scrollView.apply {
                isActivated = true
                isVisible = true
            }
        }
    }

    private fun showError() {
        binding!!.apply {
            errorMessage.apply {
                text = ERROR_MSG
                isVisible = true
            }
        }
    }

    private fun showNoUser() {
        binding!!.apply {
            disconnected.apply {
                isVisible = true
                loginFromUserInfo.setOnClickListener {
                    goToLoginActivity()
                }
            }
        }
    }

    private fun goToAboutFragment() {
        val action = UserInfoFragmentDirections.actionUserInfoFragmentToInfoFragment()
        findNavController().navigate(action)
    }

    private fun goToUserInfoModify(userId: String) {
        val intent = Intent(requireContext(), UserInfoModifyActivity::class.java)
        intent.putExtra("id", userId)
        startActivity(intent)
    }

    private fun goToUserAnnonces(userId: String) {
        val intent = Intent(requireContext(), UserAnnoncesActivity::class.java)
        intent.putExtra("id", userId)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.show()
        binding = null
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun reloadActivity() {
        val activity = requireActivity()
        val intent = Intent(activity, MainActivity::class.java)
        activity.finish()
        activity.overridePendingTransition(0, 0)
        startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }

    private fun openTheWebsite() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(getString(R.string.pcfy_website))
        startActivity(openURL)
    }

}