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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.FavouritesRepository
import com.example.pc.databinding.FragmentFavouritesBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.FavouritesModel
import com.example.pc.utils.toast

private const val FAVOURITE_DELETED_SUCCESS = "suprimée des favories avec succes"
private const val FAVOURITE_ERROR_MSG = "Erreur inattendue"
private const val TAG = "FavouritesFragment"

class FavouritesFragment : Fragment() {

    private var binding: FragmentFavouritesBinding? = null
    private lateinit var adapter: FavouritesAdapter
    private lateinit var userId: String
    private val retrofitService = RetrofitService.getInstance()
    private var viewModel = FavouritesModel(
        FavouritesRepository(
            retrofitService
        )
    )
    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authModel = AuthModel(retrofitService, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        return binding?.root
    }

    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        authModel.apply {
            auth(requireContext())
            auth.observe(viewLifecycleOwner) {
                if (isAuth()) {
                    Log.i(TAG, "isAuth: $it")

                    userId = getUserId()!!
                    Log.i(TAG, "user id: $userId")

                    adapter =
                        FavouritesAdapter(object : FavouritesAdapter.OnFavouriteClickListener {
                            override fun onFavouriteClicked(annonceId: String) {
                                goToAnnonceActivity(annonceId)
                            }

                            override fun onDeleteClickListener(annonceId: String) {
                                viewModel.apply {

                                    deleteFavourite(userId, annonceId)

                                }
                            }
                        })

                    binding!!.apply {
                        favouritesRv.layoutManager = LinearLayoutManager(activity)
                        favouritesRv.adapter = adapter

                        viewModel.apply {
                            getFavourites(userId)
                            favouritesListLiveData.observe(viewLifecycleOwner) { favourites ->
                                if (favourites == null) {
                                    requireContext().toast(FAVOURITE_ERROR_MSG, Toast.LENGTH_SHORT)
                                    reloadActivity()
                                } else {
                                    Log.i(TAG, "favourites : $favourites")
                                    adapter.setList(favourites)
                                    updateIsEmpty()
                                    emptyMsg.observe(viewLifecycleOwner) { isVisible ->
                                        isEmpty.text = isVisible
                                    }
                                }
                            }

                            deletedWithSuccess.observe(viewLifecycleOwner) { deletedWithSuccess ->
                                if (deletedWithSuccess) {
                                    requireContext().toast(
                                        FAVOURITE_DELETED_SUCCESS,
                                        Toast.LENGTH_SHORT
                                    )
                                } else {
                                    requireContext().toast(
                                        FAVOURITE_ERROR_MSG,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }

                            isProgressBarTurning.observe(viewLifecycleOwner) { isVisible ->
                                favouritesProgressBar.isVisible = isVisible
                            }
                            swiperefresh.setOnRefreshListener {
                                getFavourites(userId)
                                swiperefresh.isRefreshing = false
                            }
                        }
                    }

                } else {
                    binding!!.apply {
                        favouritesProgressBar.isVisible = false
                        noUserConnected.isVisible = true
                        loginFromUserInfo.apply {
                            isVisible = true
                            setOnClickListener {
                                goToLoginActivity()
                            }
                        }
                    }
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)

    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(this.context, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

//    private fun returnToHomeFragment() {
//        val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
//        findNavController().navigate(action)
//    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun reloadActivity() {
        val i = Intent(requireActivity(), MainActivity::class.java)
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
        startActivity(i)
        requireActivity().overridePendingTransition(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}