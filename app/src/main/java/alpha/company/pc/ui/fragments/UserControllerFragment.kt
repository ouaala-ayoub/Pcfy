package alpha.company.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import alpha.company.pc.databinding.FragmentUserControlerBinding
import alpha.company.pc.ui.activities.*

class UserControllerFragment : Fragment() {

    private lateinit var binding: FragmentUserControlerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity() as MainActivity
        activity.supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserControlerBinding.inflate(inflater, container, false)

        val args: UserControllerFragmentArgs by navArgs()
        val userId = args.userId

        binding.apply {
            userInfo.setOnClickListener {
                goToUserInfoModify(userId)
            }
            userAnnounces.setOnClickListener {
                goToUserAnnonces(userId)
            }
            orders.setOnClickListener {
                goToFullOrdersPage(userId)
            }
            buys.setOnClickListener {
                goToUserRequests(userId)
            }
            passwordChange.setOnClickListener {
                goToPasswordChange(userId)
            }
        }

        return binding.root
    }

    private fun goToPasswordChange(userId: String) {
        val action =
            UserControllerFragmentDirections.actionUserControlerFragmentToPasswordChangeFragment(
                userId
            )
        findNavController().navigate(action)
    }

    private fun goToFullOrdersPage(userId: String) {
        goToActivityWithUserId(userId, FullOrdersActivity::class.java)
    }

    private fun goToUserInfoModify(userId: String) {
        goToActivityWithUserId(userId, UserInfoModifyActivity::class.java)
    }

    private fun goToUserAnnonces(userId: String) {
        goToActivityWithUserId(userId, UserAnnoncesActivity::class.java)
    }

    private fun goToUserRequests(userId: String) {
        goToActivityWithUserId(userId, RequestsActivity::class.java)
    }

    private fun <T> goToActivityWithUserId(userId: String, activity: Class<T>) {
        val intent = Intent(requireContext(), activity)
        intent.putExtra("id", userId)
        startActivity(intent)
    }
}