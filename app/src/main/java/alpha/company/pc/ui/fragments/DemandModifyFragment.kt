package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.databinding.FragmentDemandModifyBinding
import alpha.company.pc.ui.activities.DemandsModifyActivity
import alpha.company.pc.ui.viewmodels.DemandModifyModel
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "DemandModifyFragment"

class DemandModifyFragment : Fragment() {

    private lateinit var binding: FragmentDemandModifyBinding
    private lateinit var demandModifyModel: DemandModifyModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var picasso: Picasso
    private lateinit var demandId: String
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navArgs: DemandModifyFragmentArgs by navArgs()
        picasso = (requireActivity() as DemandsModifyActivity).picasso
        demandId = navArgs.demandId
        demandModifyModel = DemandModifyModel(
            DemandRepository(
                RetrofitService.getInstance(requireContext())
            )
        ).also { it.getDemandById(demandId) }

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

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemandModifyBinding.inflate(inflater, container, false)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    if (data?.data != null) {
                        Log.d(TAG, "data?.data: ${data.data}")
                        imageUri = data.data!!
//                        binding.plusButton.setImageURI(imageUri)
                        picasso
                            .load(imageUri)
                            .fit()
                            .placeholder(circularProgressBar(requireContext()))
                            .error(R.drawable.ic_baseline_no_photography_24)
                            .into(binding.plusButton)
                    }
                }
            }

        binding.apply {
            demandModifyModel.apply {

                plusButton.setOnClickListener {
                    handleImageChoice()
                }

                confirmChanges.setOnClickListener {
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

                                if (imageUri != null) {
                                    val job = lifecycleScope.async {
                                        getImageRequestBody(
                                            imageUri!!,
                                            requireContext(),
                                        )
                                    }
                                    lifecycleScope.launch {
                                        val imagePart = job.await()
                                        if (imagePart != null) {
                                            builder.addFormDataPart(
                                                "picture",
                                                imagePart.imageName,
                                                imagePart.imageReqBody
                                            )
                                        }
                                        updateDemand(demandId, builder.build())
                                    }
                                } else {
                                    updateDemand(demandId, builder.build())
                                }
                            }

                            override fun onNegativeButtonClicked() {
//                                    _,_ -> null
                            }
                        },
                        title = getString(R.string.confirmer_les_changements),
                        message = getString(R.string.demand_modify_dialog_message)
                    ).show()
                }

                titleEditText.doOnTextChanged { text, _, _, _ ->
                    titleLiveData.postValue(text.toString())
                }

                mediatorLiveData.observe(viewLifecycleOwner) { isValidData ->
                    confirmChanges.isEnabled = isValidData
                }

                demand.observe(viewLifecycleOwner) { demandRetrieved ->
                    if (demandRetrieved != null) {
                        titleEditText.setText(demandRetrieved.title)
                        priceEditText.setText(demandRetrieved.price)
                        descriptionEditText.setText(demandRetrieved.description)
                        picasso
                            .load("$DEMANDS_AWS_S3_LINK${demandRetrieved.picture}")
                            .error(R.drawable.ic_baseline_no_photography_24)
                            .placeholder(circularProgressBar(binding.root.context))
                            .fit()
                            .into(plusButton)

                    } else {
                        requireContext().toast(getString(R.string.error_msg), Toast.LENGTH_SHORT)
                        findNavController().popBackStack()
                    }

                    demandUpdated.observe(viewLifecycleOwner) { updated ->
                        if (updated) {
                            requireContext().toast(
                                getString(R.string.demand_modified_succes),
                                Toast.LENGTH_SHORT
                            )
                            reloadImageWithNoCache(demandRetrieved?.picture)
//                        findNavController().popBackStack()
                        } else {
                            requireContext().toast(
                                getString(R.string.error_msg),
                                Toast.LENGTH_SHORT
                            )

                        }
                    }

                }
                isTurning.observe(viewLifecycleOwner) { loading ->
                    changeUiEnabling(loading)
                    progressBar.isVisible = loading
                }

            }
        }
        return binding.root
    }

    private fun reloadImageWithNoCache(url: String?) {
        picasso
            .load("$DEMANDS_AWS_S3_LINK${url}")
            .error(R.drawable.ic_baseline_no_photography_24)
            .placeholder(circularProgressBar(binding.root.context))
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .fit()
            .into(binding.plusButton)
    }

    private fun handleImageChoice() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                setTheUploadImage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
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

    private fun changeUiEnabling(updated: Boolean) {
        binding.apply {
            for (i in linearLayout.children) {
                i.isEnabled = !updated
            }
            plusButton.isEnabled = !updated
            confirmChanges.isEnabled = !updated
        }
    }
}