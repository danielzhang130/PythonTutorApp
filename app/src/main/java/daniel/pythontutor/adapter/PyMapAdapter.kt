package daniel.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject

class PyMapAdapter(private val activity: FragmentActivity) :
    ListAdapter<Pair<Any, Any>, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    companion object {
        @JvmStatic
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pair<Any, Any>>() {
            override fun areItemsTheSame(oldItem: Pair<Any, Any>, newItem: Pair<Any, Any>) = false

            override fun areContentsTheSame(oldItem: Pair<Any, Any>, newItem: Pair<Any, Any>): Boolean {
                return oldItem == newItem
            }
        }

        @JvmStatic
        private val TYPE_PRIMITIVE_PRIMITIVE = 0
        @JvmStatic
        private val TYPE_PRIMITIVE_OBJECT = 1
        @JvmStatic
        private val TYPE_OBJECT_PRIMITIVE = 2
        @JvmStatic
        private val TYPE_OBJECT_OBJECT = 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PRIMITIVE_PRIMITIVE -> PyMapTextTextViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_primitive_primitive, parent, false)
            )
            TYPE_PRIMITIVE_OBJECT -> PyMapTextImageViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_primitive_object, parent, false)
            )
            TYPE_OBJECT_OBJECT -> PyMapImageImageViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_object_object, parent, false)
            )
            else -> PyMapImageTextViewHolder(
                activity.layoutInflater.inflate(R.layout.key_value_item_object_primitive, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (getItemViewType(position)) {
            TYPE_PRIMITIVE_PRIMITIVE -> {
                if (holder is PyMapTextTextViewHolder) {
                    holder.text1.text = item.first.toString()
                    holder.text2.text = item.second.toString()
                }
            }
            TYPE_PRIMITIVE_OBJECT -> {
                if (holder is PyMapTextImageViewHolder) {
                    holder.text.text = item.first.toString()
                }
            }
            TYPE_OBJECT_PRIMITIVE -> {
                if (holder is PyMapImageTextViewHolder) {
                    holder.text.text = item.second.toString()
                }
            }
        }
    }

    override fun getItemViewType(position: Int) : Int {
        val item = getItem(position)
        return if (item.first is EncodedObject && item.second is EncodedObject) {
            TYPE_OBJECT_OBJECT
        } else if (item.first is EncodedObject && item.second !is EncodedObject) {
            TYPE_OBJECT_PRIMITIVE
        } else if (item.first !is EncodedObject && item.second is EncodedObject) {
            TYPE_PRIMITIVE_OBJECT
        } else {
            TYPE_PRIMITIVE_PRIMITIVE
        }
    }

    class PyMapTextTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1: TextView = itemView.findViewById(R.id.text1)
        val text2: TextView = itemView.findViewById(R.id.text2)
    }

    class PyMapTextImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text1)
        val image : ImageButton = itemView.findViewById(R.id.zoom_in2)
    }

    class PyMapImageTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageButton = itemView.findViewById(R.id.zoom_in1)
        val text: TextView = itemView.findViewById(R.id.text2)
    }

    class PyMapImageImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image1 : ImageButton = itemView.findViewById(R.id.zoom_in1)
        val image2 : ImageButton = itemView.findViewById(R.id.zoom_in2)
    }
}
