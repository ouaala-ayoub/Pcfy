package com.example.pc.ui.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserInfoBinding
import com.example.pc.databinding.NoUserConnectedBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.viewmodels.UserInfoModel
import com.squareup.picasso.Picasso

private const val TAG = "UserInfoFragment"

class UserInfoFragment : Fragment(),  View.OnClickListener {

    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private var binding: FragmentUserInfoBinding? = null
    private var bindingNoUser: NoUserConnectedBinding? = null
    private lateinit var loginRepository: LoginRepository
    private lateinit var userId: String
    private var picasso = Picasso.get()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginRepository = LoginRepository(
            retrofitService,
            requireContext().applicationContext
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{

        return if(loginRepository.user == null){
            bindingNoUser = NoUserConnectedBinding.inflate(inflater, container, false)
            bindingNoUser!!.loginFromUserInfo.setOnClickListener {
                goToLoginActivity()
            }
            bindingNoUser?.root
        }else {
            binding = FragmentUserInfoBinding.inflate(inflater, container, false)
            binding?.root
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userInfoModel = UserInfoModel(
            UserInfoRepository(retrofitService),
            loginRepository
        )

        if (binding == null) {
            Log.i(TAG, "binding check: binding $binding")
            return
        }

        userInfoModel.isTurning.observe(viewLifecycleOwner) {
            binding!!.userInfoProgressbar.isVisible = it
        }

        userInfoModel.getIsLoggedIn().observe(viewLifecycleOwner) {
            binding!!.apply {
                userInfoModel.apply {

                    val currentUser = getCurrentUser()

                    getUserById(currentUser!!.userId).observe(viewLifecycleOwner) { user ->
                        if (user != null) {
                            userName.text = user.name
                            userType.text = requireContext().getString(R.string.user_type, user.userType)

                            Log.i(TAG, "image : ${user.imageUrl}")

                            if (user.imageUrl.isNullOrBlank()){
                                Log.i(TAG, "image is null: yes")
                                userImage.setImageResource(
                                    R.drawable.ic_baseline_no_photography_24
                                )
                            }
                            else{
                                val url = "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50"
                                picasso
                                    .load(user.imageUrl)
                                    .fit()
                                    .centerCrop()
                                    .into(userImage)
                            }

                            this@UserInfoFragment.apply {
                                userImage.setOnClickListener(this)
                                userInfo.setOnClickListener(this)
                                userAnnounces.setOnClickListener(this)
                                about.setOnClickListener(this)
                                share.setOnClickListener(this)
                                logout.setOnClickListener(this)
                            }
                            isTurning.observe(viewLifecycleOwner) {
                                Log.i(TAG, "isTurning: $it")
                                userInfoProgressbar.isVisible = it
                            }
                        }
                        else {
                            Log.e(TAG, "error retrieving the user : ${error.value}")
                        }
                    }
                }
            }
        }

        userInfoModel.apply {
            getIsLoggedIn().observe(viewLifecycleOwner) {
                Log.i(TAG, "observed: ")
                val currentUser = getCurrentUser()
                if (currentUser != null){
                    Log.i(TAG, "user id : ${currentUser.userId}")
                    getUserById(currentUser.userId).observe(viewLifecycleOwner){ user ->
                        if (user != null){
                            Log.i(TAG, "user retrieved : $user")

                            binding!!.apply {
                                userName.text = user.name
                                userType.text = requireContext().getString(R.string.user_type, user.userType)

                                Log.i(TAG, "image : ${user.imageUrl}")

                                if (user.imageUrl.isNullOrBlank()){
                                    Log.i(TAG, "image is null: yes")
                                    userImage.setImageResource(
                                        R.drawable.ic_baseline_no_photography_24
                                    )
                                }
                                else{
                                    val url = "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50"
                                    picasso
                                        .load(user.imageUrl)
                                        .fit()
                                        .centerCrop()
                                        .into(userImage)
                                }

                                this@UserInfoFragment.apply {
                                    userImage.setOnClickListener(this)
                                    userInfo.setOnClickListener(this)
                                    userAnnounces.setOnClickListener(this)
                                    about.setOnClickListener(this)
                                    share.setOnClickListener(this)
                                    logout.setOnClickListener(this)
                                }
                            }
                        }
                        else {
                            Log.e(TAG, "error retrieving the user : ${error.value}")
                        }
                    }
                }
                else {
                    Log.e(TAG, "error message : ${error.value}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {

        Log.i(TAG, "onClick: clicked ${v?.id}")

        when(v?.id){

            R.id.user_image -> {
                //choose image intent
            }
            R.id.user_announces -> {

            }
            R.id.user_info -> {

            }
            R.id.about -> {

            }
            R.id.share -> {

            }
            R.id.logout ->{
                loginRepository.logout()
                reloadActivity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        bindingNoUser = null
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

}