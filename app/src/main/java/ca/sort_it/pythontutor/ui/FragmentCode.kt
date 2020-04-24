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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.CodeAdapter
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.code_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class FragmentCode @Inject constructor(): BaseFragment() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel by activityViewModels<MainViewModel> { mViewModelFactory }
    private lateinit var mAdapter: CodeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.code_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = CodeAdapter(layoutInflater,
            ContextCompat.getColor(requireActivity(), R.color.code_blue),
            ContextCompat.getColor(requireActivity(), R.color.code_orange),
            Color.TRANSPARENT)

        code_list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
                .apply { setCode(mViewModel.getLines()) }
        }

        var currentLine: Int
        mViewModel.currentLine.observe(viewLifecycleOwner, Observer {
            mAdapter.setCurrentLine(it)
            currentLine = it
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                if (currentLine != it) {
                    return@launch
                }
                code_list.scrollToPosition(it)
            }
        })
        mViewModel.prevLine.observe(viewLifecycleOwner, Observer {
            mAdapter.setPrevLine(it)
        })

    }
}
