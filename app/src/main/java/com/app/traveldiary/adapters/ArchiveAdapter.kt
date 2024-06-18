package com.app.traveldiary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.traveldiary.R
import com.app.traveldiary.databinding.MainItemBinding
import com.app.traveldiary.models.ArchiveModel
import com.app.traveldiary.interfaces.OnReadyClickListener
import com.bumptech.glide.Glide

class ArchiveAdapter(val onItemClickListener: OnReadyClickListener): RecyclerView.Adapter<ArchiveAdapter.ArchiveHolder>() {

    var archiveList = mutableListOf<ArchiveModel>()

    class ArchiveHolder(item : View, val onItemClickListener: OnReadyClickListener): RecyclerView.ViewHolder(item) {
        val binding = MainItemBinding.bind(item)
        fun bind(archive : ArchiveModel){
            binding.title.text = archive.title
            binding.desc.text = archive.desc
            binding.detail.text = archive.detailDest
            Glide.with(itemView)
                .load(archive.image)
                .into(binding.image)

            var isOpen = false
            binding.upDown.setOnClickListener {
                if (!isOpen){
                    binding.detail.visibility = View.VISIBLE
                    binding.upDown.setImageResource(R.drawable.up)
                    binding.noteLineat.visibility = View.VISIBLE
                    binding.ready.visibility = View.VISIBLE
                    isOpen = true
                }else{
                    binding.detail.visibility = View.GONE
                    binding.upDown.setImageResource(R.drawable.down)
                    binding.noteLineat.visibility = View.GONE
                    binding.ready.visibility = View.GONE
                    isOpen = false
                }
            }

            binding.like.visibility = View.INVISIBLE

            binding.ready.setOnClickListener {
                onItemClickListener.ItemClickListener(archive)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item, parent, false)
        return ArchiveHolder(view, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return archiveList.size
    }

    override fun onBindViewHolder(holder: ArchiveHolder, position: Int) {
        val archive = archiveList[position]
        holder.bind(archive)
    }
}