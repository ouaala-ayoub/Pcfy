package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.models.HandleSubmitInterface
import alpha.company.pc.databinding.FragmentUserPolicyBinding
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

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
        readTextFile()

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
        Log.i(TAG, "User accepted term of usage")
    }

    override fun onBackClicked() {
        Log.i(TAG, "onBackClicked: going back to step three")
    }

    private fun readTextFile() {
        var string: String? = ""
        val stringBuilder = StringBuilder()
        val `is`: InputStream = this.resources.openRawResource(R.raw.app_terms)
        val reader = BufferedReader(InputStreamReader(`is`))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
            binding.userPolicyTv.text = stringBuilder
        }
        `is`.close()
    }

}