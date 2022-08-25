package com.example.pc.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentCreateAnnonceBinding
import com.example.pc.databinding.FragmentLoginBinding
import com.example.pc.ui.viewmodels.LoginModel
import com.example.pc.utils.toast
import io.github.nefilim.kjwt.JWT

private const val LOGIN_FAILED = "Erreur de Connexion"
private const val LOGIN_SUCCESS = "Connecté avec success"
private const val TAG = "LoginFragment"

class LoginFragment : Fragment(), View.OnClickListener {

    private val retrofitService = RetrofitService.getInstance()
    private var binding: FragmentLoginBinding? = null
    private lateinit var viewModel: LoginModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )
        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //if the user is already logged in ?

        viewModel = LoginModel(LoginRepository(retrofitService, requireActivity()))

        binding!!.login.isEnabled = false

        binding!!.usernameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.userNameLiveData.value = text.toString()
        }
        binding!!.passwordInput.doOnTextChanged { text, _, _, _ ->
            viewModel.passwordLiveData.value = text.toString()
        }
        viewModel.isValidLiveData.observe(viewLifecycleOwner){ isActive->
            binding!!.login.isEnabled = isActive
        }
        binding!!.login.setOnClickListener(this)
        binding!!.signUp.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {

        when(v?.id){
            R.id.login -> {

                viewModel.apply {

                    val userName = binding!!.usernameInput.text.toString()
                    val password = binding!!.passwordInput.text.toString()

                    login(
                        userName,
                        password,
                        requireActivity()
                    ).observe(viewLifecycleOwner){ tokenObject ->

                        retrievedTokens.observe(viewLifecycleOwner){ retrievedTokens->
                            if(retrievedTokens) {
                                requireContext().toast(LOGIN_SUCCESS, Toast.LENGTH_SHORT)
//                                isAuthenticated()
                                Log.i(TAG, "loggedIn user Id: ${getTheUserIdOrNull()}")
                                goToHomeFragment()
                            }
                            else{
                                requireContext().toast(LOGIN_FAILED, Toast.LENGTH_SHORT)
                            }
                        }


                    }

                    isTurning.observe(viewLifecycleOwner){
                        binding!!.loginProgressBar.isActivated = it
                    }
                }

            }
            R.id.sign_up -> {
                //sign up fragment
                goToUserFragment()
            }
        }
    }

    private fun goToHomeFragment(){
        val action = LoginFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(action)
    }

    private fun goToUserFragment(){
        val action = LoginFragmentDirections.actionLoginFragmentToUserFragment()
        findNavController().navigate(action)
    }

}