package daniel.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject
import daniel.pythontutor.ui.ActivityMain

class PyListAdapter(private val activity: FragmentActivity, private val fragment: Fragment) :
    ListAdapter<Any, PyListAdapter.PyListViewHolder>(
        DIFF_CALLBACK
    ) {
    companion object {
        @JvmStatic
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem::class == newItem::class
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return false
            }
        }

        @JvmStatic
        private val TYPE_PRIMITIVE = 0
        @JvmStatic
        private val TYPE_OBJECT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PyListViewHolder {
        return if (viewType == TYPE_PRIMITIVE) {
            PyListTextViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_primitive_primitive, parent, false)
            )
        } else {
            PyListImageViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_primitive_object, parent, false)
            )
        }

    }

    override fun onBindViewHolder(holder: PyListViewHolder, position: Int) {
        holder.index.text = position.toString()
        if (getItemViewType(position) == TYPE_PRIMITIVE) {
            if (holder is PyListTextViewHolder) {
                holder.text.text = getItem(position).toString()
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is EncodedObject -> TYPE_OBJECT
        else -> TYPE_PRIMITIVE
    }

    abstract class PyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val index: TextView = itemView.findViewById(R.id.text1)
    }
    class PyListTextViewHolder(itemView: View) : PyListViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text2)
    }

    inner class PyListImageViewHolder(itemView: View) : PyListViewHolder(itemView),
        View.OnClickListener {
        val image : ImageButton = itemView.findViewById(R.id.zoom_in2)
        init {
            image.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (activity is ActivityMain) {
                activity.fragmentZoomInTransition(fragment)
            }
        }
    }
}
