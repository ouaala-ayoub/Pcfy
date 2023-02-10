package alpha.company.pc.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import alpha.company.pc.R
import alpha.company.pc.data.models.local.ImageLoader
import alpha.company.pc.data.models.local.LoadPolicy
import alpha.company.pc.databinding.FragmentImageModifyBinding
import alpha.company.pc.ui.activities.AnnonceModifyActivity
import alpha.company.pc.ui.adapters.ImagesAdapter
import alpha.company.pc.ui.viewmodels.AnnonceModifyModel
import alpha.company.pc.utils.*
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "ImageModifyFragment"
private const val DELETED_IMAGE = "Image supprimée avec succes"
private const val UPDATED_IMAGE = "Image changée avec succes"

class ImageModifyFragment : Fragment() {

    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentImageModifyBinding
    private lateinit var viewModel: AnnonceModifyModel
    private lateinit var annonceId: String
    private lateinit var annonceActivity: AnnonceModifyActivity
    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var picasso: Picasso
    private var imageIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        val args: ImageModifyFragmentArgs by navArgs()
        annonceActivity = (requireActivity() as AnnonceModifyActivity)
        //to modify
        annonceId = requireActivity().intent.getStringExtra("id")!!
        imageIndex = args.index
        viewModel = annonceActivity.viewModel
        picasso = annonceActivity.picasso

        Log.i(TAG, "imageIndex: $imageIndex")
        Log.i(TAG, "imagesList: ${annonceActivity.annoncePictures}")

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentImageModifyBinding.inflate(inflater, container, false)
        imagesAdapter = ImagesAdapter(
            annonceActivity.annoncePictures,
            object : ImagesAdapter.OnImageClicked {
                override fun onLeftClicked() {
                    binding.imagesPager.currentItem -= 1
                }

                override fun onRightClicked() {
                    binding.imagesPager.currentItem += 1
                }
            },
            picasso
        )
        imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    viewModel.apply {

                        if (data?.data != null) {
                            val indexToChange = binding.imagesPager.currentItem

                            imagesAdapter.modifyImageAtPosition(data.data, indexToChange)

//                            val requestBody =
//                                getRequestBody(indexToChange, data.data!!)

//                            viewModel.apply {
//                                changePicture(annonceId, requestBody)
//                                updatedImage.observe(viewLifecycleOwner) { updated ->
//                                    Log.i(TAG, "updated: $updated")
//                                    if (updated) {
//                                        requireContext().toast(UPDATED_IMAGE, Toast.LENGTH_SHORT)
//                                        imagesAdapter.reloadImageAt(indexToChange)
////                                        findNavController().popBackStack()
//                                    } else {
//                                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
//                                        (requireActivity() as AnnonceModifyActivity).finish()
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }

        binding.apply {

            imagesPager.apply {
                offscreenPageLimit = annonceActivity.annoncePictures.size
                adapter = imagesAdapter
                currentItem = imageIndex
            }

            viewModel.apply {

                isTurning.observe(viewLifecycleOwner) { loading ->
                    imagesModifyProgresBar.isVisible = loading
                    changeUiEnabling(loading)
                }

                delete.setOnClickListener {
//                    val currentItem = imagesPager.currentItem
//                    Log.i(TAG, "going to delete item : $currentItem")
//                    val builder = MultipartBody
//                        .Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("index", currentItem.toString())
//                        .build()
//
//                    makeDialog(
//                        requireContext(),
//                        object : OnDialogClicked {
//                            override fun onPositiveButtonClicked() {
////                                deleteImage(annonceId, builder)
////                                deletedImage.observe(viewLifecycleOwner) { deleted ->
////                                    // go back to the previous screen if deleted successfully
////                                    if (deleted) {
////                                        requireContext().toast(DELETED_IMAGE, Toast.LENGTH_SHORT)
////                                        findNavController().popBackStack()
////                                    } else {
////                                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
////                                        (requireActivity() as AnnonceModifyActivity).finish()
////                                    }
////                                }
//                            }
//
//                            override fun onNegativeButtonClicked() {
//                                //do nothing
//                            }
//
//                        },
//                        getString(R.string.image_instructions_title),
//                        getString(R.string.image_instructions_message)
//                    ).show()

                    imagesAdapter.deleteImageAtPosition(imagesPager.currentItem)

                }

                modify.setOnClickListener {
                    setTheUploadImage()
                }
            }
        }

        return binding.root
    }

    private fun changeUiEnabling(loading: Boolean) {
        binding.apply {
            modify.isEnabled = !loading
            delete.isEnabled = !loading
            imagesPager.isUserInputEnabled = !loading
        }
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }

    private fun getRequestBody(index: Int, imageUri: Uri): RequestBody {

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val filePath = URIPathHelper().getPath(requireContext(), imageUri)
        val file = File(filePath!!)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        builder.apply {
            addFormDataPart("index", index.toString())
            addFormDataPart(
                "picture",
                file.name,
                requestFile
            )
        }

        return builder.build()
    }

}