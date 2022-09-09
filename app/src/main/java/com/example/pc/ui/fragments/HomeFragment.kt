package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
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
import com.example.pc.ui.viewmodels.HomeModelFactory

const val NUM_ROWS = 2
private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var annoncesList: MutableLiveData<List<Annonce>>
    private lateinit var adapter: AnnoncesAdapter
    private lateinit var viewModel: HomeModel
    private lateinit var annoncesRv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()
    private var binding: FragmentHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            HomeModelFactory(HomeRepository(retrofitService))
        )[HomeModel::class.java]

        adapter = AnnoncesAdapter(object: AnnoncesAdapter.OnAnnonceClickListener{
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val categories = CategoryEnum.values().map {
            category -> category.title
        }

        Log.i(TAG, " categories: $categories")

        val categoriesList = categories.map { categoryTitle ->
            val categoryImage = getCategoryImage(categoryTitle)
             Category(categoryImage, categoryTitle)
        }

        val categoryAdapter = CategoryAdapter(
            categoriesList,
            object: CategoryAdapter.OnCategoryClickedListener {
                override fun onCategoryClicked(title: String) {
                    Log.i(TAG, "onCategoryClicked: $title")
                }
            }
        )

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding!!.categoryRv.layoutManager = LinearLayoutManager(
            this.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding!!.categoryRv.adapter = categoryAdapter

        annoncesRv = binding!!.annonceRv
        annoncesRv.layoutManager = GridLayoutManager(this.context, NUM_ROWS)
        annoncesRv.adapter = adapter

        annoncesList = viewModel.getAnnoncesList()

        annoncesList.observe(viewLifecycleOwner){ annonces ->
            adapter.setAnnoncesList(annonces)
        }
        viewModel.isProgressBarTurning.observe(viewLifecycleOwner){
            binding!!.homeProgressBar.isVisible = it
        }

        return binding?.root
    }

    private fun getCategoryImage(categoryTitle: String): Int {

        when(categoryTitle) {
            //to do
        }

        return R.drawable.ic_launcher_background
    }

    private fun goToAnnonceActivity(annonceId: String){
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}