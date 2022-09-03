package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserInfoBinding
import com.example.pc.databinding.NoUserConnectedBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.viewmodels.UserInfoModel

private const val TAG = "UserInfoFragment"

class UserInfoFragment : Fragment() {

    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private var binding: FragmentUserInfoBinding? = null
    private var bindingNoUser: NoUserConnectedBinding? = null
    private lateinit var loginRepository: LoginRepository
    private lateinit var userId: String

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
    ): View? {

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

        userInfoModel.apply {
            getIsLoggedIn().observe(viewLifecycleOwner) {
                Log.i(TAG, "observed: ")
                val currentUser = getCurrentUser()
                if (currentUser != null){
                    Log.i(TAG, "user id : ${currentUser.userId}")
                    getUserById(currentUser.userId).observe(viewLifecycleOwner){ user ->
                        if (user != null){
                            Log.i(TAG, "user retrieved : $user")
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        bindingNoUser = null
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

}