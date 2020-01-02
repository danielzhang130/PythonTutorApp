package daniel.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject

class HeapAdapter(private val activity: FragmentActivity, private val fragment: Fragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mHeap = emptyMap<Int, EncodedObject>()
    private var mRoot = emptyList<EncodedObject.Ref>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(activity.layoutInflater.inflate(R.layout.heap_list, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val encodedObject = mHeap[mRoot[position].id]
        if (holder is ListViewHolder) {
            holder.label.text = encodedObject?.let { it::class.simpleName }
            if (encodedObject is EncodedObject.PyDict) {
                holder.recyclerView.adapter = PyMapAdapter(activity)
                (holder.recyclerView.adapter as PyMapAdapter).submitList(encodedObject.elements)
            } else {
                holder.recyclerView.adapter = PyListAdapter(activity, fragment)
            }
            if (encodedObject is EncodedObject.PyList) {
                (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements)
            }
            if (encodedObject is EncodedObject.PySet) {
                (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements.toList())
            }
            if (encodedObject is EncodedObject.PyTuple) {
                (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements)
            }
        }
    }

    override fun getItemCount(): Int {
        return mRoot.size
    }

    fun setRoot(root: List<EncodedObject.Ref>) {
        mRoot = root
        notifyDataSetChanged()
    }

    fun setHeap(heap: Map<Int, EncodedObject>) {
        mHeap = heap
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.type)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.elements)
        init {
            recyclerView.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        }
    }
}
