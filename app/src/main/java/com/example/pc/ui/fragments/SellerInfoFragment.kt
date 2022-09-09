package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pc.R
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.databinding.FragmentSellerInfoBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.AnnoncesAdapter
import com.example.pc.ui.viewmodels.SellerInfoModel
import com.example.pc.utils.toast

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
                RetrofitService.getInstance()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSellerInfoBinding.inflate(inflater, container, false)

        viewModel.apply {

            getUserById(sellerId).observe(viewLifecycleOwner) { seller ->

                if (seller == null){
                    doOnGetSellerFail()
                    return@observe
                }
                else {

                    setTheSellerInfo(seller)

                    getSellerAnnonces(sellerId).observe(viewLifecycleOwner) {  annonces ->

                        if (annonces == null){
                            doOnGetSellerFail()
                        }
                        else {
                            Log.i(TAG, "annonces retrieved : $annonces")

                            setTheSellerInfo(seller)

                            val adapter = AnnoncesAdapter(
                                object : AnnoncesAdapter.OnAnnonceClickListener {
                                    override fun onAnnonceClick(annonceId: String) {
                                        goToAnnonceActivity(annonceId)
                                    }
                                }
                            )
                            binding.sellerAnnoncesRv.apply {
                                adapter.setAnnoncesList(annonces)
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

    private fun setTheSellerInfo(seller: User){
        binding.apply {

            val sellName = seller.name

            sellerName.text = sellName
            sellerType.text = seller.userType
            sellerCity.text = seller.city
            annonceOf.text = getString(R.string.annonce_of, sellName)

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