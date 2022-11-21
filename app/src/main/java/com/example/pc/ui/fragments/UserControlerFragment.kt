package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.pc.R
import com.example.pc.databinding.FragmentUserControlerBinding
import com.example.pc.ui.activities.FullOrdersActivity
import com.example.pc.ui.activities.RequestsActivity
import com.example.pc.ui.activities.UserAnnoncesActivity
import com.example.pc.ui.activities.UserInfoModifyActivity

class UserControlerFragment : Fragment() {

    private lateinit var binding: FragmentUserControlerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserControlerBinding.inflate(inflater, container, false)

        val args: UserControlerFragmentArgs by navArgs()
        val userId = args.userId

        binding.apply {
            userInfo.setOnClickListener {
                goToUserInfoModify(userId)
            }
            userAnnounces.setOnClickListener {
                goToUserAnnonces(userId)
            }
            orders.setOnClickListener {
                goToFullOrdersPage(userId)
            }
            buys.setOnClickListener {
                goToUserRequests(userId)
            }
            passwordChange.setOnClickListener {

            }
        }

        return binding.root
    }

    private fun goToFullOrdersPage(userId: String) {
        goToActivityWithUserId(userId, FullOrdersActivity::class.java)
    }

    private fun goToUserInfoModify(userId: String) {
        goToActivityWithUserId(userId, UserInfoModifyActivity::class.java)
    }

    private fun goToUserAnnonces(userId: String) {
        goToActivityWithUserId(userId, UserAnnoncesActivity::class.java)
    }

    private fun goToUserRequests(userId: String) {
        goToActivityWithUserId(userId, RequestsActivity::class.java)
    }

    private fun <T> goToActivityWithUserId(userId: String, activity: Class<T>) {
        val intent = Intent(requireContext(), activity)
        intent.putExtra("id", userId)
        startActivity(intent)
    }
}