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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import daniel.pythontutor.R
import daniel.pythontutor.model.OrderedMap
import java.util.*

class StackAdapter(private val mContext: Context) :
    RecyclerView.Adapter<StackAdapter.StackAdapterViewHolder>() {

    private val mInflater = (mContext as FragmentActivity).layoutInflater
    private var mGlobal = OrderedMap<String, Any>(Collections.emptyMap(), Collections.emptyList())
    private var mStack = emptyList<Pair<String, OrderedMap<String, Any>>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StackAdapterViewHolder(mContext, mInflater.inflate(R.layout.stack_frame, parent, false))

    override fun onBindViewHolder(holder: StackAdapterViewHolder, position: Int) {
        if (position == mStack.size) {
            holder.title.text = mContext.getString(R.string.global_frame)
            (holder.recyclerView.adapter as FrameAdapter).setFrame(mGlobal)
        } else {
            holder.title.text = mStack[position].first
            (holder.recyclerView.adapter as FrameAdapter).setFrame(mStack[position].second)
        }
    }

    override fun getItemCount() = mStack.size + 1

    fun setGlobal(global: OrderedMap<String, Any>) {
        val diffResult = DiffUtil.calculateDiff(StackDiffCallback(mStack, mStack, mGlobal, global.reverse()))
        mGlobal = global.reverse()
        diffResult.dispatchUpdatesTo(this)
    }

    fun setStack(stack: List<Pair<String, OrderedMap<String, Any>>>) {
        val diffResult = DiffUtil.calculateDiff(StackDiffCallback(mStack, stack, mGlobal, mGlobal))
        mStack = stack
        diffResult.dispatchUpdatesTo(this)
    }

    class StackAdapterViewHolder(context: Context, itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)!!
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.frame)!!
        init {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = FrameAdapter(context)
        }
    }

    class StackDiffCallback(
        private val mOldStack: List<Pair<String, OrderedMap<String, Any>>>,
        private val mNewStack: List<Pair<String, OrderedMap<String, Any>>>,
        private val mOldGlobal: OrderedMap<String, Any>,
        private val mNewGlobal: OrderedMap<String, Any>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = mOldStack.size + 1

        override fun getNewListSize() = mNewStack.size + 1

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition == mOldStack.size) {
                return newItemPosition == mNewStack.size
            }
            if (newItemPosition == mNewStack.size) {
                return oldItemPosition == mOldStack.size
            }

            return false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition == mOldStack.size || newItemPosition == mNewStack.size) {
                return mOldGlobal.data == mNewGlobal.data
            }

            return mOldStack[oldItemPosition] == mNewStack[newItemPosition]
        }
    }
}