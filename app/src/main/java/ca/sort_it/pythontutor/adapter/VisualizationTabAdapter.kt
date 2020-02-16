/*
 *     Copyright (c) 2020 danielzhang130
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.sort_it.pythontutor.adapter

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.ui.FragmentHeap
import ca.sort_it.pythontutor.ui.FragmentStack
import ca.sort_it.pythontutor.ui.FragmentStdout

class VisualizationTabAdapter(fm: FragmentManager, val context: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) =
        when (position) {
            0 -> FragmentStdout()
            1 -> FragmentStack()
            2 -> FragmentHeap()
            else -> throw IllegalArgumentException("requested item $position in getItem")
        }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) =
        when (position) {
            0 -> context.getString(R.string.stdout)
            1 -> context.getString(R.string.stack)
            2 -> context.getString(R.string.heap)
            else -> throw IllegalArgumentException("request item $position in getPageTitle")
        }
}
