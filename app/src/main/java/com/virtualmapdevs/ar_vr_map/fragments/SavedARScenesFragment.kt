package com.virtualmapdevs.ar_vr_map.fragments

import android.os.Bundle
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

class SavedARScenesFragment : Fragment() {

    private val mapList = ArrayList<MapModel>()
    private lateinit var mapAdapter: MapAdapter

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
        prepareTestData()

        view.findViewById<Button>(R.id.backBtn).setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<HomeFragment>(R.id.fragmentContainer)
                addToBackStack(null)
            }
        }
    }

    // for testing only!!
    private fun prepareTestData() {
        var map = MapModel(45756, "Action & Adventure")
        mapList.add(map)
        map = MapModel(56376, "Animation, Kids & Family")
        mapList.add(map)
        map = MapModel(5736, "Action")
        mapList.add(map)
        map = MapModel(657356, "Animation")
        mapList.add(map)
        map = MapModel(567653, "Science Fiction & Fantasy")
        mapList.add(map)
        map = MapModel(57637, "Action")
        mapList.add(map)
        map = MapModel(6357653, "Animation")
        mapList.add(map)
        map = MapModel(763576, "Science Fiction")
        mapList.add(map)
        map = MapModel(53767, "Animation")
        mapList.add(map)
        map = MapModel(5687638, "Action & Adventure")
        mapList.add(map)
        map = MapModel(653876, "Science Fiction")
        mapList.add(map)
        map = MapModel(786748, "Animation")
        mapList.add(map)
        map = MapModel(6786479, "Science Fiction")
        mapList.add(map)
        map = MapModel(5876864, "Action & Adventure")
        mapList.add(map)
        map = MapModel(6796479, "Action & Adventure")
        mapList.add(map)
        map = MapModel(746984769, "Science Fiction & Fantasy")
        mapList.add(map)
        //mapsAdapter.notifyDataSetChanged()
    }
}