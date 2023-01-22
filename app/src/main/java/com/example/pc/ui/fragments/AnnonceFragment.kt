package com.example.pc.ui.fragments

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
import com.example.pc.R
import com.example.pc.data.models.local.ImageLoader
import com.example.pc.data.models.local.LoadPolicy
import com.example.pc.databinding.FragmentAnnonceBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.adapters.DetailsAdapter
import com.example.pc.ui.adapters.ImagesAdapter
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.utils.*
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceActivity"
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

        val adRequest = AdRequest.Builder().build()
        binding = FragmentAnnonceBinding.inflate(
            inflater,
            container,
            false
        ).apply { adView?.loadAd(adRequest) }


        binding!!.apply {
            viewModel.apply {


                getAnnonceById(annonceId)

                annonceToShow.observe(viewLifecycleOwner) { annonce ->

                    if (annonce != null) {
                        //bind the data to the views

                        try {
//                            getSellerById(annonce.sellerId!!)

                            imagesVp.apply {

                                val pictures = annonce.pictures
                                val imageLoader =
                                    pictures.map { url -> ImageLoader(url, LoadPolicy.Cache) }

                                offscreenPageLimit = pictures.size
                                adapter = ImagesAdapter(
                                    imageLoader,
                                    object : ImagesAdapter.OnImageClicked {
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
                                    annonce.details
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
                                sellerName.text = this.name
                                Log.d(TAG, "annonce.seller : $this")
                                if (this.picture.isNullOrBlank()) {
                                    Log.d(TAG, "picture : ${this.picture}")
                                    selleerImage
                                        .setImageResource(R.drawable.ic_baseline_no_photography_24)
                                }
                                picasso
                                    .load("${USERS_AWS_S3_LINK}${this.picture}")
                                    .fit()
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
                        auth.observe(viewLifecycleOwner) {

                            if (isAuth()) {
                                userId = getUserId()!!
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

    private fun doOnSuccess(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
    }

    private fun goToMainActivity() {
        val activity = requireActivity() as AnnonceActivity
        activity.finish()
    }
}