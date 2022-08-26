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
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.HomeRepository
import com.example.pc.databinding.FragmentHomeBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.adapters.AnnoncesAdapter
import com.example.pc.ui.viewmodels.HomeModel
import com.example.pc.ui.viewmodels.HomeModelFactory
import com.example.pc.utils.Token

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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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