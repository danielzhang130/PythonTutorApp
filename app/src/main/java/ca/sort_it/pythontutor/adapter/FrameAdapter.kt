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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.lib.Utils
import ca.sort_it.pythontutor.model.OrderedMap
import ca.sort_it.pythontutor.ui.ActivityMain
import java.util.*

class FrameAdapter(private val mContext: Context) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>(),
    View.OnClickListener {

    companion object {
        private const val TYPE_OBJECT = 1
        private const val TYPE_PRIMITIVE = 2
    }

    private var mFrame: OrderedMap<String, Any> =
        OrderedMap(Collections.emptyMap(), Collections.emptyList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            TYPE_OBJECT ->
                ObjectViewHolder(
                    (mContext as FragmentActivity).layoutInflater.inflate(
                        R.layout.stack_object,
                        parent,
                        false
                    )
                ).apply {
                    link.setOnClickListener(this@FrameAdapter)
                }
            TYPE_PRIMITIVE ->
                PrimitiveViewHolder(
                    (mContext as FragmentActivity).layoutInflater.inflate(
                        R.layout.stack_primitive,
                        parent,
                        false
                    )
                )
            else -> throw IllegalArgumentException()
        }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val item = mFrame.get(position)
        holder.name.text = mFrame.orderedKeys[position]
        when (holder) {
            is ObjectViewHolder -> {
                holder.link.tag = mFrame.get(position)
            }
            is PrimitiveViewHolder -> {
                holder.value.text = item?.let { Utils.toString(it) } ?: "None"
            }
        }
    }

    override fun getItemCount() = mFrame.size

    override fun getItemViewType(position: Int): Int {
        return if (Utils.isPrimitive(mFrame.get(position)))
            TYPE_PRIMITIVE else TYPE_OBJECT
    }

    fun setFrame(frame: OrderedMap<String, Any>) {
        val diffResult = DiffUtil.calculateDiff(FrameDiffCallback(mFrame, frame))
        mFrame = frame
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onClick(v: View) {
        val tag = v.tag
        if (tag is List<*> && tag.size == 2 && tag[0] == "REF" && tag[1] is Number) {
            (mContext as ActivityMain).goToHeapAt((tag[1] as Number).toInt())
        }
    }

    abstract class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
    }

    class ObjectViewHolder(itemView: View) : FrameViewHolder(itemView) {
        val link = itemView.findViewById<ImageButton>(R.id.link)!!
    }

    class PrimitiveViewHolder(itemView: View) : FrameViewHolder(itemView) {
        val value = itemView.findViewById<TextView>(R.id.value)!!
    }

    class FrameDiffCallback(
        private val mOldFrame: OrderedMap<String, Any>,
        private val mNewFrame: OrderedMap<String, Any>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = mOldFrame.size

        override fun getNewListSize() = mNewFrame.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldFrame.orderedKeys[oldItemPosition] == mNewFrame.orderedKeys[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOldFrame.get(oldItemPosition) == mNewFrame.get(newItemPosition)
        }

    }
}
