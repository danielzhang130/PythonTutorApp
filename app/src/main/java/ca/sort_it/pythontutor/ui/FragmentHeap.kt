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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.HeapAdapter
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.heap.*
import javax.inject.Inject

class FragmentHeap : BaseFragment() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel by activityViewModels<MainViewModel> { mViewModelFactory }
    private var mCurrentAnimator: AnimatorSet? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.heap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val heapAdapter = HeapAdapter(this)
        heap_layout.layoutManager = LinearLayoutManager(context)
        heap_layout.adapter = heapAdapter
        (heap_layout.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        mViewModel.heapRoot.observe(viewLifecycleOwner, Observer {
            heapAdapter.setRoot(it)
            if (heapAdapter.itemCount >= 1) {
                heap_layout.smoothScrollToPosition(heapAdapter.itemCount - 1)
            }
        })

        mViewModel.heap.observe(viewLifecycleOwner, Observer {
            heapAdapter.setHeap(it)
            if (heapAdapter.itemCount >= 1) {
                heap_layout.smoothScrollToPosition(heapAdapter.itemCount - 1)
            }
        })

        mViewModel.goToHeapState.observe(viewLifecycleOwner, Observer {
            if (it is Int) {
                val index = heapAdapter.findRef(it)
                if (index >= 0) {
                    heap_layout.scrollToPosition(index)

                    Handler().postDelayed({
                        highlightItem(index)
                        mViewModel.goToHeapHandled()
                    }, 200)
                }
            }
        })
    }

    private fun highlightItem(index: Int) {
        heap_layout.findViewHolderForAdapterPosition(index)?.itemView?.let { target ->
            mCurrentAnimator?.cancel()
            mCurrentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(target, View.TRANSLATION_X, 0f, 20f, -20f, 20f, -20f, 0f))
                duration = 700
                interpolator = BounceInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        mCurrentAnimator = null
                        target.translationX = 0f

                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        mCurrentAnimator = null
                        target.translationX = 0f
                    }
                })
                start()
            }
        }
    }
}
