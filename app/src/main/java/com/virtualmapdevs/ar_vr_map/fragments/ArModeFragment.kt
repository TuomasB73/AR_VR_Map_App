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
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.viewmodels.ARItemViewModel
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class ArModeFragment : Fragment() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private var dashboards = mutableListOf<ViewRenderable>()
    private var arItemId: Int? = null
    private lateinit var arItemViewModel: ARItemViewModel
    private val viewModel: MainViewModel by viewModels()
    private val sharedPrefFile = "loginsharedpreference"

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
        //arItemViewModel = ViewModelProvider(this).get(ARItemViewModel::class.java)

        fetchARItemData()

        view.findViewById<Button>(R.id.showArSceneButton).setOnClickListener {
            add3dObject()
        }
    }

    // The AR item's details are fetched from the ViewModel and the 3D model and dashboards are loaded
    private fun fetchARItemData() {
        val sharedPreference =
            activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val QRid = sharedPreference?.getString("QRid", "")
        val loginId = sharedPreference?.getString("loginKey", "")

        Log.d("artest", "armodelF QR id: ${QRid.toString()}")

        if (loginId != null) {
            if (QRid != null) {
                viewModel.getArItemById(
                    "Bearer $loginId",
                    QRid
                )
            }
        }
        viewModel.ARItembyIdMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                Log.d("artest", "aritemMsg: ${response.body()}")
                Log.d("artest", "aritemMsg: ${response.code()}")

                val itemTitle = response.body()?.name
                Log.d("artest", itemTitle.toString())
                val itemDescription = response.body()?.description
                val itemModelUri = response.body()?.imageReference

                load3DModel(Uri.parse(itemModelUri))
                if (itemTitle != null) {
                    if (itemDescription != null) {
                        loadDashboards(itemTitle, itemDescription)
                    }
                }

            } else {
                Log.d("artest", "failed")
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
                itemModelUri
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
                layout = LayoutInflater.from(context).inflate(R.layout.ar_item_info_dashboard, null as ViewGroup?)
                layout.findViewById<TextView>(R.id.itemTitleTextView).text = itemTitle
                layout.findViewById<TextView>(R.id.itemDescriptionTextView).text = itemDescription
            } else {
                layout = LayoutInflater.from(context).inflate(R.layout.api_data_dashboard, null as ViewGroup?)
                // Data will be fetched from different APIs to the dashboards here
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
}