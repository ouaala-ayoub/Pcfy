package com.example.pc.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.FOCUS_UP
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.models.network.Status
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.SearchRepository
import com.example.pc.databinding.FragmentSearchBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.AnnoncesAdapter
import com.example.pc.ui.viewmodels.SearchModel
import com.example.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val NUM_ROWS = 2
private const val TAG = "SearchFragment"
private const val SEARCH_ERROR = "Erreur inattendue"
private const val MAX_SEARCH_PRICE = 20000
private const val WHATEVER = "-"

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var rvAdapter: AnnoncesAdapter
    private lateinit var viewModel: SearchModel
    private var priceQuery: Int? = null
    private var statusQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        rvAdapter = AnnoncesAdapter(object : AnnoncesAdapter.OnAnnonceClickListener {
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }
        })
        viewModel = SearchModel(SearchRepository(RetrofitService.getInstance()), MAX_SEARCH_PRICE)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.apply {

            searchView.onActionViewExpanded()

            setUpStatusEditText(Status.NEW.status)

            searchRv.apply {
                this.adapter = rvAdapter
                this.layoutManager = GridLayoutManager(requireContext(), NUM_ROWS)
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.apply {
                        statusQuery = getStatusQuery()
                        search(query, priceQuery, statusQuery)
                    }

                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.apply {
                        statusQuery = getStatusQuery()
                        search(newText, priceQuery, statusQuery)
                    }
                    return false
                }
            })

            viewModel.apply {
                searchResult.observe(viewLifecycleOwner) { searchResult ->
                    if (searchResult != null) {
                        rvAdapter.setAnnoncesList(searchResult)
                    } else {
                        doOnFail(SEARCH_ERROR)
                    }
                    updateSearchMessage()
                }

                priceRange.addOnChangeListener { slider, value, fromUser ->
                    Log.i(TAG, "getMaxPrice slider: $value")
                    getMaxPrice(value)

                    Log.i(
                        TAG,
                        "setUpStatusEditText search query : ${binding.searchView.query},price : $priceQuery,status : $statusQuery"
                    )
                }

                searchMessage.observe(viewLifecycleOwner) { msg ->
                    messageSearch.text = msg
                }
                isTurning.observe(viewLifecycleOwner) { isTurning ->
                    searchProgressBar.isVisible = isTurning
                }
                currentMax.observe(viewLifecycleOwner) { max ->
                    val price = max.toInt()
                    priceQuery = price
                    maxPrice.text = price.toString()
                    val searchQuery = searchView.query.toString()
                    if (searchQuery.isNotBlank()) {
                        search(searchQuery, priceQuery, statusQuery)
                    }
                    Log.i(
                        TAG,
                        "setUpStatusEditText search query : ${binding.searchView.query},price : $priceQuery,status : $statusQuery"
                    )
                }
            }
        }

        return binding.root
    }

    private fun setUpStatusEditText(default: String?) {

        binding.statusEditText.setText(default)

        val values = Status.values().map { status ->
            status.status
        }.toMutableList().apply { add(WHATEVER) }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)

        binding.statusEditText.doOnTextChanged { text, _, _, _ ->
            statusQuery = getStatusQuery()
            val searchQuery = binding.searchView.query.toString()
            if (searchQuery.isNotBlank()){
                viewModel.search(searchQuery, priceQuery, statusQuery)
            }
            Log.i(
                TAG,
                "setUpStatusEditText search query : ${binding.searchView.query},price : $priceQuery,status : $statusQuery"
            )
        }
    }

    fun getStatusQuery(): String? {
        val statusQuery: String? = when (val status = binding.statusEditText.text.toString()) {
            WHATEVER -> {
                null
            }
            else -> {
                status
            }
        }
        return statusQuery
    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
    }

}