package alpha.company.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import alpha.company.pc.R
import alpha.company.pc.data.models.local.Detail
import alpha.company.pc.databinding.FragmentAnnonceBinding
import alpha.company.pc.ui.activities.AnnonceActivity
import alpha.company.pc.ui.activities.LoginActivity
import alpha.company.pc.ui.adapters.DetailsAdapter
import alpha.company.pc.ui.adapters.ImagesAdapter
import alpha.company.pc.ui.viewmodels.AnnonceModel
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.utils.*
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceFragment"
private const val ERROR_TEXT = "Erreur inattendue"
private const val NO_USER = "Vous n'êtes pas connecté"

class AnnonceFragment : Fragment() {

    private var binding: FragmentAnnonceBinding? = null
    private lateinit var annonceId: String
    private lateinit var userId: String
    private lateinit var detailsAdapter: DetailsAdapter
    private lateinit var picasso: Picasso
    private lateinit var viewModel: AnnonceModel
    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as AnnonceActivity

        annonceId = activity.intent.getStringExtra("id") as String
        picasso = activity.picasso
        viewModel = activity.viewModel
        authModel = activity.authModel

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val adRequest = AdRequest.Builder().build()
        binding = FragmentAnnonceBinding.inflate(
            inflater,
            container,
            false
        ).apply {
//            adView.loadAd(adRequest)
        }


        binding!!.apply {
            viewModel.apply {


                getAnnonceById(annonceId)

                annonceToShow.observe(viewLifecycleOwner) { annonce ->

                    if (annonce != null) {
                        //bind the data to the views

                        try {
//                            getSellerById(annonce.sellerId!!)

                            imagesVp.apply {

                                registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {

                                    }

                                    override fun onPageScrollStateChanged(state: Int) {


                                    }
                                })

                                val pictures = annonce.pictures
                                val imageLoader =
                                    pictures.map { url -> Picture(url) }.toMutableList()

//                                if (imageLoader.isEmpty())
//                                    imageLoader.add(Picture("))
                                if (pictures.isNotEmpty())
                                    offscreenPageLimit = pictures.size

                                adapter = ImagesAdapter(
                                    imageLoader,
                                    object : ImagesAdapter.OnImageClicked {
                                        override fun onImageZoomed() {
                                            TODO("Not yet implemented")
                                        }

                                        override fun onLeftClicked() {
                                            currentItem -= 1
                                        }

                                        override fun onRightClicked() {
                                            currentItem += 1
                                        }
                                    },
                                    picasso
                                )
                                TabLayoutMediator(trackingTab, this, true) { _, _ -> }.attach()
                            }

                            productTitle.text = annonce.title
                            productPrice.text = getString(R.string.price, annonce.price.toString())
                            productStatus.text = getString(R.string.status, annonce.status)

                            val details = annonce.details
                            if (!details.isNullOrEmpty()) {
                                //setting the details recycler view
                                detailsAdapter = DetailsAdapter(
                                    annonce.details.map { detail ->
                                        val detailsSplit = detail.split(":", limit = 2)
                                        Detail(detailsSplit[0], detailsSplit[1])
                                    }
                                )
                                detailsRv.apply {
                                    adapter = detailsAdapter
                                    isNestedScrollingEnabled = false
                                    setHasFixedSize(true)
                                    layoutManager = LinearLayoutManager(requireContext())
                                }
                            } else {
                                detailTv.isVisible = false
                            }

                            //the seller field
                            annonce.seller.apply {
                                Log.d(TAG, "annonce.seller: $this")
                                sellerName.text = this.name

                                picasso
                                    .load("${USERS_AWS_S3_LINK}${this.picture}")
                                    .fit()
                                    .placeholder(circularProgressBar(requireContext()))
                                    .error(R.drawable.ic_baseline_no_photography_24)
                                    .into(selleerImage)
                            }

                            productDescription.text = annonce.description

                            productSeller.setOnClickListener {
                                goToSellerPage(annonce.seller.id)
                            }
                            sellerInfo.setOnClickListener {
                                goToSellerPage(annonce.seller.id)
                            }

                        } catch (e: Throwable) {
                            Log.e(TAG, "binding error : ${e.message}")
                        }

                        //get the sellers name
                    } else {
                        requireContext().toast(ERROR_TEXT, Toast.LENGTH_SHORT)
                        Log.e(TAG, "error: something went wrong")
                        goToMainActivity()
                    }



                    authModel.apply {
                        user.observe(viewLifecycleOwner) { user ->

                            if (user != null) {
                                userId = user.userId!!
                                updateIsAddedToFav(userId, annonceId)

                                isAddedToFav.observe(viewLifecycleOwner) { isFavChecked ->
                                    addToFav.apply {
                                        isChecked = isFavChecked
                                        if (!isFavChecked) {
                                            setOnClickListener {
                                                addToFavourites(userId, annonceId)
                                                addedFavouriteToUser.observe(viewLifecycleOwner) {
                                                    if (!it) {
                                                        doOnFail(ERROR_TEXT)
                                                    }
                                                }
                                            }
                                        } else if (isFavChecked) {
                                            setOnClickListener {
                                                //add logic to delete favourite
                                                deleteFavourite(userId, annonceId)
                                                deletedWithSuccess.observe(viewLifecycleOwner) { deleted ->
                                                    if (!deleted) {
                                                        requireContext().toast(
                                                            ERROR_TEXT,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                orderNow.setOnClickListener {
                                    goToOrderFragment()
                                }

                            } else {
                                addToFav.apply {
                                    isChecked = false
                                    isEnabled = false
                                    setOnClickListener {
                                        requireContext().toast(NO_USER, Toast.LENGTH_SHORT)
//                                        doOnFail(NO_USER)
                                    }
                                }
                                orderNow.setOnClickListener {
//                                    requireContext().toast(
//                                        NO_USER,
//                                        Toast.LENGTH_SHORT
//                                    )
                                    goToLoginActivity()
                                }
                            }
                        }
                    }


                }

                isProgressBarTurning.observe(viewLifecycleOwner) {
                    annonceProgressBar.isVisible = it
                }

            }
        }

        return binding?.root
    }

    private fun goToOrderFragment() {
        val action = AnnonceFragmentDirections.actionAnnonceFragmentToOrderFragment()
        findNavController().navigate(action)
    }

    private fun goToSellerPage(sellerId: String) {
        val action = AnnonceFragmentDirections.actionAnnonceFragmentToSellerInfoFragment(sellerId)
        findNavController().navigate(action)
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        goToMainActivity()
    }

//    private fun doOnSuccess(message: String) {
//        requireContext().toast(message, Toast.LENGTH_SHORT)
//    }

    private fun goToMainActivity() {
        val activity = requireActivity() as AnnonceActivity
        activity.finish()
    }
}