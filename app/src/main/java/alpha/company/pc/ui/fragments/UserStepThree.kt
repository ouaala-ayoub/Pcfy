package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import alpha.company.pc.R
import alpha.company.pc.data.models.local.SellerType
import alpha.company.pc.databinding.FragmentUserStepThreeBinding
import alpha.company.pc.ui.activities.UserCreateActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val TAG = "UserStepThree"

class UserStepThree : Fragment(), alpha.company.pc.data.models.HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepThreeBinding
    private lateinit var userCreateActivity: UserCreateActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUserStepThreeBinding.inflate(inflater, container, false)
        setTheTypeEditText()

        return binding.root
    }

    override fun onNextClicked() {
        binding.apply {
            userCreateActivity = requireActivity() as UserCreateActivity

            val cityField = cityEditText.text.toString()
            val typeField = userTypeEditText.text.toString()
            val organization = organisationNameEditText.text.toString()

            userCreateActivity.requestBody.apply {
                if (cityField.isNotBlank()) {
                    addFormDataPart("city", cityField)
                }
                if (typeField.isNotBlank()) {
                    addFormDataPart("type", getTypeRequestBody(typeField))
                }
                if (organization.isNotBlank()) {
                    addFormDataPart("brand", organization)
                }
                for (v in linearLayout4.children) {
                    v.isEnabled = false
                }
            }
        }
    }

    override fun onBackClicked() {
        //do Nothing no need
    }

    private fun setTheTypeEditText() {
        binding.userTypeTextField.editText?.setText(SellerType.SOLO.type)

        val values = SellerType.values().map { sellerType ->
            getType(sellerType.type)
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun getType(valueInEnglish: String): String {
        //to change with translation functionality
        return when (valueInEnglish) {
            SellerType.SOLO.type -> "Utilisteur normal"
            SellerType.PRO.type -> "Utilisteur professionnel"
            else -> {
                "erreur"
            }
        }
    }

    private fun getTypeRequestBody(valueTranslated: String): String {
        //to change with translation functionality
        return when (valueTranslated) {
            "Utilisteur normal" -> SellerType.SOLO.type
            "Utilisteur professionnel" -> SellerType.PRO.type
            else -> {
                SellerType.SOLO.type
            }
        }
    }
}