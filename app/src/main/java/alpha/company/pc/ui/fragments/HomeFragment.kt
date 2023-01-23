package alpha.company.pc.ui.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.models.network.CategoryEnum
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.HomeRepository
import alpha.company.pc.databinding.FragmentHomeBinding
import alpha.company.pc.ui.activities.AnnonceActivity
import alpha.company.pc.ui.adapters.AnnoncesAdapter
import alpha.company.pc.ui.adapters.CategoryAdapter
import alpha.company.pc.ui.adapters.PopularsAdapter
import alpha.company.pc.ui.viewmodels.HomeModel
import alpha.company.pc.utils.ERROR_MSG
import com.google.android.gms.ads.AdRequest

private const val NUM_ROWS = 2
private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var annoncesToShow = mutableListOf<Annonce>()
    private lateinit var annoncesAdapter: AnnoncesAdapter
    private lateinit var popularsAdapter: PopularsAdapter
    private lateinit var viewModel: HomeModel
    private val retrofitService = RetrofitService.getInstance()
    private var binding: FragmentHomeBinding? = null
    private lateinit var categoriesList: List<alpha.company.pc.data.models.local.Category>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        categoriesList = CategoryEnum.values().map { category ->
            category.title
        }.map { categoryTitle ->
            alpha.company.pc.data.models.local.Category(categoryTitle)
        }

        viewModel = HomeModel(HomeRepository(retrofitService))

        val onClickListener = object : AnnoncesAdapter.OnAnnonceClickListener {
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }

            override fun onAnnonceLoadFail() {
//                findNavController().popBackStack()
                Log.e(TAG, "onAnnonceLoadFail : something went wrong with loading the annonce")
            }
        }
        annoncesAdapter = AnnoncesAdapter(onClickListener)
        popularsAdapter = PopularsAdapter(onClickListener)
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
                            annoncesToShow = mutableListOf()
                        }
                    } else {
                        viewModel.apply {
                            getAnnoncesByCategory(title)
                            annoncesToShow = mutableListOf()
                        }
                    }
                }
            }
        )

        binding!!.apply {

            val adRequest = AdRequest.Builder().build()
            Log.d(TAG, "adRequest: $adRequest")
            adView?.loadAd(adRequest)

            var multiplier = 1
            val orientation = resources.configuration.orientation
            val isTablet = resources.getBoolean(R.bool.isTablet)

            if (isTablet) {
                multiplier *= 2
            }
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                multiplier *= 2
            }

            vAppBar.addOnOffsetChangedListener { _, verticalOffset ->
                val isScreenOnTop = verticalOffset == 0
                Log.d(TAG, "addOnOffsetChangedListener isScreenOnTop : $isScreenOnTop")
                swiperefresh.isEnabled = isScreenOnTop
            }
            popularsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val isRvDragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    swiperefresh.isEnabled = !isRvDragging
                    Log.d(TAG, "onScrollStateChanged isRvDragging : $isRvDragging")
                }
            })

            //setting the categories list
            categoryRv.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = categoryAdapter
            }

            //setting the annonces list
            annonceRv.apply {
                layoutManager = GridLayoutManager(requireContext(), NUM_ROWS * multiplier)
                adapter = annoncesAdapter
            }

            //setting the popular annonces list
            popularsRv.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = popularsAdapter
            }
        }

        viewModel.apply {
            getPopularAnnonces()
            getAnnoncesListAll()
            annoncesList.observe(viewLifecycleOwner) { annonces ->

                if (annonces != null) {
                    for (annonce in annonces) {
                        annoncesToShow.add(annonce)
                    }
                    annoncesAdapter.setAnnoncesList(annoncesToShow)
                } else {
                    Log.e(TAG, "annoncesList is $annonces")
                }

                updateIsEmpty()
                emptyMsg.observe(viewLifecycleOwner) { msg ->
                    Log.i(TAG, "updateIsEmpty: $msg")
                    if (msg.isEmpty()) {
                        binding!!.noAnnonce.visibility = View.GONE
                    } else {
                        if (msg == ERROR_MSG) {
                            binding!!.apply {
                                popularTv.visibility = View.GONE
                                foruTv.visibility = View.GONE
                            }
                        }
                        binding!!.noAnnonce.visibility = View.VISIBLE
                        binding!!.noAnnonce.text = msg
                    }
                }

            }

            popularsList.observe(viewLifecycleOwner) { populars ->
                if (populars != null) {
                    val popularsList = populars.map { popular -> popular.title }
                    Log.d(TAG, "popularsList: $popularsList")
                    popularsAdapter.setPopularsList(populars)
                } else {
                    Log.e(TAG, "popularsList is : $populars")
                }
            }

            binding!!.apply {
                swiperefresh.setOnRefreshListener {
                    val current = categoryAdapter.getCurrentCategory()
                    annoncesToShow = if (current == CategoryEnum.ALL.title) {
                        getPopularAnnonces()
                        getAnnoncesListAll()
                        mutableListOf()
                    } else {
                        getPopularAnnonces()
                        getAnnoncesByCategory(current)
                        mutableListOf()
                    }
                    swiperefresh.isRefreshing = false
                }
                annonceRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) &&
                            newState == RecyclerView.SCROLL_STATE_IDLE &&
                            annoncesToShow.isNotEmpty()
                        ) {
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