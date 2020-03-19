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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.HeapAdapter
import ca.sort_it.pythontutor.model.PythonVisualization.EncodedObject
import kotlinx.android.synthetic.main.heap_zoom.*

class FragmentHeapZoom : Fragment() {
    companion object {
        private const val KEY_OBJECT = "object"
        fun newInstance(encodedObject: EncodedObject) =
            FragmentHeapZoom().also {
                val b = Bundle()
                b.putSerializable(KEY_OBJECT, encodedObject)
                it.arguments = b
            }
    }

    private lateinit var mEncodedObject: EncodedObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mEncodedObject = it.getSerializable(KEY_OBJECT) as EncodedObject
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.heap_zoom, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val heapAdapter = HeapAdapter(this)
        heap_layout.layoutManager = LinearLayoutManager(context)
        heap_layout.adapter = heapAdapter
        (heap_layout.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        heapAdapter.setHeap(mapOf(0 to mEncodedObject))
        heapAdapter.setRoot(listOf(EncodedObject.Ref(0)))

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}