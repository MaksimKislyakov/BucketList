package com.app.traveldiary.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.traveldiary.R
import com.app.traveldiary.databinding.AnotherItemBinding
import com.app.traveldiary.interfaces.OnPopularItemClickListener
import com.app.traveldiary.models.PopularModel
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class PopularAdapter(val onItemClickListener: OnPopularItemClickListener, val context: Context) : RecyclerView.Adapter<PopularAdapter.PopularHolder>() {

    var popularList = mutableListOf<PopularModel>()
    private val sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    private val db = Firebase.firestore

    init {
        loadLikeStates()
    }

    class PopularHolder(item: View, val onItemClickListener: OnPopularItemClickListener, val sharedPreferences: SharedPreferences) : RecyclerView.ViewHolder(item) {
        val binding = AnotherItemBinding.bind(item)

        fun bind(item: PopularModel) {
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

            binding.neZnakom.setOnClickListener {
                onItemClickListener.ZnakomoClickListener(item)
            }
        }

        private fun saveLikeState(item: PopularModel) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(item.title, item.isLiked)
            editor.apply()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.another_item, parent, false)
        return PopularHolder(view, onItemClickListener, sharedPreferences)
    }

    override fun getItemCount(): Int {
        return popularList.size
    }

    override fun onBindViewHolder(holder: PopularHolder, position: Int) {
        val item = popularList[position]
        holder.bind(item)
    }

    private fun loadLikeStates() {
        for (item in popularList) {
            item.isLiked = sharedPreferences.getBoolean(item.title, false)
        }
    }

    fun updatePopularList(newList: List<PopularModel>) {
        popularList.clear()
        popularList.addAll(newList)
        loadLikeStates() // Загрузка состояний после обновления списка
        notifyDataSetChanged()
    }
}