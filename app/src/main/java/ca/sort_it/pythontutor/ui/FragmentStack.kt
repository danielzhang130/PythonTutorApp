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

package ca.sort_it.pythontutor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.StackAdapter
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.stack.*
import javax.inject.Inject

class FragmentStack : BaseFragment() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel by activityViewModels<MainViewModel> { mViewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.stack, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val stackAdapter = StackAdapter(requireContext())
        (stack_layout.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        stack_layout.layoutManager = LinearLayoutManager(context)
        stack_layout.adapter = stackAdapter

        mViewModel.globals.observe(viewLifecycleOwner, Observer {
            stackAdapter.setGlobal(it)
            stack_layout.scrollToPosition(0)
        })
        mViewModel.stack.observe(viewLifecycleOwner, Observer {
            stackAdapter.setStack(it)
            stack_layout.scrollToPosition(0)
        })
    }
}
