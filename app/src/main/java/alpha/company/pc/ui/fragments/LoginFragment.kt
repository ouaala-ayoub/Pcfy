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
import alpha.company.pc.data.repositories.LoginRepository
import alpha.company.pc.databinding.FragmentLoginBinding
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.activities.UserCreateActivity
import alpha.company.pc.ui.viewmodels.LoginModel
import alpha.company.pc.utils.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

private const val LOGIN_FAILED = "Erreur de Connexion"
private const val TAG = "LoginFragment"

class LoginFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentLoginBinding? = null
    private lateinit var viewModel: LoginModel
    private lateinit var loginRepository: LoginRepository
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        loginRepository = LoginRepository(requireContext())
        viewModel = LoginModel(loginRepository)
        binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.d(TAG, "permission to show notifications $isGranted")
            if (isGranted) {
                Log.d(TAG, "permission to show notifications $isGranted")
            } else {
                val snackBar = makeSnackBar(
                    binding!!.root,
                    getString(R.string.permission),
                    Snackbar.LENGTH_INDEFINITE
                )
                snackBar.setAction(R.string.ok) {
                    snackBar.dismiss()
                }.show()
            }
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //if the user is already logged in ?


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
                password
            )
            Firebase.messaging.token.addOnCompleteListener { task ->
                retrievedTokens.observe(viewLifecycleOwner) { retrievedTokens ->
                    if (retrievedTokens) {
                        val userId =
                            LocalStorage.getTokens(requireContext()).refreshToken?.let {
                                PayloadClass.getInfoFromJwt(
                                    it
                                )
                            }?.id
                        if (userId != null) {
                            val token = task.result
                            Log.d(TAG, "performLoginAction userId: $userId")
                            Log.d(TAG, "performLoginAction token: $token")
                            registerToken(userId, token)
                            askNotificationPermission()
                        }

                        requireContext().toast(getString(R.string.login_success), Toast.LENGTH_SHORT)
                        goToMainActivity()
                    } else {
//                        requireContext().toast(LOGIN_FAILED, Toast.LENGTH_SHORT)
//                    goToMainActivity()
                        Log.e(TAG, "performLoginAction: $LOGIN_FAILED" )
                    }
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                showInContextUI(object : OnDialogClicked {
                    override fun onPositiveButtonClicked() {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    override fun onNegativeButtonClicked() {
                        Log.d(TAG, "onNegativeButtonClicked: user declined notifications service")
                    }

                })

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            requireContext(),
            onDialogClicked,
            getString(R.string.permission_required),
            getString(R.string.no_notifications),
            negativeText = getString(R.string.no_thanks),
            positiveText = getString(R.string.authorise)
        ).show()
    }

}