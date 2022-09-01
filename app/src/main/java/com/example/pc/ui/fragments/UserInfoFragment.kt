package com.example.pc.ui.fragments

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
import com.example.pc.ui.viewmodels.UserInfoModel

private const val TAG = "UserInfoFragment"

class UserInfoFragment : Fragment() {

    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userInfoModel: UserInfoModel
    private var binding: FragmentUserInfoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userInfoModel = UserInfoModel(
            UserInfoRepository(retrofitService),
            LoginRepository(
                retrofitService,
                requireContext().applicationContext
            )
        )
        userInfoModel.apply {
            getIsLoggedIn().observe(viewLifecycleOwner) {
                val currentUser = getCurrentUser()
                if (currentUser != null){
                    Log.i(TAG, "user id : ${currentUser.userId}")
                    getUserById(currentUser.userId).observe(viewLifecycleOwner){

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
    }

}