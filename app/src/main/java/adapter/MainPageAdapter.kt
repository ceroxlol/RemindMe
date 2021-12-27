package adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ceroxlol.remindme.AppointmentsFragment
import com.example.ceroxlol.remindme.LocationsFragment

class MainPageAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> {
                return AppointmentsFragment()
            }
            1 -> {
                return LocationsFragment()
            }
            else -> {
                return AppointmentsFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
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
