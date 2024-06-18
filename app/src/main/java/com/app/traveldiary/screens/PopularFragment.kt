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
import com.app.traveldiary.adapters.PopularAdapter
import com.app.traveldiary.databinding.FragmentPopularBinding
import com.app.traveldiary.interfaces.OnPopularItemClickListener
import com.app.traveldiary.models.PopularModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class PopularFragment : Fragment(), OnPopularItemClickListener {
    lateinit var binding: FragmentPopularBinding
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: PopularAdapter
    lateinit var sharedPreferences: SharedPreferences

    private val loadedList = mutableListOf<PopularModel>()

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPopularBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE)
        navigate()
        initial()
        getData()
        setupSearch()
    }

    private fun initial() {
        recyclerView = binding.rv
        adapter = PopularAdapter(this, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun getData() {
        db.collection("popular")
            .get()
            .addOnSuccessListener { result ->
                val popularList = mutableListOf<PopularModel>()
                for (document in result) {
                    val title = document.get("title").toString()
                    val desc = document.get("desc").toString()
                    val detailDest = document.get("detailDesc").toString()
                    val image = document.get("image").toString()

                    val newItem = PopularModel(title, desc, detailDest, image)
                    popularList.add(newItem)
                }
                loadedList.clear()
                loadedList.addAll(popularList)
                adapter.updatePopularList(loadedList)
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
            }
    }

    private fun navigate() {
        binding.notPopular.setOnClickListener {
            findNavController().navigate(R.id.notPopularFragment)
        }

        binding.home.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun ItemClickListener(item: PopularModel) {
        val userId = sharedPreferences.getString("id", "")

        if (item.isLiked) {
            val user = hashMapOf(
                "id" to userId,
                "desc" to item.desc,
                "title" to item.title,
                "image" to item.image,
                "detailDesc" to item.detailDest
            )

            db.collection("home")
                .add(user)
                .addOnSuccessListener { documentReference ->
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        } else {
            db.collection("home")
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
    }

    override fun ZnakomoClickListener(item: PopularModel) {

        val user = hashMapOf(
            "desc" to item.desc,
            "detailDesc" to item.detailDest,
            "image" to item.image,
            "title" to item.title,
        )

        db.collection("not_popular")
            .add(user)
            .addOnSuccessListener { documentReference ->
                db.collection("popular")
                    .whereEqualTo("title", item.title)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    loadedList.remove(item)
                                    getData()
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
        val filteredList = if (query.isEmpty()) {
            loadedList
        } else {
            loadedList.filter { it.title.contains(query, ignoreCase = true) }
        }
        adapter.updatePopularList(filteredList)
    }
}