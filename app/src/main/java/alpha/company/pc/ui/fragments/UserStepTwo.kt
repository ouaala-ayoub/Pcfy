package alpha.company.pc.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import alpha.company.pc.R
import alpha.company.pc.databinding.FragmentUserStepTwoBinding
import alpha.company.pc.ui.activities.UserCreateActivity
import alpha.company.pc.ui.viewmodels.UserStepTwoModel
import alpha.company.pc.utils.OnDialogClicked
import alpha.company.pc.utils.URIPathHelper
import alpha.company.pc.utils.makeDialog
import alpha.company.pc.utils.makeSnackBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "UserStepTwo"

class UserStepTwo : Fragment(), alpha.company.pc.data.models.HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepTwoBinding
    private lateinit var viewModel: UserStepTwoModel
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestBody: MultipartBody.Builder
    private var lastState = false
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = UserStepTwoModel()
        requestBody = (requireActivity() as UserCreateActivity).requestBody

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    setTheUploadImage()
                } else {
                    val snackBar = makeSnackBar(
                        binding.root,
                        getString(R.string.permission),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar.setAction(R.string.ok) {
                        snackBar.dismiss()
                    }.show()

                }
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserStepTwoBinding.inflate(inflater, container, false)

        imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    Log.i(TAG, "resultLauncher: ${data?.data}")
                    imageUri = data?.data
                    binding.imageSelect.setImageURI(imageUri)
                }
            }

        val nextButton = requireActivity().findViewById<Button>(R.id.next)
        nextButton.apply {
            isEnabled = false
            viewModel.isValidInput.observe(viewLifecycleOwner) {
                isEnabled = it
            }
        }

        binding.apply {
            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    nameLiveData.value = text.toString()
                    nameHelperText.observe(viewLifecycleOwner) {
                        nameTextField.helperText = it
                    }
                }
            }

            phoneEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    phoneLiveData.value = text.toString()
                    phoneHelperText.observe(viewLifecycleOwner) {
                        phoneTextField.helperText = it
                    }
                }
            }
            imageSelect.setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        setTheUploadImage()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        showInContextUI(
                            object : OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }

                                override fun onNegativeButtonClicked() {
                                    //cancel the dialog without doing nothing
                                }
                            }
                        )
                    }
                    else -> {
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }

        return binding.root
    }

    override fun onNextClicked() {
        submitData()
        lastState = viewModel.isValidInput.value!!
        Log.i(TAG, "onNextClicked lastState Step Two : $lastState")
    }

    override fun onBackClicked() {
        Log.i(TAG, "onBackClicked lastState Step Two : $lastState")
        val lastValue = viewModel.isValidInput.value

        if (lastValue != null) {
            lastState = lastValue
        }
    }

    override fun onResume() {
        super.onResume()
        val nextButton = requireActivity().findViewById<Button>(R.id.next)
        nextButton.isEnabled = lastState
    }

    private fun submitData() {

        binding.apply {
            requestBody.apply {

                addFormDataPart("name", nameEditText.text.toString())
                addFormDataPart("phone", phoneEditText.text.toString())

                if (imageUri != null) {
                    val info = getImagesRequestBody(imageUri!!)
                    addFormDataPart("picture", info.imageName, info.imageReqBody)
                }
            }
        }

    }

    private fun getImagesRequestBody(uri: Uri): ImageInfo {

        val file = File(URIPathHelper().getPath(requireContext(), uri)!!)
        Log.i(TAG, "file selected : ${file.name}")
        val requestFile: RequestBody =
            file.asRequestBody("image/*".toMediaTypeOrNull())

        return ImageInfo(
            file.name,
            requestFile
        )
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            requireContext(),
            onDialogClicked,
            getString(R.string.permission_required),
            getString(R.string.you_cant),
            negativeText = getString(R.string.no_thanks),
            positiveText = getString(R.string.authorise)

        ).show()
    }

    class ImageInfo(val imageName: String, val imageReqBody: RequestBody)

}