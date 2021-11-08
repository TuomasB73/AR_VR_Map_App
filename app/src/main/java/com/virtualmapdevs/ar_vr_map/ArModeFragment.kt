package com.virtualmapdevs.ar_vr_map

import android.content.ContentValues
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
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ArModeFragment : Fragment() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private lateinit var modelUri: Uri
    private var dashboards = mutableListOf<ViewRenderable>()

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

        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        modelUri = Uri.parse("https://users.metropolia.fi/~tuomasbb/mobile_project/test_3d_model/terrain_example.gltf")

        load3DModel()
        loadDashboards()

        view.findViewById<Button>(R.id.show3dModelButton).setOnClickListener {
            add3dObject()
        }
    }

    private fun load3DModel() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        ModelRenderable.builder()
            .setSource(
                context,
                modelUri
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

    private fun loadDashboards() {
        // Data will be fetched from different APIs to the dashboards here
        val arItemDashboard = LayoutInflater.from(context).inflate(R.layout.item_info_dashboard, null as ViewGroup?)
        arItemDashboard.findViewById<TextView>(R.id.itemTitleTextView).text = "TESTING"

        ViewRenderable.builder()
            .setView(context, arItemDashboard)
            .build()
            .thenAccept { dashboards.add(it) }

        ViewRenderable.builder()
            .setView(context, R.layout.api_data_dashboard)
            .build()
            .thenAccept { dashboards.add(it) }
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
        var xAxisPosition = -0.3f

        for (dashboard in dashboards) {
            val dashboardNode = TransformableNode(arFragment.transformationSystem)
            dashboardNode.renderable = dashboard
            dashboardNode.scaleController.minScale = 0.3f
            dashboardNode.scaleController.maxScale = 0.5f
            dashboardNode.localScale = Vector3(0.4f, 0.4f, 0.4f)
            dashboardNode.localPosition = Vector3(xAxisPosition, 0.2f, -0.1f)
            dashboardNode.setParent(anchorNode)

            xAxisPosition += 0.6f
        }
    }

    private fun getScreenCenter(): Point {
        val vw = requireActivity().findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }
}