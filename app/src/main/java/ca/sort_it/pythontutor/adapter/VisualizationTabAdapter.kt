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

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.sort_it.pythontutor.ui.FragmentHeap
import ca.sort_it.pythontutor.ui.FragmentStack
import ca.sort_it.pythontutor.ui.FragmentStdout

class VisualizationTabAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val mStdoutFragment = FragmentStdout()
    private val mStackFragment = FragmentStack()
    private val mHeapFragment = FragmentHeap()

    override fun getItem(position: Int) =
        when (position) {
            0 -> mStdoutFragment
            1 -> mStackFragment
            else -> mHeapFragment
        }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) =
        when (position) {
            0 -> "StdOut"
            1 -> "Stack"
            else -> "Heap"
        }
}
