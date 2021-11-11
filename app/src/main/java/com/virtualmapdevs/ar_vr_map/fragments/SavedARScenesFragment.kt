package com.virtualmapdevs.ar_vr_map.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.virtualmapdevs.ar_vr_map.MapAdapter
import com.virtualmapdevs.ar_vr_map.MapModel
import com.virtualmapdevs.ar_vr_map.R
import java.util.HashSet
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONTokener


class SavedARScenesFragment : Fragment() {

    private val mapList = ArrayList<MapModel>()
    private lateinit var mapAdapter: MapAdapter
    private val sharedPrefFile = "loginsharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_a_r_scenes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        mapAdapter = MapAdapter(mapList)
        val layoutManager = LinearLayoutManager(activity?.applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mapAdapter

        prepareData()

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<HomeFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }
    }

    private fun prepareData() {

        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val json: String? = sharedPreference?.getString("savedIds", "")

        if (json != ""){
            val jsonArray = JSONTokener(json).nextValue() as JSONArray
            for (i in 0 until jsonArray.length()) {
                // mapId
                val mapId = jsonArray.getJSONObject(i).getString("mapId")
                Log.i("ID: ", mapId)

                // mapName
                val mapName = jsonArray.getJSONObject(i).getString("mapName")
                Log.i("mapName: ", mapName)

                val map = MapModel(mapId, mapName)
                mapList.add(map)
            }
            Log.d("artest", "SavedARScenesFragment: jsonarray test3: $jsonArray.")
        }else{
            val map = MapModel("mapId", "still empty")
            mapList.add(map)
        }

    }
}