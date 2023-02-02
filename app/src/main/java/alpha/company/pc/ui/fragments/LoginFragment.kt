package alpha.company.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.databinding.FragmentLoginBinding
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.activities.UserCreateActivity
import alpha.company.pc.ui.viewmodels.LoginModel
import alpha.company.pc.utils.toast

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
            if (isActive) {
                binding!!.passwordInput.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        performLoginAction()
                    }
                    true
                }
            } else {
                binding!!.passwordInput.setOnEditorActionListener(null)
            }
        }
        binding!!.login.setOnClickListener(this)
        binding!!.signUp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.login -> {
                performLoginAction()
            }
            R.id.sign_up -> {
                //sign up fragment
                goToUserFragment()
            }
        }
    }

    private fun performLoginAction() {
        viewModel.apply {

            val userName = binding!!.usernameInput.text.toString()
            val password = binding!!.passwordInput.text.toString()

            login(
                userName,
                password,
                requireActivity()
            )
            retrievedTokens.observe(viewLifecycleOwner) { retrievedTokens ->
                if (retrievedTokens) {
                    requireContext().toast(LOGIN_SUCCESS, Toast.LENGTH_SHORT)
                    goToMainActivity()
                } else {
                    requireContext().toast(LOGIN_FAILED, Toast.LENGTH_SHORT)
                    goToMainActivity()
                }
            }


            getErrorMessage().observe(viewLifecycleOwner) { error ->
                binding!!.problemMessage.text = error
            }

            isTurning.observe(viewLifecycleOwner) { loading ->
                binding!!.loginProgressBar.isVisible = loading
                changeUiEnabling(loading)
            }
        }
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding?.apply {
            login.isEnabled = !loading
            signUp.isEnabled = !loading
            passwordInput.isEnabled = !loading
            usernameInput.isEnabled = !loading
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