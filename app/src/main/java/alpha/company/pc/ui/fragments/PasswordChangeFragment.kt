package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import alpha.company.pc.data.models.network.PasswordRequest
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.FragmentPasswordChangeBinding
import alpha.company.pc.ui.viewmodels.ChangePasswordModel
import alpha.company.pc.utils.toast

private const val PASS_MODIFIED = "Mot de passe modifié avec success"
private const val PASS_ERROR = "Veuillez verifiez les informations entré"
private const val TAG = "PasswordChangeFragment"

class PasswordChangeFragment : Fragment() {

    private lateinit var binding: FragmentPasswordChangeBinding
    private lateinit var changePasswordModel: ChangePasswordModel
    private lateinit var userId: String
    private val args: PasswordChangeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordChangeBinding.inflate(inflater, container, false)
        changePasswordModel = ChangePasswordModel(UserInfoRepository(RetrofitService.getInstance(requireContext())))

        binding.apply {
            changePasswordModel.apply {

                isTurning.observe(viewLifecycleOwner) { isTurning ->
                    passwordProgressBar.isVisible = isTurning
                    changeUiEnabling(isTurning)
                }

                oldPasswordInput.doOnTextChanged { text, _, _, _ ->
                    oldPassword.postValue(text.toString())
                }
                newPasswordInput.doOnTextChanged { text, _, _, _ ->
                    newPassword.postValue(text.toString())
                }
                newPasswordRetypeInput.doOnTextChanged { text, _, _, _ ->
                    newPasswordRetype.postValue(text.toString())
                }

                isValidData.observe(viewLifecycleOwner) { isValid ->
                    submitChanges.isEnabled = isValid
                }

                submitChanges.setOnClickListener {
                    userId = args.userId
                    val passwordData = PasswordRequest(
                        oldPassword.value!!,
                        newPassword.value!!
                    )
                    changePassword(userId, passwordData)
                    passwordModified.observe(viewLifecycleOwner) { passwordModified ->
                        if (passwordModified)
                            doOnEnd(PASS_MODIFIED)
                        else
                            doOnEnd(PASS_ERROR)
                    }
                }

            }
        }
        return binding.root
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {
            oldPasswordInput.isEnabled = !loading
            newPasswordInput.isEnabled = !loading
            newPasswordRetypeInput.isEnabled = !loading
            submitChanges.isEnabled = !loading
        }
    }

    private fun doOnEnd(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        findNavController().popBackStack()
    }
}