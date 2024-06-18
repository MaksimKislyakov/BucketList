package com.app.traveldiary.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.traveldiary.R
import com.app.traveldiary.databinding.AnotherItemBinding
import com.app.traveldiary.models.NotPopularModel
import com.app.traveldiary.interfaces.OnNotPopularItemClickListener
import com.bumptech.glide.Glide

class NotPopularAdapter(val onItemClickListener: OnNotPopularItemClickListener, val context: Context) : RecyclerView.Adapter<NotPopularAdapter.NotPopularHolder>() {

    var notPopularList = mutableListOf<NotPopularModel>()
    private val sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

    init {
        loadLikeStates()
    }

    class NotPopularHolder(item: View, val onItemClickListener: OnNotPopularItemClickListener, val sharedPreferences: SharedPreferences) : RecyclerView.ViewHolder(item) {
        val binding = AnotherItemBinding.bind(item)

        fun bind(item: NotPopularModel) {
            binding.title.text = item.title
            binding.desc.text = item.desc
            binding.detail.text = item.detailDest

            Glide.with(itemView)
                .load(item.image)
                .into(binding.image)

            binding.like.setImageResource(if (item.isLiked) R.drawable.active_heart else R.drawable.pasive_heart)

            var isOpen = false
            binding.upDown.setOnClickListener {
                if (!isOpen){
                    binding.detail.visibility = View.VISIBLE
                    binding.upDown.setImageResource(R.drawable.up)
                    binding.linear.visibility = View.VISIBLE
                    isOpen = true
                }else{
                    binding.detail.visibility = View.GONE
                    binding.upDown.setImageResource(R.drawable.down)
                    binding.linear.visibility = View.GONE
                    isOpen = false
                }
            }

            binding.like.setOnClickListener {
                item.isLiked = !item.isLiked
                onItemClickListener.ItemClickListener(item)
                binding.like.setImageResource(if (item.isLiked) R.drawable.active_heart else R.drawable.pasive_heart)
                saveLikeState(item)
            }

            binding.znakom.setOnClickListener {
                onItemClickListener.NeZnakomo(item)
            }
        }

        private fun saveLikeState(item: NotPopularModel) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(item.title, item.isLiked)
            editor.apply()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotPopularHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.another_item, parent, false)
        return NotPopularHolder(view, onItemClickListener, sharedPreferences)
    }

    override fun getItemCount(): Int {
        return notPopularList.size
    }

    override fun onBindViewHolder(holder: NotPopularHolder, position: Int) {
        val item = notPopularList[position]
        holder.bind(item)
    }

    private fun loadLikeStates() {
        for (item in notPopularList) {
            item.isLiked = sharedPreferences.getBoolean(item.title, false)
        }
    }

    fun updateNotPopularList(newList: List<NotPopularModel>) {
        notPopularList.clear()
        notPopularList.addAll(newList)
        loadLikeStates()
        notifyDataSetChanged()
    }
}