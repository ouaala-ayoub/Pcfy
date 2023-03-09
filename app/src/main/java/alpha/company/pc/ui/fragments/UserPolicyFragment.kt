package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.models.HandleSubmitInterface
import alpha.company.pc.databinding.FragmentUserPolicyBinding
import alpha.company.pc.utils.readTextFile
import android.util.Log
import android.widget.Button

private const val TAG = "UserPolicyFragment"

class UserPolicyFragment : Fragment(), HandleSubmitInterface {

    private lateinit var binding: FragmentUserPolicyBinding
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserPolicyBinding.inflate(inflater, container, false)
        binding.userPolicyTv.text = readTextFile(R.raw.app_terms, requireContext())

        nextButton = requireActivity().findViewById(R.id.next)
        nextButton.apply {
            isEnabled = false
            binding.acceptCb.setOnCheckedChangeListener { _, isChecked -> isEnabled = isChecked }
        }

        return binding.root

    }

    override fun onResume() {
        super.onResume()
        nextButton.isEnabled = binding.acceptCb.isChecked
    }

    override fun onNextClicked() {
        binding.acceptCb.isEnabled = false
        Log.i(TAG, "User accepted term of usage")
    }

    override fun onBackClicked() {
        Log.i(TAG, "onBackClicked: going back to step three")
    }


}