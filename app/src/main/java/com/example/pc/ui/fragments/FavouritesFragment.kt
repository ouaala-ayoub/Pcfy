package com.example.pc.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.FavouritesRepository
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentFavouritesBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.adapters.FavouritesAdapter
import com.example.pc.ui.viewmodels.FavouritesModel
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
import com.example.pc.utils.toast

private const val FAVOURITE_DELETED_SUCCESS = "suprimée des favories avec succes"
private const val FAVOURITE_ERROR_MSG = "Erreur Inatendue"
private const val TAG = "FavouritesFragment"

class FavouritesFragment : Fragment() {

    private var binding: FragmentFavouritesBinding? = null
    private lateinit var adapter: FavouritesAdapter
    private var userId: String? = null
    private var alertDialog: AppCompatDialog? = null
    private var viewModel = FavouritesModel(FavouritesRepository(
        RetrofitService.getInstance()
    ))

    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var loginRepository: LoginRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginRepository = LoginRepository(
            RetrofitService.getInstance(),
            requireContext().applicationContext
        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginRepository.isLoggedIn.observe(viewLifecycleOwner) { isLogged ->

            if (loginRepository.user == null){
                Log.i(TAG, "isLogged in : $isLogged")
                makeDialog(
                    requireActivity(),
                    object: OnDialogClicked {
                        override fun onPositiveButtonClicked() {
                            goToLoginActivity()
                        }

                        override fun onNegativeButtonClicked() {
                            requireActivity().finish()
                            reloadActivity()
//                            returnToHomeFragment()
                        }
                    },
                    getString(R.string.confirm_login_title),
                    getString(R.string.confirm_login_message)
                ).show()
            }

            else {
                Log.i(TAG, "other case isLogged in : $isLogged")
                Log.i(TAG, "user: ${loginRepository.user}")
                if (loginRepository.user == null) return@observe
                userId = loginRepository.user!!.userId
                Log.i(TAG, "user id : $userId")

                adapter = FavouritesAdapter(object : FavouritesAdapter.OnFavouriteClickListener{
                    override fun onFavouriteClicked(annonceId: String) {
                        goToAnnonceActivity(annonceId)
                    }
                    override fun onDeleteClickListener(annonceId: String) {
                        viewModel.deleteFavourite(userId!!, annonceId).observe(viewLifecycleOwner){deletedWithSuccess ->
                            if(deletedWithSuccess) {
                                requireContext().toast(FAVOURITE_DELETED_SUCCESS, Toast.LENGTH_SHORT)
                            }
                            else {
                                requireContext().toast(FAVOURITE_ERROR_MSG, Toast.LENGTH_SHORT)
                            }
                        }
                    }
                }, viewModel)

                binding!!.apply {
                    favouritesRv.layoutManager = LinearLayoutManager(activity)
                    favouritesRv.adapter = adapter

                    viewModel.apply {
                        getFavourites(userId!!).observe(viewLifecycleOwner){ favourites ->
                            if (favourites == null) {
                                requireContext().toast(FAVOURITE_ERROR_MSG, Toast.LENGTH_SHORT)
                                returnToHomeFragment()
                            }
                            else{
                                Log.i(TAG, "favourites : $favourites")
                                adapter.setFavouritesList(favourites)
                                updateIsEmpty().observe(viewLifecycleOwner){ isVisible ->
                                    isEmpty.isVisible = isVisible
                                }
                            }
                        }
                        isProgressBarTurning.observe(viewLifecycleOwner){ isVisible ->
                            favouritesProgressBar.isVisible = isVisible
                        }
                    }
                }
            }

        }

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
        if (alertDialog != null && alertDialog!!.isShowing) {
            Log.i(TAG, "onDestroy: ${alertDialog?.isShowing}")
            alertDialog!!.dismiss()
        }
    }

//    class PurchaseConfirmationDialogFragment : DialogFragment() {
//        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
//            AlertDialog.Builder(requireContext())
//                .setTitle(R.string.confirm_login_title)
//                .setMessage(getString(R.string.confirm_login_message))
//                .setPositiveButton(getString(R.string.Oui)) { _, _ ->
//
//                }
//                .setNegativeButton(getString(R.string.Cancel)){ _, _ ->
//
//                }
//                .create()
//
//        companion object {
//            const val TAG = "PurchaseConfirmationDialog"
//        }
//    }
}