package com.virtualmapdevs.ar_vr_map.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.virtualmapdevs.ar_vr_map.MapModel
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.utils.Constants
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import java.net.URL
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONTokener


class ArModeFragment : Fragment() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private var dashboards = mutableListOf<ViewRenderable>()
    private val viewModel: MainViewModel by viewModels()
    private val sharedPrefFile = "loginsharedpreference"
    private val mapList = ArrayList<MapModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ar_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the AR item ID passed from the previous fragment here

        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fetchARItemData()

        view.findViewById<Button>(R.id.showArSceneButton).setOnClickListener {
            add3dObject()
        }

        view.findViewById<Button>(R.id.saveBtn).setOnClickListener {

            prepareData()

            val sharedPreference =
                activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            val qRid = sharedPreference?.getString("QRid", "")
            val loginId = sharedPreference?.getString("loginKey", "")

            viewModel.getArItemById(
                "Bearer $loginId",
                "$qRid"
            )
            viewModel.ARItembyIdMsg.observe(viewLifecycleOwner, { response ->
                if (response.isSuccessful) {
                    Log.d("artest", "aritemMsg: ${response.body()}")
                    Log.d("artest", "aritemMsg: ${response.code()}")

                    val itemTitle = response.body()?.name

                    val map = MapModel(qRid, itemTitle)
                    mapList.add(map)

                    val gson = Gson()
                    val json = gson.toJson(mapList)
                    val editor = sharedPreference?.edit()
                    editor?.putString("savedIds", json)
                    editor?.apply()

                } else {
                    Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // The AR item's details are fetched from the ViewModel and the 3D model and dashboards are loaded
    private fun fetchARItemData() {
        val sharedPreference = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val itemId = sharedPreference?.getString("QRid", "")
        val loginKey = sharedPreference?.getString("loginKey", "")
        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val qRid = sharedPreference?.getString("QRid", "")
        val loginId = sharedPreference?.getString("loginKey", "")

        Log.d("artest", "armodelF QR id: ${qRid.toString()}")
        Log.d("artest", "armodelF QR id: ${loginId.toString()}")

        viewModel.getArItemById(
            "Bearer $loginKey",
            "$itemId"
            "Bearer $loginId",
            "$qRid"
        )

        viewModel.ARItembyIdMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val itemTitle = response.body()?.name
                val itemDescription = response.body()?.description
                val itemModelUri = response.body()?.imageReference

                val fullItemModelUrl = Uri.parse(Constants.ITEM_MODEL_BASE_URL + itemModelUri.toString())
                load3DModel(fullItemModelUrl)

                if (itemTitle != null && itemDescription != null) {
                    loadDashboards(itemTitle, itemDescription)
                }
            } else {
                Log.d("ARItemFetch", "failed")
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })


/*        if (arItemId != null) {
            arItemViewModel.getARItem(arItemId!!)
            arItemViewModel.arItem.observe(requireActivity(), {
                val itemTitle = it.itemTitle
                val itemDescription = it.itemDescription
                val itemModelUri = it.itemModelUri

                load3DModel(Uri.parse(itemModelUri))
                loadDashboards(itemTitle, itemDescription)
            })
        }*/
    }

    private fun load3DModel(itemModelUri: Uri) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        ModelRenderable.builder()
            .setSource(
                context,
                Uri.parse("https://users.metropolia.fi/~tuomasbb/mobile_project/test_3d_model/terrain_example.gltf")
            )
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .build()
            .thenAccept { modelRenderable = it }
            .exceptionally {
                Log.e(ContentValues.TAG, "something went wrong ${it.localizedMessage}")
                null
            }
    }

    private fun loadDashboards(itemTitle: String, itemDescription: String) {
        for (i in 0..2) {
            var layout: View

            if (i == 0) {
                layout = LayoutInflater.from(context)
                    .inflate(R.layout.ar_item_info_dashboard, null as ViewGroup?)
                layout.findViewById<TextView>(R.id.itemTitleTextView).text = itemTitle
                layout.findViewById<TextView>(R.id.itemDescriptionTextView).text = itemDescription
            } else {
                layout = LayoutInflater.from(context)
                    .inflate(R.layout.api_data_dashboard, null as ViewGroup?)
                // Data will be fetched from different APIs to the dashboards here
                layout.findViewById<ImageView>(R.id.apiDataImageView)
                    .setImageBitmap(loadImage(itemModelUrl))
            }

            ViewRenderable.builder()
                .setView(context, layout)
                .build()
                .thenAccept { dashboards.add(it) }
        }
    }

    private fun add3dObject() {
        val frame = arFragment.arSceneView.arFrame

        if (frame != null && modelRenderable != null) {
            val screenCenter = getScreenCenter()
            val hits = frame.hitTest(screenCenter.x.toFloat(), screenCenter.y.toFloat())

            for (hit in hits) {
                val trackable = hit.trackable

                if (trackable is Plane) {
                    val anchor = hit!!.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)
                    val modelNode = TransformableNode(arFragment.transformationSystem)
                    modelNode.renderable = modelRenderable
                    modelNode.scaleController.minScale = 0.05f
                    modelNode.scaleController.maxScale = 0.15f
                    modelNode.localScale = Vector3(0.1f, 0.1f, 0.1f)
                    modelNode.setParent(anchorNode)
                    modelNode.select()

                    addDashboards(anchorNode)

                    break
                }
            }
        }
    }

    private fun addDashboards(anchorNode: AnchorNode) {
        var xAxisPosition = 0.6f

        for (dashboard in dashboards) {
            val dashboardNode = TransformableNode(arFragment.transformationSystem)
            dashboardNode.renderable = dashboard
            dashboardNode.scaleController.minScale = 0.3f
            dashboardNode.scaleController.maxScale = 0.5f
            dashboardNode.localScale = Vector3(0.4f, 0.4f, 0.4f)
            dashboardNode.localPosition = Vector3(xAxisPosition, 0.2f, -0.1f)
            dashboardNode.setParent(anchorNode)

            xAxisPosition -= 0.6f
        }
    }

    private fun getScreenCenter(): Point {
        val vw = requireActivity().findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    fun loadImage(url: String): Bitmap? {
        val url = URL(url)
        return try {
            val connection = url.openConnection()
            val istream = connection.getInputStream()
            BitmapFactory.decodeStream(istream)
        } catch (e: Exception) {
            Log.d("Error", "Icon download error")
            null
        }
    }

    // If user want to save map, this will create list of all previously saved maps so
    // new one is added to that.
    private fun prepareData() {

        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val json: String? = sharedPreference?.getString("savedIds", "")

        if (json != "") {
            val jsonArray = JSONTokener(json).nextValue() as JSONArray
            for (i in 0 until jsonArray.length()) {
                // mapName
                val mapName = jsonArray.getJSONObject(i).getString("mapName")
                // mapId
                val mapId = jsonArray.getJSONObject(i).getString("mapId")
                if (mapName != "still empty") {
                    val map = MapModel(mapId, mapName)
                    mapList.add(map)
                }
            }
        } else {
            val map = MapModel("mapId", "still empty")
            mapList.add(map)
        }

    }
}