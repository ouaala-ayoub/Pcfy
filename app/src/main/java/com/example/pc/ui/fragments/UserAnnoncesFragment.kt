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
import com.example.pc.data.models.network.Tokens
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.FragmentUserAnnoncesBinding
import com.example.pc.ui.activities.AnnonceModifyActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.activities.UserAnnoncesActivity
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.adapters.OrdersShortAdapter
import com.example.pc.ui.viewmodels.UserAnnoncesModel
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
import com.example.pc.utils.toast

private const val TAG = "UserAnnoncesFragment"
private const val ANNONCE_DELETED_SUCCESS = "Annonce suprimée avec succès"
private const val ANNONCE_ERROR_MSG = "Erreur inattendue"
private const val ORDERS_ERROR = "Erreur de chargement des commandes"

class UserAnnoncesFragment : Fragment() {

    private lateinit var binding: FragmentUserAnnoncesBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var userId: String
    private lateinit var userAnnoncesModel: UserAnnoncesModel
    private lateinit var currentTokens: Tokens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity() as UserAnnoncesActivity
        userId = activity.userId
        userAnnoncesModel = UserAnnoncesModel(
            UserInfoRepository(
                retrofitService,
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        currentTokens = LocalStorage.getTokens(requireActivity())
        binding = FragmentUserAnnoncesBinding.inflate(inflater, container, false)

        val adapter = FavouritesAdapter(

            object : FavouritesAdapter.OnFavouriteClickListener {

                override fun onFavouriteClicked(annonceId: String) {
                    goToAnnonceModifyActivity(annonceId)
                }

                override fun onDeleteClickListener(annonceId: String) {
                    //to do
                    makeDialog(
                        requireContext(),
                        object : OnDialogClicked {
                            override fun onPositiveButtonClicked() {
                                userAnnoncesModel.apply {
                                    //delete then observe the deleted Boolean
                                    deleteAnnonce(currentTokens, annonceId)

                                    deletedAnnonce.observe(viewLifecycleOwner) { deletedWithSuccess ->
                                        if (deletedWithSuccess) {
                                            getAnnoncesById(userId)
                                            requireContext().toast(
                                                ANNONCE_DELETED_SUCCESS,
                                                Toast.LENGTH_SHORT
                                            )
                                        } else {
                                            requireContext().toast(
                                                ANNONCE_ERROR_MSG,
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                                }
                            }

                            override fun onNegativeButtonClicked() {}
                        },
                        getString(R.string.annonce_delete_dialog_title),
                        getString(R.string.annonce_delete_dialog_message)
                    ).show()
                }
            },
            object : FavouritesAdapter.OnCommandsClicked {
                override fun onCommandClicked(annonceId: String, adapter: OrdersShortAdapter) {
                    //send the request
                    Log.i(TAG, "onCommandClicked: $annonceId")
                    userAnnoncesModel.apply {
                        getAnnonceOrders(annonceId)
                        ordersMap.observe(viewLifecycleOwner) { orders ->
                            Log.i(TAG, "onCommandClicked orders: $orders")
                            if (orders[annonceId] == null) {
                                requireContext().toast(
                                    ORDERS_ERROR,
                                    Toast.LENGTH_SHORT
                                )
                            } else if (orders[annonceId] != null) {
                                Log.i(TAG, "onCommandClicked error : ")

                                adapter.setOrdersList(orders[annonceId]!!)
                                return@observe
                            }
                        }
                    }
                }
            },
            object : OrdersShortAdapter.OnOrderClicked {
                override fun onOrderClicked(orderId: String) {
                    // go to order page
                    goToOrderPage(orderId)
                }
            }
        )

        userAnnoncesModel.apply {
            getAnnoncesById(userId).observe(viewLifecycleOwner) { annonces ->

                if (annonces == null) {
                    requireContext().toast(ANNONCE_ERROR_MSG, Toast.LENGTH_SHORT)
                    returnToUserInfo()
                } else {
                    updateIsEmpty().observe(viewLifecycleOwner) {
                        binding.isEmpty.isVisible = it
                    }
                    Log.i(TAG, "annonces : $annonces")
                    adapter.setList(annonces)
                }
            }
        }

        binding.apply {
            annoncesRv.adapter = adapter
            annoncesRv.layoutManager = LinearLayoutManager(requireContext())

            swiperefresh.setOnRefreshListener {
                userAnnoncesModel.getAnnoncesById(userId)
            }

            userAnnoncesModel.isTurning.observe(requireActivity()) {
                userAnnoncesProgressbar.isVisible = it
            }
        }

        return binding.root
    }

    private fun goToOrderPage(orderId: String) {
        val action =
            UserAnnoncesFragmentDirections.actionUserAnnoncesFragmentToOrderPageFragment2(orderId)
        findNavController().navigate(action)
    }

    private fun returnToUserInfo() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToAnnonceModifyActivity(annonceId: String) {
        val intent = Intent(requireContext(), AnnonceModifyActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

}