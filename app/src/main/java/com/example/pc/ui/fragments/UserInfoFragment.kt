package com.example.pc.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserInfoBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.activities.UserAnnoncesActivity
import com.example.pc.ui.activities.UserInfoModifyActivity
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.UserInfoModel
import com.example.pc.utils.USERS_AWS_S3_LINK
import com.example.pc.utils.toast
import com.squareup.picasso.Picasso

private const val TAG = "UserInfoFragment"
private const val ERROR_MSG = "Erreur inattendue"

class UserInfoFragment : Fragment(), View.OnClickListener {

    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private lateinit var authModel: AuthModel
    private var binding: FragmentUserInfoBinding? = null
    private lateinit var user: LoggedInUser
    private var picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {

        userInfoModel = UserInfoModel(
            UserInfoRepository(retrofitService),
        )
        authModel = AuthModel(retrofitService, LoginRepository(retrofitService, requireContext()))

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.hide()

        binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding?.root
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

                    showForm()

                    userInfoModel.apply {
                        binding!!.apply {

                            val payload = getPayload()!!
                            user = LoggedInUser(payload.id, payload.name)
                            Log.i(TAG, "current user: $user")

                            getUserById(user.userId)
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
                                            .fit()
                                            .centerCrop()
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
                } else if (!isAuth()) {
                    //user is not authenticated do this
                    showNoUser()
                }
            }
        }
    }

    override fun onClick(v: View?) {

        Log.i(TAG, "onClick: clicked ${v?.id}")

        when (v?.id) {

            R.id.user_image -> {
                //change user image
            }

            R.id.user_info -> {
                goToUserInfoModify(user.userId)
            }

            R.id.about -> {
                goToAboutFragment()
            }

            R.id.share -> {
                // share the app play store link
            }

            R.id.user_announces -> {
                goToUserAnnonces(user.userId)
            }

            R.id.website -> {
                openTheWebsite()
            }

            R.id.logout -> {
                authModel.logout()
                reloadActivity()
            }
        }
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