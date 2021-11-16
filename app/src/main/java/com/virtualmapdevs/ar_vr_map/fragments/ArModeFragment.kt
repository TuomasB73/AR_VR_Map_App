package com.virtualmapdevs.ar_vr_map.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.utils.Constants
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class ArModeFragment : Fragment() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private var dashboards = mutableListOf<ViewRenderable>()
    private val viewModel: MainViewModel by viewModels()
    private var arItemId: String? = null
    private var loginToken: String? = null
    private val sharedPrefFile = "loginsharedpreference"
    private var arItemSaved: Boolean? = null
    private lateinit var saveItemButton: Button

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

        arItemId = requireArguments().getString("arItemId")

        val sharedPreference = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        loginToken = sharedPreference?.getString("loginKey", "")

        saveItemButton = view.findViewById(R.id.saveBtn)

        checkIfItemIsAlreadySaved()

        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fetchARItemData()

        view.findViewById<Button>(R.id.showArSceneButton).setOnClickListener {
            add3dObject()
        }

        saveItemButton.setOnClickListener {
            saveOrDeleteUserScannedItem()
        }
    }

    private fun checkIfItemIsAlreadySaved() {
        if (loginToken != null) {
            viewModel.getUserScannedItems("Bearer $loginToken")
        }

        viewModel.getUserScannedItemsMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val savedArItems = response.body()

                arItemSaved = false

                if (savedArItems != null) {
                    for (arItem in savedArItems) {
                        if (arItem._id == arItemId) {
                            arItemSaved = true
                            break
                        }
                    }
                }

                setSaveButtonAppearance()
            }
        })
    }

    private fun setSaveButtonAppearance() {
        saveItemButton.isEnabled = true

        if (arItemSaved == true) {
            saveItemButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_favorite_24,
                0,
                0,
                0
            )
        } else {
            saveItemButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_favorite_border_24,
                0,
                0,
                0
            )
        }
    }

    private fun saveOrDeleteUserScannedItem() {
        val sharedPreference = activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val loginKey = sharedPreference?.getString("loginKey", "")

        if (loginKey != null && arItemId != null) {
            // If AR item is not saved, it will be saved
            if (arItemSaved == false) {
                viewModel.postUserScannedItem("Bearer $loginKey", arItemId!!)

                viewModel.postUserScannedItemMsg.observe(viewLifecycleOwner, { response ->
                    if (response.isSuccessful) {
                        val message = response.body()?.message
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

                        arItemSaved = true
                        setSaveButtonAppearance()
                    } else {
                        Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
                    }
                })

                viewModel.postUserScannedItemMsgFail.observe(viewLifecycleOwner, {
                    Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                })
                // If AR item is already saved, it will be deleted
            } else {
                viewModel.deleteUserScannedItem("Bearer $loginKey", arItemId!!)

                viewModel.deleteUserScannedItemMsg.observe(viewLifecycleOwner, { response ->
                    if (response.isSuccessful) {
                        val message = response.body()?.message
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

                        arItemSaved = false
                        setSaveButtonAppearance()
                    } else {
                        Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
                    }
                })

                viewModel.deleteUserScannedItemMsgFail.observe(viewLifecycleOwner, {
                    Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    // The AR item's details are fetched from the ViewModel and the 3D model and dashboards are loaded
    private fun fetchARItemData() {
        if (arItemId != null) {
            viewModel.getArItemById(
                "Bearer $loginToken",
                arItemId!!
            )
        }

        viewModel.arItembyIdMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val itemTitle = response.body()?.name
                val itemDescription = response.body()?.description
                val itemModelUri = response.body()?.imageReference

                if (itemModelUri != null) {
                    val fullItemModelUri =
                        Uri.parse(Constants.AR_ITEM_MODEL_BASE_URL + itemModelUri)
                    load3DModel(fullItemModelUri)
                } else {
                    Log.d("ARItemFetch", "Item model Uri not found")
                    Toast.makeText(activity, "Item model Uri not found", Toast.LENGTH_SHORT).show()
                }

                if (itemTitle != null && itemDescription != null) {
                    loadDashboards(itemTitle, itemDescription)
                } else {
                    Log.d("ARItemFetch", "Item title and/or description not found")
                    Toast.makeText(
                        activity,
                        "Item title and/or description not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d("ARItemFetch", "Item fetch failed")
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })
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
                Log.e(ContentValues.TAG, "Something went wrong ${it.localizedMessage}")
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
            dashboardNode.scaleController.minScale = 0.2f
            dashboardNode.scaleController.maxScale = 0.6f
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