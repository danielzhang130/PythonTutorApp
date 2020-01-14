package daniel.pythontutor.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import daniel.pythontutor.R
import daniel.pythontutor.model.OrderedMap
import daniel.pythontutor.model.Utils
import java.util.*

class FrameAdapter(private val mContext: Context) :
    RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {

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
                )
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
            }
            is PrimitiveViewHolder -> {
                holder.value.text = item?.let { Utils.toString(it) } ?: "None"
            }
        }
    }

    override fun getItemCount() = mFrame.size

    override fun getItemViewType(position: Int): Int {
        val item = mFrame.get(position)
        return if (Utils.isPrimitive(item))
            TYPE_PRIMITIVE else TYPE_OBJECT
    }

    fun setFrame(frame: OrderedMap<String, Any>) {
        val diffResult = DiffUtil.calculateDiff(FrameDiffCallback(mFrame, frame))
        mFrame = frame
        diffResult.dispatchUpdatesTo(this)
    }

    abstract class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)!!
    }

    class ObjectViewHolder(itemView: View) : FrameViewHolder(itemView) {
        val link = itemView.findViewById<ImageView>(R.id.link)!!
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
