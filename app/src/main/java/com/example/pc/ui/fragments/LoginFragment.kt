package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentLoginBinding
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.activities.UserCreateActivity
import com.example.pc.ui.viewmodels.LoginModel
import com.example.pc.utils.toast

private const val LOGIN_FAILED = "Erreur de Connexion"
private const val LOGIN_SUCCESS = "Connecté avec success"
private const val TAG = "LoginFragment"

class LoginFragment : Fragment(), View.OnClickListener {

    private val retrofitService = RetrofitService.getInstance()
    private var binding: FragmentLoginBinding? = null
    private lateinit var viewModel: LoginModel
    private lateinit var loginRepository: LoginRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        loginRepository = LoginRepository(retrofitService, requireContext().applicationContext)
        binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //if the user is already logged in ?

        viewModel = LoginModel(loginRepository)

        binding!!.login.isEnabled = false

        binding!!.usernameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.userNameLiveData.value = text.toString()
        }
        binding!!.passwordInput.doOnTextChanged { text, _, _, _ ->
            viewModel.passwordLiveData.value = text.toString()
        }
        viewModel.isValidLiveData.observe(viewLifecycleOwner) { isActive ->
            binding!!.login.isEnabled = isActive
        }
        binding!!.login.setOnClickListener(this)
        binding!!.signUp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.login -> {

                viewModel.apply {

                    val userName = binding!!.usernameInput.text.toString()
                    val password = binding!!.passwordInput.text.toString()

                    login(
                        userName,
                        password,
                        requireActivity()
                    ).observe(viewLifecycleOwner) {

                        retrievedTokens.observe(viewLifecycleOwner) { retrievedTokens ->
                            if (retrievedTokens) {
                                requireContext().toast(LOGIN_SUCCESS, Toast.LENGTH_SHORT)
                                goToMainActivity()
                            } else {
                                requireContext().toast(LOGIN_FAILED, Toast.LENGTH_SHORT)
                                goToMainActivity()
                            }
                        }
                    }

                    getErrorMessage().observe(viewLifecycleOwner) { error ->
                        binding!!.problemMessage.text = error
                    }

                    isTurning.observe(viewLifecycleOwner) {
                        binding!!.loginProgressBar.isVisible = it
                    }
                }

            }
            R.id.sign_up -> {
                //sign up fragment
                goToUserFragment()
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToUserFragment() {
        val intent = Intent(requireContext(), UserCreateActivity::class.java)
        startActivity(intent)
    }

}