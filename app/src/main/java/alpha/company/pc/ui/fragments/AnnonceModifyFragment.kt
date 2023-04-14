package alpha.company.pc.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import alpha.company.pc.R
import alpha.company.pc.data.models.local.Detail
import alpha.company.pc.data.models.network.Status
import alpha.company.pc.databinding.AddDetailBinding
import alpha.company.pc.databinding.FragmentAnnonceModifyBinding
import alpha.company.pc.ui.activities.AnnonceModifyActivity
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.adapters.AddDetailsAdapter
import alpha.company.pc.ui.adapters.ImagesModifyAdapter
import alpha.company.pc.ui.viewmodels.AnnonceModifyModel
import alpha.company.pc.utils.*
import android.net.Uri
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "AnnonceModifyActivity"

class AnnonceModifyFragment : Fragment() {

    private lateinit var binding: FragmentAnnonceModifyBinding
    private lateinit var viewModel: AnnonceModifyModel
    private lateinit var annonceToModifyId: String
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var picturesAdapter: ImagesModifyAdapter
    private lateinit var annonceActivity: AnnonceModifyActivity
    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {

        annonceActivity = (requireActivity() as AnnonceModifyActivity)
        picasso = annonceActivity.picasso
        annonceToModifyId = annonceActivity.intent.getStringExtra("id")!!

        viewModel = annonceActivity.viewModel.also {
            it.apply {
                getAnnonce(annonceToModifyId)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAnnonceModifyBinding.inflate(inflater, container, false)

        picturesAdapter = ImagesModifyAdapter(
            annonceActivity.annoncePictures,
            object : ImagesModifyAdapter.OnImageModifyClicked {
                override fun onImageClicked(
                    imageIndex: Int,
                    imagesList: List<Picture>
                ) {
                    goToImageModifyFragment(
                        imageIndex,
                    )
                }

                override fun onAddClicked() {
                    openGallery()
                }

//                override fun onRightClicked() {
////                    TODO("Not yet implemented")
//                }
//
//                override fun onLeftClicked() {
////                    TODO("Not yet implemented")
//                }
            },
            picasso
        )

        imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    viewModel.apply {

                        if (data != null) {

                            val clipData = data.clipData
                            if (clipData == null) {
                                picturesAdapter.addPictures(listOf(Picture("new", data.data)))
                            } else {
                                val pictures = mutableListOf<Picture>()
                                for (i in 0 until clipData.itemCount) {
                                    val currentItemUri = clipData.getItemAt(i).uri
                                    pictures.add(
                                        Picture(
                                            "new",
                                            currentItemUri
                                        )
                                    )
                                }
                                picturesAdapter.addPictures(pictures)
                            }

                        }
                    }
                }
            }

        binding.apply {
            viewModel.apply {
                oldAnnonce.observe(viewLifecycleOwner) { annonce ->

                    isTurning.observe(viewLifecycleOwner) { loading ->
                        annonceModifyProgressBar.isVisible = loading
                        changeUiEnabling(loading)
                    }

                    // on annonce retrieved fail
                    if (annonce == null) {
                        doOnFail(getString(R.string.error))
                    } else {

                        Log.i(TAG, "annonce retrieved : $annonce")

                        var details = if (annonce.details.isNullOrEmpty()) {
                            mutableListOf()
                        } else {
                            annonce.details.map { detail ->
                                val detailsSplit = detail.split(":", limit = 2)
                                Detail(detailsSplit[0], detailsSplit[1])
                            }
                        }
                        //fill the annoncePictures list

                        if (annonceActivity.annoncePictures.size == 1) {
                            annonceActivity.annoncePictures = annonce.pictures.map { name ->
                                Picture(name)
                            }.toMutableList()
                            picturesAdapter.setImageList(annonceActivity.annoncePictures)
                        }

                        Log.d(TAG, "annoncePictures: ${annonceActivity.annoncePictures}")

                        // on annonce retrieved success
                        //to add image adding and deleting

                        addDetails.setOnClickListener {
                            // open the dialog
                            val detailsViewBinding = AddDetailBinding.inflate(layoutInflater)

                            Log.i(TAG, "details: $details")
                            val detailsAddAdapter = AddDetailsAdapter(
                                details as MutableList<Detail>
                            )

                            detailsViewBinding.apply {
                                detailsAddAdapter.apply {
                                    filterDetailsList()
                                    isEmpty.observe(viewLifecycleOwner) { isEmpty ->
                                        noDetails.isVisible = isEmpty
                                    }
                                    addDetail.setOnClickListener {
                                        addEmptyField()
                                        noDetails.isVisible = false
                                    }
                                }

                                addDetailsRv.apply {
                                    adapter = detailsAddAdapter
                                    layoutManager =
                                        LinearLayoutManager(requireContext())
                                }
                            }

                            val dialog = makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {
                                        detailsAddAdapter.apply {
                                            filterDetailsList()
                                            details = detailsList
                                        }

                                        Log.i(TAG, "details : $details")
                                    }

                                    override fun onNegativeButtonClicked() {
                                        Log.i(TAG, "onNegativeButtonClicked: clicked")
                                    }
                                },
                                getString(R.string.detail_title),
                                null,
                                detailsViewBinding.root
                            )
                            dialog.show()
                            dialog.window!!.clearFlags(
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                        or
                                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                            )
                        }


                        imagesRv.apply {
                            adapter = picturesAdapter

                            layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        }
                        titleEditText.setText(annonce.title)
                        priceEditText.setText(annonce.price.toString())
                        setTheCategoriesEditText(annonce.category)
                        setTheSubCategoriesEditText(annonce.subCategory)
                        setTheCitiesEditText(annonce.city)
                        setTheStatueEditTextView(annonce.status)
                        setTheAvailabilityEditText(getAvailabilityMessage(annonce.isAvailable))
                        markEditText.setText(annonce.mark)
                        descriptionEditText.setText(annonce.description)

                        validateTheData()

                        submitChanges.setOnClickListener {

                            triggerLoading()

//                            val newAnnonce = Annonce(
                            val title = titleEditText.text.toString()
                            val price = priceEditText.text.toString()
                            val availability =
                                getAvailabilityFromString(availabilityEditText.text.toString())
                            val category = categoryEditText.text.toString()
                            val subCategory = subCategoryEditText.text.toString()
                            val status = statusEditText.text.toString()
                            val mark = markEditText.text.toString()
                            val description = descriptionEditText.text.toString()
                            val city = cityEditText.text.toString()

                            annonceActivity.annoncePictures.removeAt(annonceActivity.annoncePictures.lastIndex)
//                                seller = annonce.seller,
//                                visited = annonce.visited
//                            )

                            val builder = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)

                            builder.apply {
                                if (annonce.title != title)
                                    addFormDataPart("title", title)

                                if (annonce.price.toString() != price)
                                    addFormDataPart("price", price)

                                if (annonce.isAvailable != availability)
                                    addFormDataPart("isAvailable", availability.toString())

                                if (annonce.category != category)
                                    addFormDataPart("category", category)

                                if (annonce.subCategory != subCategory && subCategory != "-")
                                    addFormDataPart("subCategory", subCategory)

                                if (annonce.status != status)
                                    addFormDataPart("status", status)

                                if (annonce.mark != mark)
                                    addFormDataPart("mark", mark)

                                if (annonce.description != mark)
                                    addFormDataPart("description", description)

                                if (annonce.city != city)
                                    addFormDataPart("city", city)

                                Log.d(TAG, "details: $details")
                                for (i in details.indices) {
                                    addFormDataPart(
                                        "details[$i]",
                                        "${details[i].title}:${details[i].body}"
                                    )
                                }

                                val job = lifecycleScope.async {
                                    getImagesRequest(builder)
                                }
                                lifecycleScope.launch {
                                    job.await()
                                    updateAnnonceInfo(annonceToModifyId, builder.build())
                                }

                            }

                            updatedAnnonce.observe(viewLifecycleOwner) { annonceModified ->

                                //on annonce modification fail
                                if (!annonceModified) {
                                    doOnFail(getString(R.string.error))
                                } else {
                                    doOnSuccess(getString(R.string.annonce_modify_success))
                                }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }


    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {
            scrollView.isEnabled = !loading
            submitChanges.isEnabled = !loading
            imagesRv.isEnabled = !loading
            addDetails.isEnabled = !loading
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imageResultLauncher.launch(intent)
    }

    private fun goToImageModifyFragment(imageIndex: Int) {
        val action = AnnonceModifyFragmentDirections
            .actionAnnonceModifyFragmentToImageModifyFragment(imageIndex)
        findNavController().navigate(action)
    }

    private fun setTheEditText(editText: EditText, list: List<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, list)
        (editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }


    private fun setTheCategoriesEditText(default: String) {
        //default is the annonce attribute value
        binding.categoryTextField.editText?.setText(default)
        //set the adapter
        viewModel.categoriesList.observe(viewLifecycleOwner) { categories ->
            val values = categories.map { category ->
                category.category
            }
            setTheEditText(binding.categoryEditText, values)
        }
    }

    private fun setTheSubCategoriesEditText(default: String) {
        binding.subCategoryEditText.setText(default)
        viewModel.categoriesList.observe(viewLifecycleOwner) { categories ->
            val subCategoriesDefault = categories.find { category ->
                category.subcategories.contains(default)
            }?.subcategories
            setTheEditText(binding.subCategoryEditText, subCategoriesDefault!!)

            binding.categoryEditText.doOnTextChanged { text, _, _, _ ->
                val subCategories = categories.find { category ->
                    category.category == text.toString()
                }?.subcategories
                if (!subCategories.isNullOrEmpty()) {
                    setTheEditText(binding.subCategoryEditText, subCategories)
                } else {
                    setTheEditText(binding.subCategoryEditText, listOf("-"))
                }
            }
        }
    }

    private fun setTheCitiesEditText(default: String) {
        binding.cityEditText.setText(default)
        viewModel.citiesList.observe(viewLifecycleOwner) { cities ->
            setTheEditText(binding.cityEditText, cities)
        }
    }

    private fun setTheStatueEditTextView(default: String) {
        //default is the annonce attribute value
        binding.statusTextField.editText?.setText(default)
        //set the adapter
        val values = Status.values().map {
            it.status
        }
        setTheEditText(binding.statusEditText, values)
    }

    private fun setTheAvailabilityEditText(default: String) {
        binding.availabilityEditText.setText(default)
        val values = listOf(getString(R.string.in_stock), getString(R.string.out_of_stock))
        setTheEditText(binding.availabilityEditText, values)
    }

    private fun getAvailabilityMessage(availability: Boolean): String {
        return if (availability) getString(R.string.in_stock) else getString(R.string.out_of_stock)
    }

    private fun getAvailabilityFromString(text: String): Boolean {
        return text == getString(R.string.in_stock)
    }

    private fun validateTheData() {

        //by default the button is disabled
        binding.apply {

            //initialise the value of the live data to check
            submitChanges.isEnabled = true

            viewModel.apply {
                titleLiveData.value = titleEditText.text.toString()
                priceLiveData.value = priceEditText.text.toString()
            }

            titleEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.titleLiveData.value = text.toString()
            }

            priceEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.priceLiveData.value = text.toString()
            }

            viewModel.isValidInput.observe(viewLifecycleOwner) { isActive ->
                binding.submitChanges.isEnabled = isActive
            }
        }
    }

    private fun geToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        geToMainActivity()
    }

    private fun doOnSuccess(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        requireActivity().finish()
    }

    private suspend fun getImagesRequest(builder: MultipartBody.Builder) {
        val pictures = annonceActivity.annoncePictures
        for (i in pictures.indices) {
            if (pictures[i].uri != null) {
                val file = getImageRequestBody(pictures[i].uri!!, requireContext())
                if (file != null) {
                    builder.addFormDataPart(
                        "pictures",
                        file.imageName,
                        file.imageReqBody
                    )
                }
            } else {
                builder.addFormDataPart(
                    "pictures[$i][name]",
                    pictures[i].name
                ).addFormDataPart(
                    "pictures[$i][position]",
                    i.toString()
                )
            }
        }
    }

}

data class Picture(
    val name: String,
    var uri: Uri? = null
)