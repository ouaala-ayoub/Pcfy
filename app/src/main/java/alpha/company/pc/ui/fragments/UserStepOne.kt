package alpha.company.pc.ui.fragments

import alpha.company.pc.R
import alpha.company.pc.databinding.FragmentUserStepOneBinding
import alpha.company.pc.ui.activities.UserCreateActivity
import alpha.company.pc.ui.viewmodels.UserStepOneModel
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import okhttp3.MultipartBody


private const val TAG = "UserStepOne"

class UserStepOne : Fragment(), alpha.company.pc.data.models.HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepOneBinding
    private lateinit var viewModel: UserStepOneModel
    private lateinit var requestBody: MultipartBody.Builder
    private var lastState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = UserStepOneModel()
        requestBody = (requireActivity() as UserCreateActivity).requestBody
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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

            val filter =
                InputFilter { source, start, end, _, _, _ ->
                    for (i in start until end) {
                        if (Character.isWhitespace(source[i])) {
                            return@InputFilter ""
                        }
                    }
                    null
                }

            emailEditText.filters = arrayOf(filter)

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

        binding.apply {
            requestBody.apply {
                requestBody.addFormDataPart("email", emailEditText.text.toString())
                requestBody.addFormDataPart("password", passwordEditText.text.toString())
            }
        }
    }
}