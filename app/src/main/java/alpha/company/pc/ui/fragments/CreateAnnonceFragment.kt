package alpha.company.pc.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Status
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.CreateAnnonceRepository
import alpha.company.pc.databinding.FragmentCreateAnnonceBinding
import alpha.company.pc.ui.activities.LoginActivity
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.ui.viewmodels.CreateAnnonceModel
import alpha.company.pc.utils.*
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "CreateAnnonceFragment"

class CreateAnnonceFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var binding: FragmentCreateAnnonceBinding? = null
    private lateinit var viewModel: CreateAnnonceModel
    private lateinit var authModel: AuthModel
    private lateinit var userId: String
    private var imagesUris = listOf<Uri>()

    //add the livedata validation

    override fun onCreate(savedInstanceState: Bundle?) {

        val retrofitService = RetrofitService.getInstance(requireContext())
        viewModel = CreateAnnonceModel(CreateAnnonceRepository(retrofitService))
        authModel = AuthModel(retrofitService, null)
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

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data

                    if (data?.data != null) {
                        updateImageText(1)
                        imagesUris = listOf(data.data!!)
                    } else if (data?.clipData?.itemCount != null) {
                        val itemCount = data.clipData?.itemCount
                        updateImageText(itemCount)
                        imagesUris = getImagesUris(data.clipData!!)
                    }
                }
            }

        binding = FragmentCreateAnnonceBinding.inflate(
            inflater,
            container,
            false
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        authModel.apply {

            isTurning.observe(viewLifecycleOwner) { isLoading ->
                binding!!.progressBar.isVisible = isLoading
            }

            user.observe(viewLifecycleOwner) { user ->

                if (user != null) {

                    userId = user.userId!!
                    viewModel.apply {
                        getCategories()
                        getCities()
                    }


                    showForm()
                    setTheStatueEditTextView()
                    setTheCityEditText()
                    setTheCategoriesEditText()
                    validateTheData()

                    binding!!.apply {

                        addButton.setOnClickListener {

                            makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {

                                        val imagesPart = getImagesRequest()

                                        val builder = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart(
                                                "title",
                                                viewModel.titleLiveData.value!!
                                            )
                                            .addFormDataPart(
                                                "price",
                                                priceEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "category",
                                                categoryEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "city",
                                                cityEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "status",
                                                statusEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "mark",
                                                markEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "description",
                                                descriptionEditText.text.toString()
                                            )
                                            .addFormDataPart("seller[id]", user.userId)
                                            .addFormDataPart("seller[name]", user.name)

                                        if (user.imageUrl != null) {
                                            builder.addFormDataPart(
                                                "seller[picture]",
                                                user.imageUrl
                                            )
                                        }


                                        var i = 0
                                        for (body in imagesPart) {
                                            builder.addFormDataPart(
                                                "pictures",
                                                body.key,
                                                body.value
                                            )
                                            i++
                                        }
                                        val annonceToAdd = builder.build()


                                        viewModel.apply {

                                            isTurning.observe(viewLifecycleOwner) { loading ->
                                                binding!!.progressBar.isVisible = loading
                                                changeUiEnabling(loading)
                                            }

                                            //to change
                                            addAnnonce(annonceToAdd)
                                            requestSuccessful.observe(viewLifecycleOwner) { requestSuccess ->
                                                isTurning.observe(viewLifecycleOwner) { isVisible ->
                                                    progressBar.isVisible = isVisible
                                                }
                                                Log.i(
                                                    TAG,
                                                    "response succes from fragment $requestSuccess"
                                                )
                                                if (requestSuccess) doOnSuccess()
                                                else doOnFail()
                                            }
                                        }


                                    }

                                    override fun onNegativeButtonClicked() {
//                                    _,_ -> null
                                    }
                                },
                                title = getString(R.string.confirm_annonce_title),
                                message = getString(R.string.confirm_annonce_message)
                            ).show()
                        }
                    }

                } else {
                    Log.i(TAG, "user not connected auth body $user")
                    showNoUserConnected()
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding?.apply {

            for (i in linearLayout.children) {
                i.isEnabled = !loading
            }
            imageSelection.isEnabled = !loading
            addButton.isEnabled = !loading
        }
    }

    private fun showForm() {
        binding!!.apply {
            createAnnonceForm.apply {
                isActivated = true
                isVisible = true
            }

            addButton.apply {
                isVisible = true
                isActivated = true
            }
            imageSelection.setOnClickListener {

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

    private fun showNoUserConnected() {
        binding!!.apply {
            noUserConnected.isVisible = true
            loginFromUserInfo.apply {
                isVisible = true
                setOnClickListener {
                    goToLoginActivity()
                }
            }
        }
    }

    private fun setTheEditText(editText: EditText, list: List<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, list)
        (editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheStatueEditTextView() {
        //default is new
        val values = Status.values().map { status ->
            status.status
        }
        setTheEditText(binding!!.statusEditText, values)
    }

    private fun setTheCityEditText() {
        viewModel.citiesList.observe(viewLifecycleOwner) { cities ->
            setTheEditText(binding!!.cityEditText, cities)
        }
    }


    private fun setTheCategoriesEditText() {
        viewModel.categoriesList.observe(viewLifecycleOwner) { categories ->
            setTheEditText(binding!!.categoryEditText, categories)
        }
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
    }

    private fun updateImageText(quantity: Int?) {
        if (quantity == 1) binding!!.imageNames.text = getString(R.string.image_selected)
        else binding!!.imageNames.text = getString(R.string.multiple_images_selected, quantity)
    }

    private fun getImagesUris(clipData: ClipData): List<Uri> {
        val imagesList = mutableListOf<Uri>()
        for (i in 0 until clipData.itemCount) {
            imagesList.add(clipData.getItemAt(i).uri)
        }
        return imagesList
    }

    private fun goToHomeFragment() {
        val action = CreateAnnonceFragmentDirections.actionCreateAnnonceFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun validateTheData() {

        //by default the button is disabled
        binding!!.apply {

            addButton.isEnabled = false

            titleEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.titleLiveData.value = text.toString()
            }

            priceEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.priceLiveData.value = text.toString()
            }

            imageNames.doOnTextChanged { text, _, _, _ ->
                viewModel.imagesLiveData.value = text.toString()
            }

            categoryEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.categoryLiveData.value = text.toString()
            }

            cityEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.citiesLiveData.value = text.toString()
            }

            statusEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.statusLiveData.value = text.toString()
            }

            viewModel.isValidInput.observe(viewLifecycleOwner) { isActive ->
                binding!!.addButton.isEnabled = isActive
            }
        }
    }

    private fun doOnSuccess() {
        requireContext().toast(getString(R.string.annonce_success_msg), Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }

    private fun doOnFail() {
        requireContext().toast(getString(R.string.error_msg), Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

//    private fun reloadActivity() {
//        val i = Intent(requireActivity(), MainActivity::class.java)
//        requireActivity().finish()
//        requireActivity().overridePendingTransition(0, 0)
//        startActivity(i)
//        requireActivity().overridePendingTransition(0, 0)
//    }

    private fun getImagesRequest(): HashMap<String, RequestBody> {

        val partsList = HashMap<String, RequestBody>()

        for (uri in imagesUris) {
            val file = getImageRequestBody(uri, requireContext())

            if (file != null) {
                partsList[file.imageName] = file.imageReqBody
            }

        }
        Log.i(TAG, "getRequestBody: $partsList")
        return partsList

    }

}