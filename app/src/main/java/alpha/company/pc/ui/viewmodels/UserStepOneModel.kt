package alpha.company.pc.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val INVALID_EMAIL = "Email Invalide"
private const val MIN_EIGHT = "Au moins 8 caracteres"
private const val NON_IDENTICAL = "Mot de passes non identiques"

class UserStepOneModel: ViewModel() {

    val emailLiveData = MutableLiveData<String>()
    val emailHelperText = MutableLiveData<String>()

    val passwordLiveData = MutableLiveData<String>()
    val passwordHelperText = MutableLiveData<String>()

    val retypedPasswordLiveData = MutableLiveData<String>()
    val retypedPasswordHelperText = MutableLiveData<String>()

    val isValidInput = MediatorLiveData<Boolean>().apply {
        addSource(emailLiveData) { email ->
            this.value = validateTheData(
                email,
                passwordLiveData.value,
                retypedPasswordLiveData.value
            )
        }
        addSource(passwordLiveData) { password ->
            this.value = validateTheData(
                emailLiveData.value,
                password,
                retypedPasswordLiveData.value
            )
        }
        addSource(retypedPasswordLiveData) { retypedPassword ->
            this.value = validateTheData(
                emailLiveData.value,
                passwordLiveData.value,
                retypedPassword
            )
        }
    }

    private fun validateTheData(
        email: String?,
        password: String?,
        retypedPassword: String?
    ): Boolean {

        val isValidEmail = if (email.isNullOrBlank()) false
        else
            Patterns.EMAIL_ADDRESS.matcher(
                email
            ).matches()

        val isValidPassword = if (password.isNullOrBlank()) false else password.length >= 8
        val isValidRetypedPassword = retypedPassword == password

        if (!isValidEmail) {
            emailHelperText.value = INVALID_EMAIL
        } else emailHelperText.value = ""

        if (!isValidPassword) {
            passwordHelperText.value = MIN_EIGHT
        } else passwordHelperText.value = ""

        if (!isValidRetypedPassword) {
            retypedPasswordHelperText.value = NON_IDENTICAL
        } else retypedPasswordHelperText.value = ""

        return  isValidEmail && isValidPassword && isValidRetypedPassword
    }

    fun submitData(): PartialData{
        return PartialData(emailLiveData.value, emailLiveData.value)
    }
    data class PartialData(val email: String?, val password: String?)
}


