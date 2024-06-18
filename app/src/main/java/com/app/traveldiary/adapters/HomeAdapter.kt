package com.app.traveldiary.adapters

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.traveldiary.R
import com.app.traveldiary.databinding.MainItemBinding
import com.app.traveldiary.models.HomeModel
import com.app.traveldiary.interfaces.OnHomeClickListener
import com.app.traveldiary.interfaces.OnHomeReadyClickListener
import com.bumptech.glide.Glide

class HomeAdapter(val onItemClickListener: OnHomeClickListener, val onReadyClickListener: OnHomeReadyClickListener, val context: Context): RecyclerView.Adapter<HomeAdapter.HomeHolder>() {

    var homeList = mutableListOf<HomeModel>()
    private val sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

    class HomeHolder(item: View, val onItemClickListener: OnHomeClickListener, val onReadyClickListener: OnHomeReadyClickListener, val sharedPreferences: SharedPreferences): RecyclerView.ViewHolder(item) {
        val binding = MainItemBinding.bind(item)

        fun bind(home : HomeModel){
            binding.title.text = home.title
            binding.desc.text = home.desc
            binding.detail.text = home.detailDest

            Glide.with(itemView)
                .load(home.image)
                .into(binding.image)

            val key = "note_${home.title}"

            val savedNote = sharedPreferences.getString(key, "")

            binding.note.setText(savedNote)

            binding.note.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    sharedPreferences.edit().putString(key, s.toString()).apply()
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

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

            binding.like.setOnClickListener {
                onItemClickListener.ItemClickListener(home)
            }

            binding.ready.setOnClickListener {
                onReadyClickListener.ItemReadyClickListener(home)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item, parent, false)
        return HomeHolder(view, onItemClickListener,onReadyClickListener, sharedPreferences)
    }

    override fun getItemCount(): Int {
        return homeList.size
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val home = homeList[position]
        holder.bind(home)
    }
}