package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.FavouritesRepository
import com.example.pc.databinding.FragmentFavouritesBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.viewmodels.FavouritesModel
import com.example.pc.utils.toast

private const val userId = "62e7fa498dd9b229c8057014"
private const val FAVOURITE_DELETED_SUCCESS = "suprimée des favories avec succes"
private const val FAVOURITE_ERROR_MSG = "Erreur Inatendue"

class FavouritesFragment : Fragment() {
    private var binding: FragmentFavouritesBinding? = null
    private lateinit var adapter: FavouritesAdapter
    private var viewModel = FavouritesModel(FavouritesRepository(
        RetrofitService.getInstance()
    ))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = FavouritesAdapter(object : FavouritesAdapter.OnFavouriteClickListener{
            override fun onFavouriteClicked(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }

            override fun onDeleteClickListener(annonceId: String) {
                viewModel.deleteFavourite(userId, annonceId).observe(viewLifecycleOwner){deletedWithSuccess ->
                    if(deletedWithSuccess) {
                        requireContext().toast(FAVOURITE_DELETED_SUCCESS, Toast.LENGTH_SHORT)
                    }
                    else {
                        requireContext().toast(FAVOURITE_ERROR_MSG, Toast.LENGTH_SHORT)
                    }
                }
            }
        }, viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        binding!!.apply {
            favouritesRv.layoutManager = LinearLayoutManager(activity)
            favouritesRv.adapter = adapter

            viewModel.apply {
                getFavourites(userId).observe(viewLifecycleOwner){ favourites ->
                    if (favourites == null) {
                        requireContext().toast(FAVOURITE_ERROR_MSG, Toast.LENGTH_SHORT)
                        returnToHomeFragment()
                    }
                    else{
                        adapter.setFavouritesList(favourites)
                    }
                }
                isProgressBarTurning.observe(viewLifecycleOwner){ isVisible ->
                    favouritesProgressBar.isVisible = isVisible
                }

            }
        }

        return binding?.root
    }

    private fun goToAnnonceActivity(annonceId: String){
        val intent = Intent(this.context, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }
    private fun returnToHomeFragment(){
        val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}