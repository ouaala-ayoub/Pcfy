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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import alpha.company.pc.R
import alpha.company.pc.data.models.network.User
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.AnnonceRepository
import alpha.company.pc.databinding.FragmentSellerInfoBinding
import alpha.company.pc.ui.activities.AnnonceActivity
import alpha.company.pc.ui.adapters.AnnoncesAdapter
import alpha.company.pc.ui.viewmodels.SellerInfoModel
import alpha.company.pc.utils.USERS_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import alpha.company.pc.utils.defineField
import alpha.company.pc.utils.toast
import android.widget.TextView
import com.squareup.picasso.Picasso

private const val NUM_ROWS = 2
private const val TAG = "SellerInfoFragment"
private const val ERROR_INN = "Erreur Innatendue"

class SellerInfoFragment : Fragment() {

    private lateinit var binding: FragmentSellerInfoBinding
    private lateinit var viewModel: SellerInfoModel
    private val args: SellerInfoFragmentArgs by navArgs()
    private lateinit var sellerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sellerId = args.id
        viewModel = SellerInfoModel(
            AnnonceRepository(
                RetrofitService.getInstance(requireContext())
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSellerInfoBinding.inflate(inflater, container, false)

        viewModel.apply {

            getUserById(sellerId).observe(viewLifecycleOwner) { seller ->

                if (seller == null) {
                    doOnGetSellerFail()
                    return@observe
                } else {

                    setTheSellerInfo(seller)

                    getSellerAnnonces(sellerId).observe(viewLifecycleOwner) { annonces ->

                        if (annonces == null) {
                            doOnGetSellerFail()
                        } else {
                            Log.i(TAG, "annonces retrieved : $annonces")

                            isTurning.observe(viewLifecycleOwner) {
                                binding.sellerProgresBar.isVisible = it
                            }
                            updateIsEmpty().observe(viewLifecycleOwner) {
                                binding.noAnnounces.isVisible = it
                            }

                            val adapter = AnnoncesAdapter(
                                object : AnnoncesAdapter.OnAnnonceClickListener {
                                    override fun onAnnonceClick(annonceId: String) {
                                        goToAnnonceActivity(annonceId)
                                    }

                                    override fun onAnnonceLoadFail() {
                                        findNavController().popBackStack()
                                    }
                                }, mutableListOf()
                            )
                            binding.sellerAnnoncesRv.apply {
                                adapter.setAnnoncesListFromAdapter(annonces)
                                this.adapter = adapter
                                layoutManager = GridLayoutManager(requireContext(), NUM_ROWS)
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(requireContext(), AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    private fun setTheSellerInfo(seller: User) {
        binding.apply {

            val userName = seller.name

//            sellerImage to add
            Picasso
                .get()
                .load("${USERS_AWS_S3_LINK}${seller.imageUrl}")
                .error(R.drawable.ic_baseline_no_photography_24)
                .placeholder(circularProgressBar(requireContext()))
                .fit()
                .into(sellerImage)


            sellerContact.text = seller.phoneNumber
            sellerName.text = userName
            defineField(sellerType, seller.role, requireContext())
            defineField(sellerCity, seller.city, requireContext())

            annonceOf.text = getString(R.string.annonce_of, userName)

            swiperefresh.setOnRefreshListener {
                viewModel.getSellerAnnonces(sellerId)
                swiperefresh.isRefreshing = false
            }

//            TODO("seller website redirect to link")
//            sellerWebsite.text
//            sellerWebsite.setOnClickListener {
//                to do
//               go to seller website
//            }
        }
    }

    private fun doOnGetSellerFail() {
        requireContext().toast(ERROR_INN, Toast.LENGTH_SHORT)
        requireActivity().finish()
    }
}