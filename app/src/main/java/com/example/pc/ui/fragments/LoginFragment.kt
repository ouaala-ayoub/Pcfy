package com.example.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentCreateAnnonceBinding
import com.example.pc.databinding.FragmentLoginBinding
import com.example.pc.ui.viewmodels.LoginModel
import com.example.pc.utils.toast

private const val LOGIN_FAILED = "Erreur de Connexion"

class LoginFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = LoginFragment()
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //if the user is already logged in ?

        viewModel = LoginModel(LoginRepository(retrofitService))

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

    override fun onClick(v: View?) {

        when(v?.id){
            R.id.login -> {
                //add a waiting thing progress bar maybe while waiting for the login

                //login
//                val isAuthenticated = viewModel.login()

                if(true) {
                    goToHomeFragment()
//                    viewModel.login()
                }
                else {
                    //handle response error messages
                    // exp = "user errors , internet errors, username and password are wrong ?"
                    // to implement

                    requireActivity().toast(ERROR_MSG, Toast.LENGTH_LONG)
                }
                //then go to home fragment
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