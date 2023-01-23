package alpha.company.pc.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import alpha.company.pc.R
import alpha.company.pc.databinding.FragmentUserStepOneBinding
import alpha.company.pc.ui.activities.UserCreateActivity
import alpha.company.pc.ui.viewmodels.UserStepOneModel

private const val TAG = "UserStepOne"

class UserStepOne : Fragment(), alpha.company.pc.data.models.HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepOneBinding
    private lateinit var viewModel: UserStepOneModel
    private lateinit var userCreateActivity: UserCreateActivity
    private var lastState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = UserStepOneModel()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserStepOneBinding.inflate(inflater, container, false)

        val nextButton = requireActivity().findViewById<Button>(R.id.next)
        nextButton.apply {
            Log.i(TAG, "nextButton last step: $lastState")
            isEnabled = false
            viewModel.isValidInput.observe(viewLifecycleOwner) {
                nextButton.isEnabled = it
            }
        }

        binding.apply {
            emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    emailLiveData.value = text.toString()
                    emailHelperText.observe(viewLifecycleOwner) {
                        emailTextField.helperText = it
                    }
                }
            }

            passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    passwordLiveData.value = text.toString().replace("\\s".toRegex(), "")
                    passwordHelperText.observe(viewLifecycleOwner) {
                        passwordTextField.helperText = it
                    }
                }
            }

            retypePasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    retypedPasswordLiveData.value = text.toString().replace("\\s".toRegex(), "")
                    retypedPasswordHelperText.observe(viewLifecycleOwner) {
                        retypePasswordTextField.helperText = it
                    }
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val nextButton = requireActivity().findViewById<Button>(R.id.next)
        nextButton.isEnabled = lastState
    }

    override fun onNextClicked() {
        lastState = viewModel.isValidInput.value!!
        Log.i(TAG, "onNextClicked lastState Step One : $lastState")
        handleUserInput()
    }

    override fun onBackClicked() {
        // do nothing
    }

    private fun handleUserInput() {
        userCreateActivity = requireActivity() as UserCreateActivity
        binding.apply {
            userCreateActivity.apply {
                requestBody.addFormDataPart("email", emailEditText.text.toString())
                requestBody.addFormDataPart("password", passwordEditText.text.toString())
            }
        }
    }
}