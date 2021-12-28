package adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ceroxlol.remindme.AppointmentsFragment
import com.example.ceroxlol.remindme.LocationsFragment

//TODO: Replace FragmentPagerAdapter
class MainPageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                AppointmentsFragment()
            }
            1 -> {
                LocationsFragment()
            }
            else -> {
                AppointmentsFragment()
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
