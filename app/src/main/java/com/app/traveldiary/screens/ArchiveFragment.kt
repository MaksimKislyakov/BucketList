package com.app.traveldiary.screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.traveldiary.R
import com.app.traveldiary.adapters.ArchiveAdapter
import com.app.traveldiary.databinding.FragmentArchiveBinding
import com.app.traveldiary.models.ArchiveModel
import com.app.traveldiary.interfaces.OnReadyClickListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class ArchiveFragment : Fragment(), OnReadyClickListener {
    lateinit var binding : FragmentArchiveBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var adapter: ArchiveAdapter
    lateinit var recyclerView: RecyclerView

    private val loadedList = mutableListOf<ArchiveModel>()

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArchiveBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE)

        initial()
        getData()
        navigate()
        setupSearch()
    }

    private fun navigate() {
        binding.notPopular.setOnClickListener {
            findNavController().navigate(R.id.notPopularFragment)
        }

        binding.home.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.popular.setOnClickListener {
            findNavController().navigate(R.id.popularFragment)
        }
    }

    private fun initial(){
        recyclerView = binding.rv
        adapter = ArchiveAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearch() {
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
        })
    }

    private fun filterList(query: String) {
        val filteredList = mutableListOf<ArchiveModel>()
        if (query.isEmpty()) {
            filteredList.addAll(loadedList)
        } else {
            for (item in loadedList) {
                if (item.title.contains(query, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.archiveList = filteredList
        adapter.notifyDataSetChanged()
    }

    private fun getData(){
        val userId = sharedPreferences.getString("id", "")

        db.collection("archive")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { result ->
                loadedList.clear()
                for (document in result) {
                    val title = document.get("title").toString()
                    val desc = document.get("desc").toString()
                    val detailDest = document.get("detailDesc").toString()
                    val image = document.get("image").toString()
                    val note = document.get("note").toString()

                    val newItem = ArchiveModel(title, desc, detailDest, note, image)
                    loadedList.add(newItem)

                    recyclerView?.adapter?.let { adapter ->
                        if (adapter is ArchiveAdapter) {
                            adapter.archiveList = loadedList
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                Log.d("SIZE", "Size : ${loadedList.size}")
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
            }
    }

    override fun ItemClickListener(item: ArchiveModel) {
        val userId = sharedPreferences.getString("id", "")

        val user = hashMapOf(
            "desc" to item.desc,
            "detailDesc" to item.detailDest,
            "id" to userId,
            "image" to item.image,
            "title" to item.title,
        )

        db.collection("home")
            .add(user)
            .addOnSuccessListener { documentReference ->
                db.collection("archive")
                    .whereEqualTo("title", item.title)
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    loadedList.remove(item)
                                    adapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("TAG", "Error deleting document with title ", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error getting documents with title ", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

}