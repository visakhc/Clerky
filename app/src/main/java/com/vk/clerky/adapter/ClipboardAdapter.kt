package com.vk.clerky.adapter

import android.content.ClipData
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vk.clerky.databinding.ItemClipboardBinding

class ClipboardAdapter(private var listener: OnClickListener) :
    RecyclerView.Adapter<ClipboardAdapter.MyViewHolder>() {

    private var clipLocal = mutableListOf<String>()

    inner class MyViewHolder(val binding: ItemClipboardBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClipboardAdapter.MyViewHolder {
        return MyViewHolder(
            ItemClipboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ClipboardAdapter.MyViewHolder, position: Int) {
        with(clipLocal[position]) {
            holder.binding.tvClipText.text = this
            holder.itemView.setOnClickListener {
                listener.onItemClick(this.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return clipLocal.size
    }

    fun updateList(clip: String) {
        clipLocal.add(clip)
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun onItemClick(text: String)
    }
}
