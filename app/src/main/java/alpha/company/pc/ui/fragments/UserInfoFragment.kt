package alpha.company.pc.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import alpha.company.pc.R
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.FragmentUserInfoBinding
import alpha.company.pc.ui.activities.*
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.ui.viewmodels.UserInfoModel
import alpha.company.pc.utils.*
import com.squareup.picasso.Picasso

private const val TAG = "UserInfoFragment"

class UserInfoFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentUserInfoBinding? = null
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private lateinit var authModel: AuthModel
    private lateinit var loggedInUser: String
    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {

        picasso = (requireActivity() as MainActivity).picasso
        userInfoModel = UserInfoModel(
            UserInfoRepository(retrofitService),
            picasso
        )

        authModel = AuthModel(retrofitService, LoginRepository(retrofitService, requireContext()))

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.hide()

        binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        authModel.apply {
            auth(requireContext())
            auth.observe(viewLifecycleOwner) {

                isTurning.observe(viewLifecycleOwner) { loading ->
                    binding!!.userInfoProgressbar.isVisible = loading
                    changeUiEnabling(loading)
                }

                if (isAuth()) {

                    Log.i(TAG, " auth from auth true : ${isAuth()}")

                    showForm()

                    userInfoModel.apply {
                        binding!!.apply {

                            loggedInUser = getUserId()!!
//                            loggedInUser = "63dbfb767218225b4aa26b96"
                            Log.i(TAG, "current user: $loggedInUser")

                            getUserById(loggedInUser)
                            userRetrieved.observe(viewLifecycleOwner) { user ->

                                Log.i(TAG, "imageLoader: $imageLoader")
                                if (user != null) {
                                    userName.text = user.name
//                                    if (!user.userType.isNullOrBlank()) {
//                                        userType.text = requireContext().getString(
//                                            R.string.user_type,
//                                            user.userType
//                                        )
//                                    }

                                    val imageName = user.imageUrl
                                    if (imageName != null) {
                                        imageLoader?.imageUrl = imageName
                                    }

                                    Log.i(TAG, "image : ${user.imageUrl}")

                                    if (user.imageUrl.isNullOrBlank()) {
                                        Log.i(TAG, "image is null: yes")
                                        userImage.setImageResource(
                                            R.drawable.ic_baseline_no_photography_24
                                        )
                                        userImage.setOnClickListener {
                                            goToUserImageModify(
                                                loggedInUser,
                                                "undefined"
                                            )
                                        }
                                    } else {
                                        //for test purposes
                                        val url = "${USERS_AWS_S3_LINK}${user.imageUrl}"
                                        Log.i(TAG, "user image url: $url")
                                        //to fix the cache logic

                                        if (imageLoader?.loadingPolicy == LoadPolicy.Cache) {
                                            Log.i(TAG, "loading image with cache")
                                            loadUserImageFromCache(
                                                imageLoader!!.imageUrl,
                                                userImage
                                            )
                                        } else if (imageLoader?.loadingPolicy == LoadPolicy.Reload) {
                                            Log.i(TAG, "loading image with no cache")
                                            loadUserImageNoCache(
                                                imageLoader!!.imageUrl,
                                                userImage
                                            )
                                            imageLoader?.loadingPolicy = LoadPolicy.Cache
                                        }

                                        userImage.setOnClickListener {
                                            goToUserImageModify(
                                                loggedInUser,
                                                user.imageUrl
                                            )
                                        }
                                    }

                                    this@UserInfoFragment.apply {
                                        personalSpace.setOnClickListener(this)
                                        website.setOnClickListener(this)
                                        about.setOnClickListener(this)
                                        share.setOnClickListener(this)
                                        logout.setOnClickListener(this)
//                                        loginFromUserInfo.setOnClickListener(this)
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
                            Log.e(TAG, "some error occured: $error", )
                            showError()
                        } else if (error == NON_AUTHENTICATED) {
                            Log.e(TAG, "NON_AUTHENTICATED: $NON_AUTHENTICATED", )
//                            showNoUser()
                            binding?.apply {

                                noUserConnected.isVisible = true
                                noUserConnected.setOnClickListener {
                                    Log.d(TAG, "noUserConnected: clicked")
                                }
                                loginFromUserInfo.apply {
                                    isVisible = true
                                    setOnClickListener {
                                        goToLoginActivity()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return binding!!.root
    }


    private fun changeUiEnabling(loading: Boolean) {
        binding?.apply {
            for (v in linearLayoutForm.children) {
                v.isEnabled = !loading
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

//            R.id.login_from_user_info -> {
//                goToLoginActivity()
//            }

            R.id.personal_space -> {
                goToUserControllerFragment(loggedInUser)
            }

            R.id.about -> {
                goToAboutFragment()
            }

            R.id.share -> {
                // share the app play store link
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

    private fun goToUserControllerFragment(userId: String) {
        val action =
            UserInfoFragmentDirections.actionUserInfoFragmentToUserControlerFragment(userId)
        findNavController().navigate(action)
    }

    private fun goToUserImageModify(userId: String, imageName: String) {
        val action = UserInfoFragmentDirections.actionUserInfoFragmentToUserImageModifyFragment(
            userId = userId,
            imageName = imageName
        )
        findNavController().navigate(action)
    }


    private fun showForm() {
        binding!!.apply {
            linearLayoutForm.apply {
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
        binding?.apply {
//            disconnected.apply {
//                isVisible = true
////                noUserConnected.text = "test"
////                loginFromUserInfo.apply {
//////                    setOnClickListener {
//////                        Log.d(TAG, "loginFromUserInfo: clicked")
//////                        goToLoginActivity()
//////                    }
////                }
//            }
        }
    }

    private fun goToAboutFragment() {
        val action = UserInfoFragmentDirections.actionUserInfoFragmentToInfoFragment()
        findNavController().navigate(action)
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

    override fun onDestroy() {
        super.onDestroy()
        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.show()
        binding = null
    }

}