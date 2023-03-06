package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.databinding.FragmentDemandCreateBinding
import alpha.company.pc.ui.activities.LoginActivity
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.ui.viewmodels.DemandCreateModel
import alpha.company.pc.utils.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import okhttp3.MultipartBody

private const val TAG = "DemandCreateFragment"

class DemandCreateFragment : Fragment() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentDemandCreateBinding
    private lateinit var demandCreateModel: DemandCreateModel
    private lateinit var authModel: AuthModel
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        demandCreateModel =
            DemandCreateModel(DemandRepository(RetrofitService.getInstance(requireContext())))
        authModel = AuthModel(RetrofitService.getInstance(requireContext()))
        authModel.auth(requireContext())

        (requireActivity() as MainActivity).supportActionBar?.hide()

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

                Log.i(TAG, "isGranted: $isGranted")

                if (isGranted) {
                    setTheUploadImage()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                    val snackBar = makeSnackBar(
                        requireView(),
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
    ): View {
        binding = FragmentDemandCreateBinding.inflate(inflater, container, false)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    if (data?.data != null) {
                        Log.d(TAG, "data?.data: ${data.data}")
                        imageUri = data.data!!
                        binding.plusButton.setImageURI(imageUri)
                    }
                }
            }

        authModel.apply {
            isTurning.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
            user.observe(viewLifecycleOwner) { user ->
                if (user != null) {

                    showForm()
                    //set the other fields
                    //validate the data using the view model

                    binding.apply {
                        addButton.isEnabled = false
                        titleEditText.doOnTextChanged { text, _, _, _ ->
                            demandCreateModel.titleLiveData.value = text.toString()
                        }
                        demandCreateModel.isValidData.observe(viewLifecycleOwner) { isActive ->
                            addButton.isEnabled = isActive
                        }
                        addButton.setOnClickListener {

                            makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {
                                        val builder = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart(
                                                "title",
                                                titleEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "price",
                                                priceEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "description",
                                                descriptionEditText.text.toString()
                                            )
                                            .addFormDataPart("creator", user.userId.toString())

                                        if (imageUri != null) {
                                            val imagePart =
                                                getImageRequestBody(
                                                    imageUri!!,
                                                    requireContext(),
                                                )
                                            if (imagePart != null) {
                                                builder.addFormDataPart(
                                                    "picture",
                                                    imagePart.imageName,
                                                    imagePart.imageReqBody
                                                )
                                            }

                                        }

                                        demandCreateModel.apply {

                                            isTurning.observe(viewLifecycleOwner) { loading ->
                                                progressBar.isVisible = loading
                                                changeUiEnabling(loading)
                                            }

                                            //to change
                                            addDemand(builder.build())
                                            demandAdded.observe(viewLifecycleOwner) { demandAdded ->

                                                Log.i(
                                                    TAG,
                                                    "response succes from fragment $demandAdded"
                                                )
                                                if (demandAdded) doOnSuccess()
                                                else doOnFail()
                                            }
                                        }


                                    }

                                    override fun onNegativeButtonClicked() {
//                                    _,_ -> null
                                    }
                                },
                                title = getString(R.string.confirm_demand_title),
                                message = getString(R.string.confirm_demand_message)
                            ).show()

                        }
                    }


                } else {
                    Log.i(TAG, "user not connected auth body $user")
                    showNoUserConnected()
                }
            }
        }

        return binding.root
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {

            for (i in linearLayout.children) {
                i.isEnabled = !loading
            }
            plusButton.isEnabled = !loading
            addButton.isEnabled = !loading
        }
    }

    private fun showForm() {
        binding.apply {
            createAnnonceForm.apply {
                isActivated = true
                isVisible = true
            }

            addButton.apply {
                isVisible = true
                isActivated = true
            }
            plusButton.setOnClickListener {

                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        setTheUploadImage()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected. In this UI,
                        // include a "cancel" or "no thanks" button that allows the user to
                        // continue using your app without granting the permission.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: true")
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
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: false")
                        Log.i(TAG, "request Permission Launcher ")
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }

    private fun doOnSuccess() {
        requireContext().toast(getString(R.string.demand_success_msg), Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }

    private fun doOnFail() {
        requireContext().toast(getString(R.string.demand_error_msg), Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
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

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun goToHomeFragment() {
        val action = DemandCreateFragmentDirections.actionDemandCreateFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun showNoUserConnected() {
        binding.apply {
            noUserConnected.isVisible = true
            loginFromAddDemand.apply {
                isVisible = true
                setOnClickListener {
                    goToLoginActivity()
                }
            }
        }
    }

}