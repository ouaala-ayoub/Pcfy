package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import alpha.company.pc.R
import alpha.company.pc.data.models.HandleSubmitInterface
import alpha.company.pc.data.models.local.SellerType
import alpha.company.pc.databinding.FragmentUserStepThreeBinding
import alpha.company.pc.ui.activities.UserCreateActivity
import android.widget.Button
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MultipartBody
import okhttp3.RequestBody

private const val TAG = "UserStepThree"

class UserStepThree : Fragment(), HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepThreeBinding
    private lateinit var requestBody: MultipartBody.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBody = (requireActivity() as UserCreateActivity).requestBody
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

            val cityField = cityEditText.text.toString()
            val typeField = userTypeEditText.text.toString()
            val organization = organisationNameEditText.text.toString()

            requestBody.apply {
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Button>(R.id.next).isEnabled = true
        binding.apply {
            cityTextField.isEnabled = true
            userTypeTextField.isEnabled = true
            organisationNameTextField.isEnabled = true
        }
    }

    override fun onBackClicked() {
        //do Nothing no need
    }

    private fun setTheTypeEditText() {
        binding.userTypeTextField.editText?.setText(getType(SellerType.SOLO.type))

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