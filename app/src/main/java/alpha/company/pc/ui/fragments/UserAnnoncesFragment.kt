package alpha.company.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Tokens
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.FragmentUserAnnoncesBinding
import alpha.company.pc.ui.activities.AnnonceModifyActivity
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.activities.UserAnnoncesActivity
import alpha.company.pc.ui.adapters.FavouritesAdapter
import alpha.company.pc.ui.adapters.OrdersShortAdapter
import alpha.company.pc.ui.viewmodels.SingleAnnounceCommandModel
import alpha.company.pc.ui.viewmodels.UserAnnoncesModel
import alpha.company.pc.utils.LocalStorage
import alpha.company.pc.utils.OnDialogClicked
import alpha.company.pc.utils.makeDialog
import alpha.company.pc.utils.toast

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
        ).also {
            it.initialiseAdd(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        currentTokens = LocalStorage.getTokens(requireActivity())
        Log.d(TAG, "currentTokens: $currentTokens")
        binding = FragmentUserAnnoncesBinding.inflate(inflater, container, false)

//        userAnnoncesModel.showAdd(requireContext())

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
                                    Log.d(TAG, "currentTokens: $currentTokens")
                                    deleteAnnonce(currentTokens, annonceId)
                                }
                            }

                            override fun onNegativeButtonClicked() {}
                        },
                        getString(R.string.annonce_delete_dialog_title),
                        getString(R.string.annonce_delete_dialog_message)
                    ).show()
                }
            },
            object : OrdersShortAdapter.OnOrderClicked {
                override fun onOrderClicked(orderId: String) {
                    // go to order page
                    goToOrderPage(orderId)
                }
            },
            object : FavouritesAdapter.OnCommandsClicked {
                override fun onCommandClicked(
                    annonceId: String,
                    empty: TextView,
                    orderRv: RecyclerView,
                    onOrderClicked: OrdersShortAdapter.OnOrderClicked?
                ) {
                    val singleCommandModel =
                        SingleAnnounceCommandModel(UserInfoRepository(retrofitService))

                    val adapter = OrdersShortAdapter(onOrderClicked!!)

                    if (empty.visibility == View.GONE && orderRv.visibility == View.GONE) {
                        singleCommandModel.apply {
                            getAnnonceOrders(annonceId)
                            ordersList.observe(viewLifecycleOwner) { orders ->
                                if (orders != null) {
                                    if (orders.isEmpty()) {
                                        empty.visibility = View.VISIBLE
                                    } else {
                                        orderRv.apply {
                                            this.adapter = adapter
                                            layoutManager = LinearLayoutManager(requireContext())
                                            adapter.setOrdersList(orders)
                                            visibility = View.VISIBLE
                                        }
                                    }
                                } else {
                                    requireContext().toast(
                                        ORDERS_ERROR,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    } else {
                        if (empty.visibility == View.VISIBLE) {
                            empty.visibility = View.GONE
                        } else if (orderRv.visibility == View.VISIBLE) {
                            orderRv.visibility = View.GONE
                        }
                    }


                    singleCommandModel.isTurning.observe(viewLifecycleOwner) { isLoading ->
                        binding.userAnnoncesProgressbar.isVisible = isLoading
                    }

                }
            },

            )

        userAnnoncesModel.apply {
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
                swiperefresh.isRefreshing = false
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