package com.example.pc.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import com.example.pc.R
import com.example.pc.data.models.HandleSubmitInterface
import com.example.pc.databinding.FragmentUserStepOneBinding
import com.example.pc.ui.activities.UserCreateActivity
import com.example.pc.ui.viewmodels.UserStepOneModel

private const val TAG = "UserStepOne"

class UserStepOne : Fragment(), HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepOneBinding
    private lateinit var viewModel: UserStepOneModel
    private lateinit var userCreateActivity: UserCreateActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = UserStepOneModel()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserStepOneBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = requireActivity().findViewById<Button>(R.id.next)
        nextButton.apply {
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
                    passwordLiveData.value = text.toString()
                    passwordHelperText.observe(viewLifecycleOwner) {
                        passwordTextField.helperText = it
                    }
                }
            }

            retypePasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    retypedPasswordLiveData.value = text.toString()
                    retypedPasswordHelperText.observe(viewLifecycleOwner) {
                        retypePasswordTextField.helperText = it
                    }
                }
            }
        }

    }

    override fun onNextClicked() {
        handleUserInput()
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