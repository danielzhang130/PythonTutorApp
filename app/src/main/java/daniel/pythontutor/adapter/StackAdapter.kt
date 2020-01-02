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
    private var mStack = OrderedMap<String, OrderedMap<String, Any>>(Collections.emptyMap(), Collections.emptyList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StackAdapterViewHolder(mContext, mInflater.inflate(R.layout.stack_frame, parent, false))

    override fun onBindViewHolder(holder: StackAdapterViewHolder, position: Int) {
        if (position == mStack.size) {
            holder.title.text = mContext.getString(R.string.global_frame)
            (holder.recyclerView.adapter as FrameAdapter).setFrame(mGlobal)
        } else {
            holder.title.text = mStack.orderedKeys[position]
            (holder.recyclerView.adapter as FrameAdapter).setFrame(
                mStack.get(position) ?: OrderedMap(Collections.emptyMap(), Collections.emptyList())
            )
        }
    }

    override fun getItemCount() = mStack.size + 1

    fun setGlobal(global: OrderedMap<String, Any>) {
        val diffResult = DiffUtil.calculateDiff(StackDiffCallback(mStack, mStack, mGlobal, global.reverse()))
        mGlobal = global.reverse()
        diffResult.dispatchUpdatesTo(this)
    }

    fun setStack(stack: OrderedMap<String, OrderedMap<String, Any>>) {
        val diffResult = DiffUtil.calculateDiff(StackDiffCallback(mStack, stack.reverse(), mGlobal, mGlobal))
        mStack = stack.reverse()
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
        private val mOldStack: OrderedMap<String, OrderedMap<String, Any>>,
        private val mNewStack: OrderedMap<String, OrderedMap<String, Any>>,
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
            return mOldStack.orderedKeys[oldItemPosition] == mNewStack.orderedKeys[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition == mOldStack.size || newItemPosition == mNewStack.size) {
                return mOldGlobal.data == mNewGlobal.data
            }

            val oldItem = mOldStack.get(oldItemPosition)
            val newItem = mNewStack.get(newItemPosition)
            if (oldItem == null || newItem == null) {
                return oldItem == newItem
            }

            return oldItem.data == newItem.data
        }
    }
}