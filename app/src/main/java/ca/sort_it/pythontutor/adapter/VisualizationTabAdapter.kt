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
import ca.sort_it.pythontutor.ui.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class VisualizationTabAdapter(fm: FragmentManager, val context: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        sealed class TabType (val kClass: KClass<out BaseFragment>, val stringResource: Int) {
            object CODE : TabType(FragmentCode::class, R.string.code)
            object STDOUT : TabType(FragmentStdout::class, R.string.stdout)
            object STACK : TabType(FragmentStack::class, R.string.stack)
            object HEAP : TabType(FragmentHeap::class, R.string.heap)
        }
    }

    private val mTabs = mutableListOf<TabType>()

    override fun getItem(position: Int) =
        mTabs[position].kClass.createInstance()

    override fun getCount() = mTabs.size

    override fun getPageTitle(position: Int) =
        context.getString(mTabs[position].stringResource)

    fun addTab(tab: TabType) {
        mTabs.add(tab)
        notifyDataSetChanged()
    }

    fun removeTab(position: Int) {
        mTabs.removeAt(position)
        notifyDataSetChanged()
    }
}
