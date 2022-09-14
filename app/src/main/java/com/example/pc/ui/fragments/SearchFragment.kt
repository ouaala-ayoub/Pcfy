package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.SearchRepository
import com.example.pc.databinding.FragmentSearchBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.AnnoncesAdapter
import com.example.pc.ui.viewmodels.SearchModel
import com.example.pc.utils.toast

private const val NUM_ROWS = 2
private const val TAG = "SearchFragment"
private const val SEARCH_ERROR = "Erreur inattendue"

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var rvAdapter: AnnoncesAdapter
    private lateinit var viewModel: SearchModel

    override fun onCreate(savedInstanceState: Bundle?) {
        rvAdapter = AnnoncesAdapter(object: AnnoncesAdapter.OnAnnonceClickListener{
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }
        })
        viewModel = SearchModel(SearchRepository(RetrofitService.getInstance()
        ))
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.apply {

            searchRv.apply {
                this.adapter = rvAdapter
                this.layoutManager = GridLayoutManager(requireContext(), NUM_ROWS)
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.apply {
                        search(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.apply {
                        search(newText)
                    }
                    return false
                }
            })

            viewModel.apply {
                searchResult.observe(viewLifecycleOwner) { searchResult ->
                    if (searchResult != null){
                        rvAdapter.setAnnoncesList(searchResult)
                    }
                    else {
                        doOnFail(SEARCH_ERROR)
                    }
                    updateSearchMessage()
                }
                searchMessage.observe(viewLifecycleOwner){ msg ->
                    messageSearch.text = msg
                }
                isTurning.observe(viewLifecycleOwner){ isTurning ->
                    searchProgressBar.isVisible = isTurning
                }
            }
        }

        return binding.root
    }

    private fun goToAnnonceActivity(annonceId: String){
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    private fun doOnFail(message: String){
        requireContext().toast(message, Toast.LENGTH_SHORT)
    }

}