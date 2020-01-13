package daniel.pythontutor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.adapter.HeapAdapter
import daniel.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.heap.*

class FragmentHeap : Fragment() {
    private lateinit var mViewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)
    }

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

        mViewModel.getHeapRoot().observe(this, Observer {
            heapAdapter.setRoot(it)
            if (heapAdapter.itemCount >= 1) {
                heap_layout.smoothScrollToPosition(heapAdapter.itemCount - 1)
            }
        })

        mViewModel.getHeap().observe(this, Observer {
            heapAdapter.setHeap(it)
            if (heapAdapter.itemCount >= 1) {
                heap_layout.smoothScrollToPosition(heapAdapter.itemCount - 1)
            }
        })
    }
}
