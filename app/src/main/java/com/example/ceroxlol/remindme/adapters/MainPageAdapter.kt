package com.example.ceroxlol.remindme.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ceroxlol.remindme.fragments.AppointmentsListFragment
import com.example.ceroxlol.remindme.fragments.LocationsFragment

//TODO: Replace FragmentPagerAdapter
class MainPageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                AppointmentsListFragment()
            }
            1 -> {
                LocationsFragment()
            }
            else -> {
                AppointmentsListFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return "Appointments"
            }
            1 -> {
                return "Locations"
            }
        }
        return super.getPageTitle(position)
    }

}
