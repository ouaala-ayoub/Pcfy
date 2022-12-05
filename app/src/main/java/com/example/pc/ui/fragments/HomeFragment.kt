package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Category
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.CategoryEnum
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.HomeRepository
import com.example.pc.databinding.FragmentHomeBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.AnnoncesAdapter
import com.example.pc.ui.adapters.CategoryAdapter
import com.example.pc.ui.viewmodels.HomeModel

private const val NUM_ROWS = 2
private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var annoncesToShow = mutableListOf<Annonce>()
    private lateinit var annoncesAdapter: AnnoncesAdapter
    private lateinit var viewModel: HomeModel
    private lateinit var annoncesRv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()
    private var binding: FragmentHomeBinding? = null
    private lateinit var categoriesList: List<Category>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        categoriesList = CategoryEnum.values().map { category ->
            category.title
        }.map { categoryTitle ->
            Category(categoryTitle)
        }

        viewModel = HomeModel(HomeRepository(retrofitService))

        annoncesAdapter = AnnoncesAdapter(object : AnnoncesAdapter.OnAnnonceClickListener {
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }

            override fun onAnnonceLoadFail() {
                findNavController().popBackStack()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val categoryAdapter = CategoryAdapter(
            categoriesList,
            object : CategoryAdapter.OnCategoryClickedListener {
                override fun onCategoryClicked(title: String) {
                    if (title == CategoryEnum.ALL.title) {
                        viewModel.apply {
                            getAnnoncesListAll()
                        }
                    } else {
                        viewModel.apply {
                            getAnnoncesByCategory(title)
                        }
                    }
                }
            }
        )

        binding!!.categoryRv.layoutManager = LinearLayoutManager(
            this.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding!!.categoryRv.adapter = categoryAdapter

        annoncesRv = binding!!.annonceRv
        annoncesRv.apply {
            layoutManager = GridLayoutManager(this.context, NUM_ROWS)
            this.adapter = annoncesAdapter
        }

        viewModel.apply {
            getAnnoncesListAll()
            annoncesList.observe(viewLifecycleOwner) { annonces ->

                if (annonces != null)
                    for (annonce in annonces){
                        annoncesToShow.add(annonce)
                    }
                    annoncesAdapter.setAnnoncesList(annoncesToShow)

                updateIsEmpty()
                emptyMsg.observe(viewLifecycleOwner) { msg ->
                    Log.i(TAG, "updateIsEmpty: $msg")
                    binding!!.noAnnonce.text = msg
                }

            }

            binding!!.apply {
                swiperefresh.setOnRefreshListener {
                    val current = categoryAdapter.getCurrentCategory()
                    annoncesToShow = if (current == CategoryEnum.ALL.title) {
                        getAnnoncesListAll()
                        mutableListOf()
                    } else {
                        getAnnoncesByCategory(current)
                        mutableListOf()
                    }
                    swiperefresh.isRefreshing = false
                }
                annoncesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Log.i(TAG, "end")
                            val current = categoryAdapter.getCurrentCategory()
                            if (current == CategoryEnum.ALL.title) {
                                getAnnoncesListAll()
                            } else {
                                getAnnoncesByCategory(current)
                            }
                        }
                    }
                })
            }

            isProgressBarTurning.observe(viewLifecycleOwner) {
                binding!!.homeProgressBar.isVisible = it
            }

        }
        return binding?.root
    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}