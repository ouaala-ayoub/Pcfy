package alpha.company.pc.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private const val NO_NAME = "Entrez un nom"
private const val TEN_NUMBERS = "Doit etre composé de 10 chiffres"

class UserStepTwoModel: ViewModel() {
    
    val nameLiveData = MutableLiveData<String>()
    val nameHelperText = MutableLiveData<String>()

    val phoneLiveData = MutableLiveData<String>()
    val phoneHelperText = MutableLiveData<String>()

    val isValidInput = MediatorLiveData<Boolean>().apply {
        addSource(nameLiveData) { name ->
            this.value = validateTheData(
                name,
                phoneLiveData.value,
            )
        }
        addSource(phoneLiveData) { phone ->
            this.value = validateTheData(
                nameLiveData.value,
                phone,
            )
        }
    }

    private fun validateTheData(
        name: String?,
        phone: String?,
    ): Boolean {
        val isValidName = !name.isNullOrBlank()
        val isValidPhone = phone?.length == 10 && phone.matches(".*[0-9].*".toRegex())

        if (!isValidName) {
            nameHelperText.value = NO_NAME
        } else nameHelperText.value = ""

        if (!isValidPhone) {
            phoneHelperText.value = TEN_NUMBERS
        } else phoneHelperText.value = ""

        return isValidName && isValidPhone
    }

}