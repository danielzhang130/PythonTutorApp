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

package ca.sort_it.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.model.PythonVisualization.EncodedObject
import ca.sort_it.pythontutor.model.Utils
import ca.sort_it.pythontutor.ui.ActivityMain

class PyListAdapter(private val fragment: Fragment) :
    ListAdapter<Any, PyListAdapter.PyListViewHolder>(
        DIFF_CALLBACK
    ), View.OnClickListener {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem::class == newItem::class
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return false
            }
        }

        private const val TYPE_PRIMITIVE = 0
        private const val TYPE_OBJECT = 1
    }

    private val activity = fragment.requireActivity()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PyListViewHolder {
        return if (viewType == TYPE_PRIMITIVE) {
            PyListTextViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_primitive_primitive,
                    parent,
                    false
                )
            )
        } else {
            PyListImageViewHolder(
                activity.layoutInflater.inflate(
                    R.layout.key_value_item_primitive_object,
                    parent,
                    false
                )
            ).also {
                it.image.setOnClickListener(this)
            }
        }

    }

    override fun onBindViewHolder(holder: PyListViewHolder, position: Int) {
        holder.index.text = position.toString()
        if (getItemViewType(position) == TYPE_PRIMITIVE) {
            if (holder is PyListTextViewHolder) {
                holder.text.text = Utils.toString(getItem(position))
            }
        }
        if (getItemViewType(position) == TYPE_OBJECT) {
            if (holder is PyListImageViewHolder) {
                holder.image.tag = position
            }
        }
    }

    override fun getItemViewType(position: Int) = if (Utils.isPrimitive(getItem(position)))
        TYPE_PRIMITIVE else TYPE_OBJECT

    override fun onClick(v: View?) {
        v?.let {
            val tag = it.tag
            if (tag is Int) {
                if (activity is ActivityMain) {
                    activity.fragmentZoomInTransition(fragment, getItem(tag) as EncodedObject, v)
                }
            }
        }
    }

    abstract class PyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val index: TextView = itemView.findViewById(R.id.text1)
    }

    class PyListTextViewHolder(itemView: View) : PyListViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text2)
    }

    class PyListImageViewHolder(itemView: View) : PyListViewHolder(itemView) {
        val image: ImageButton = itemView.findViewById(R.id.zoom_in2)
    }
}
