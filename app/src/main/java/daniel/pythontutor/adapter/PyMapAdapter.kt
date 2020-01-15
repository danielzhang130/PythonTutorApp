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

package daniel.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject
import daniel.pythontutor.model.Utils
import daniel.pythontutor.ui.ActivityMain

class PyMapAdapter(private val fragment: Fragment) :
    ListAdapter<Pair<Any, Any>, RecyclerView.ViewHolder>(DIFF_CALLBACK), View.OnClickListener {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pair<Any, Any>>() {
            override fun areItemsTheSame(oldItem: Pair<Any, Any>, newItem: Pair<Any, Any>) = false

            override fun areContentsTheSame(oldItem: Pair<Any, Any>, newItem: Pair<Any, Any>): Boolean {
                return oldItem == newItem
            }
        }

        private const val TYPE_PRIMITIVE_PRIMITIVE = 0
        private const val TYPE_PRIMITIVE_OBJECT = 1
        private const val TYPE_OBJECT_PRIMITIVE = 2
        private const val TYPE_OBJECT_OBJECT = 4
    }

    private val activity = fragment.requireActivity()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PRIMITIVE_PRIMITIVE -> PyMapTextTextViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_primitive_primitive,
                    parent,
                    false
                )
            )
            TYPE_PRIMITIVE_OBJECT -> PyMapTextImageViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_primitive_object,
                    parent,
                    false
                )
            )
                .also {
                    it.image.setOnClickListener(this)
                }
            TYPE_OBJECT_OBJECT -> PyMapImageImageViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_object_object,
                    parent,
                    false
                )
            )
                .also {
                    it.image1.setOnClickListener(this)
                    it.image2.setOnClickListener(this)
                }
            else -> PyMapImageTextViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_object_primitive,
                    parent,
                    false
                )
            )
                .also {
                    it.image.setOnClickListener(this)
                }
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
                    holder.image.tag = item.second
                }
            }
            TYPE_OBJECT_PRIMITIVE -> {
                if (holder is PyMapImageTextViewHolder) {
                    holder.text.text = item.second.toString()
                    holder.image.tag = item.first
                }
            }
            TYPE_OBJECT_OBJECT -> {
                if (holder is PyMapImageImageViewHolder) {
                    holder.image1.tag = item.first
                    holder.image2.tag = item.second
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (!Utils.isPrimitive(item.first) && !Utils.isPrimitive(item.second)) {
            TYPE_OBJECT_OBJECT
        } else if (!Utils.isPrimitive(item.first) && Utils.isPrimitive(item.second)) {
            TYPE_OBJECT_PRIMITIVE
        } else if (Utils.isPrimitive(item.first) && !Utils.isPrimitive(item.second)) {
            TYPE_PRIMITIVE_OBJECT
        } else {
            TYPE_PRIMITIVE_PRIMITIVE
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            val tag = it.tag
            if (activity is ActivityMain && tag is EncodedObject) {
                activity.fragmentZoomInTransition(fragment, tag, v)
            }
        }
    }

    class PyMapTextTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1: TextView = itemView.findViewById(R.id.text1)
        val text2: TextView = itemView.findViewById(R.id.text2)
    }

    class PyMapTextImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text1)
        val image: ImageButton = itemView.findViewById(R.id.zoom_in2)
    }

    class PyMapImageTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageButton = itemView.findViewById(R.id.zoom_in1)
        val text: TextView = itemView.findViewById(R.id.text2)
    }

    class PyMapImageImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image1: ImageButton = itemView.findViewById(R.id.zoom_in1)
        val image2: ImageButton = itemView.findViewById(R.id.zoom_in2)
    }
}
