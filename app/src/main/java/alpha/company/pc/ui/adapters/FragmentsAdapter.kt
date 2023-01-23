package alpha.company.pc.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import alpha.company.pc.ui.fragments.UserStepOne
import alpha.company.pc.ui.fragments.UserStepThree
import alpha.company.pc.ui.fragments.UserStepTwo

class FragmentsAdapter(fa: FragmentActivity, private val fragmentList: List<Fragment>) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun onNextClicked(position: Int) {

        val current = fragmentList[position]
        val castedFrag: Fragment

        when (position) {
            0 -> {
                castedFrag = current as UserStepOne
                castedFrag.onNextClicked()
            }
            1 -> {
                castedFrag = current as UserStepTwo
                castedFrag.onNextClicked()
            }
            2 -> {
                castedFrag = current as UserStepThree
                castedFrag.onNextClicked()
            }
        }

    }

    fun onBackClicked(position: Int) {

        val current = fragmentList[position]
        val castedFrag: Fragment

        if(position == 1){
            castedFrag = current as UserStepTwo
            castedFrag.onBackClicked()
        }

    }
}