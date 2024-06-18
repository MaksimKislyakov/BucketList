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
import com.app.traveldiary.adapters.HomeAdapter
import com.app.traveldiary.databinding.FragmentHomeBinding
import com.app.traveldiary.models.HomeModel
import com.app.traveldiary.interfaces.OnHomeClickListener
import com.app.traveldiary.interfaces.OnHomeReadyClickListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.UUID


class HomeFragment : Fragment(), OnHomeClickListener, OnHomeReadyClickListener {
    private lateinit var binding: FragmentHomeBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var adapter: HomeAdapter
    lateinit var recyclerView: RecyclerView

    private val loadedList = mutableListOf<HomeModel>()

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE)
        newUser()
        sharedPreferences.edit().putInt("TY", 9).apply()

        initial()
        navigate()
        getData()
        setupSearch()
    }

    private fun initial(){
        recyclerView = binding.rv
        adapter = HomeAdapter(this,this, requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getData(){
        val userId = sharedPreferences.getString("id", "")

        db.collection("home")
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

                    val newItem = HomeModel(title, desc, detailDest, note, image)
                    loadedList.add(newItem)

                    recyclerView?.adapter?.let { adapter ->
                        if (adapter is HomeAdapter) {
                            adapter.homeList = loadedList
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

    private fun newUser(){
        val TY = sharedPreferences.getInt("TY", -9)
        if (TY < 0){

            val userId = UUID.randomUUID().toString()

            val user = hashMapOf(
                "id" to userId
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    sharedPreferences.edit().putString("id", userId).apply()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }

        }
    }

    private fun navigate() {
        binding.notPopular.setOnClickListener {
            findNavController().navigate(R.id.notPopularFragment)
        }

        binding.popular.setOnClickListener {
            findNavController().navigate(R.id.popularFragment)
        }

        binding.archive.setOnClickListener {
            findNavController().navigate(R.id.archiveFragment)
        }
    }

    override fun ItemClickListener(item: HomeModel) {
        val userId = sharedPreferences.getString("id", "")

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
        val filteredList = mutableListOf<HomeModel>()
        if (query.isEmpty()) {
            filteredList.addAll(loadedList)
        } else {
            for (item in loadedList) {
                if (item.title.contains(query, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.homeList = filteredList
        adapter.notifyDataSetChanged()
    }

    override fun ItemReadyClickListener(item: HomeModel) {
        val userId = sharedPreferences.getString("id", "")

        val user = hashMapOf(
            "desc" to item.desc,
            "detailDesc" to item.detailDest,
            "id" to userId,
            "image" to item.image,
            "title" to item.title,
        )

        db.collection("archive")
            .add(user)
            .addOnSuccessListener { documentReference ->
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
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }


    }


}