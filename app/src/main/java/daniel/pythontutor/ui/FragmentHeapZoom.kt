package daniel.pythontutor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.adapter.HeapAdapter
import daniel.pythontutor.model.PythonVisualization.EncodedObject
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

        heapAdapter.setHeap(mapOf(0 to mEncodedObject))
        heapAdapter.setRoot(listOf(EncodedObject.Ref(0)))

        toolbar.setNavigationOnClickListener {
            requireFragmentManager().popBackStack()
        }
    }
}