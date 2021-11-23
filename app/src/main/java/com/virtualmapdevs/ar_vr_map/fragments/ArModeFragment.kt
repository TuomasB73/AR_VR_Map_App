package com.virtualmapdevs.ar_vr_map.fragments

import android.content.ContentValues
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.model.Poi
import com.virtualmapdevs.ar_vr_map.utils.Constants
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel

class ArModeFragment : Fragment() {
    private lateinit var pois: MutableList<Poi>
    private lateinit var arFragment: ArFragment
    lateinit var navView: NavigationView
    private var anchorNode: AnchorNode? = null
    private var modelNode: TransformableNode? = null
    private var cubeRenderable: ModelRenderable? = null
    private var modelRenderable: ModelRenderable? = null
    private var dashboards = mutableListOf<ViewRenderable>()
    private val viewModel: MainViewModel by viewModels()
    private var arItemId: String? = null
    private var userToken: String? = null
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

        userToken = SharedPreferencesFunctions.getUserToken(requireActivity())

        saveItemButton = view.findViewById(R.id.saveBtn)

        checkIfItemIsAlreadySaved()

        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        navView = view.findViewById(R.id.nav_view)

        fetchARItemData()

        createCube()

        view.findViewById<Button>(R.id.showArSceneButton).setOnClickListener {
            add3dObject()
        }

        saveItemButton.setOnClickListener {
            saveOrDeleteUserScannedItem()
        }

        view.findViewById<Button>(R.id.arModeBackButton).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun checkIfItemIsAlreadySaved() {
        if (userToken != null) {
            viewModel.getUserScannedItems(userToken!!)
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
        if (userToken != null && arItemId != null) {
            // If AR item is not saved, it will be saved
            if (arItemSaved == false) {
                viewModel.postUserScannedItem(userToken!!, arItemId!!)

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
                viewModel.deleteUserScannedItem(userToken!!, arItemId!!)

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
        if (userToken != null && arItemId != null) {
            viewModel.getArItemById(userToken!!, arItemId!!)
        }

        viewModel.arItembyIdMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val itemTitle = response.body()?.name
                val itemDescription = response.body()?.description
                val itemModelUri = response.body()?.imageReference
                pois = mutableListOf<Poi>(
                    Poi(
                        "1a5edf6d-fe59-4e98-ad77-928120dd9106",
                        "Room D590",
                        "Describe classroom D590 here...",
                        -29.2849f, 0f, -4.26449f
                    ),
                    Poi(
                        "1a5edf6d-fe59-4e98-ad77-928120dd9106",
                        "Room D558",
                        "Describe classroom D558 here...",
                        -20.5637f, 0f, -10.068f
                    ),
                    Poi(
                        "1a5edf6d-fe59-4e98-ad77-928120dd9106",
                        "Room D557",
                        "Describe classroom D557 here...",
                        -9.16784f, 0f, -19.2356f
                    ),
                    Poi(
                        "1a5edf6d-fe59-4e98-ad77-928120dd9106",
                        "Room D550",
                        "Describe classroom D550 here...",
                        -0.145803f, 0f, -10.6616f
                    ),
                    Poi(
                        "1a5edf6d-fe59-4e98-ad77-928120dd9106",
                        "Room D503.1",
                        "Describe classroom 503.1 here...",
                        -5.58866f, 0f, -6.05427f
                    ),
                )

                Log.d("pois", pois.toString())

                if (itemModelUri != null) {
                    val fullItemModelUri =
                        Uri.parse("${Constants.AR_ITEM_MODEL_BASE_URL}$itemModelUri")
                    load3DModel(fullItemModelUri)
                } else {
                    Log.d("ARItemFetch", "Item model Uri not found")
                    Toast.makeText(activity, "Item model Uri not found", Toast.LENGTH_SHORT).show()
                }

                if (itemTitle != null && itemDescription != null) {
                    //loadDashboards(itemTitle, itemDescription)
                } else {
                    Log.d("ARItemFetch", "Item title and/or description not found")
                    Toast.makeText(
                        activity,
                        "Item title and/or description not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (pois.size > 0) {
                    initDrawerItems(pois)
                }

            } else {
                Log.d("ARItemFetch", "Item fetch failed")
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initDrawerItems(pois: MutableList<Poi>) {
        Log.d("pois", "yes pois")

        val mMenu = navView.menu
        val menuSize = mMenu.size()

        pois.forEach { poi ->
            // groupId, itemId, order, title
            mMenu.add(1, menuSize, menuSize, poi.name).setOnMenuItemClickListener {
                val cubeNode = Node()
                cubeNode.renderable = cubeRenderable
                // TODO: Add a property in ARItem response which indicates the size of the item? (x, y, z)
                //cubeNode.localScale = Vector3(25f, 25f, 25f)
                cubeNode.localPosition = Vector3(poi.x, poi.y, poi.z)

                cubeNode.setOnTapListener { _, _ ->
                    setNodeRemovalAlertBuilder(poi, cubeNode)
                }

                cubeNode.setParent(modelNode)

                false
            }
        }
    }

    private fun setNodeRemovalAlertBuilder(poi: Poi, cubeNode: Node) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(poi.name)
        builder.setMessage(poi.description)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Remove from map") { _, _ ->
            arFragment.arSceneView.scene.removeChild(cubeNode)
            cubeNode.setParent(null)
            cubeNode.renderable = null
        }
        builder.setNeutralButton("Cancel") { _, _ ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
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
                    anchorNode = AnchorNode(anchor)
                    anchorNode?.setParent(arFragment.arSceneView.scene)
                    modelNode = TransformableNode(arFragment.transformationSystem)
                    modelNode?.renderable = modelRenderable
                    //modelNode?.scaleController?.minScale = 0.01f
                    //modelNode?.scaleController?.maxScale = 0.03f
                    anchorNode?.localScale = Vector3(0.01f, 0.01f, 0.01f)
                    modelNode?.setParent(anchorNode)
                    modelNode?.select()

                    //addDashboards(anchorNode!!)

                    break
                }
            }
        }
    }

    private fun addDashboards(anchorNode: AnchorNode) {
        var xAxisPosition = 0.8f

        for (dashboard in dashboards) {
            val dashboardNode = TransformableNode(arFragment.transformationSystem)
            dashboardNode.renderable = dashboard
            dashboardNode.scaleController.minScale = 0.2f
            dashboardNode.scaleController.maxScale = 0.8f
            dashboardNode.localScale = Vector3(0.4f, 0.4f, 0.4f)
            dashboardNode.localPosition = Vector3(xAxisPosition, 0.2f, -0.1f)
            dashboardNode.setParent(anchorNode)

            xAxisPosition -= 0.8f
        }
    }

    private fun createCube() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(255f, 0f, 0f))
            .thenAccept { material: Material? ->
                cubeRenderable =
                        //ShapeFactory.makeSphere(0.05f, Vector3(0.0f, 0.15f, 0.0f), material)
                    ShapeFactory.makeCube(
                        Vector3(2.5f, 2.5f, 2.5f),
                        Vector3(0.0f, 1f, 0.0f),
                        material
                    )
            }
    }

    private fun getScreenCenter(): Point {
        val vw = requireActivity().findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }
}