package com.virtualmapdevs.ar_vr_map.fragments

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.virtualmapdevs.ar_vr_map.R
import com.virtualmapdevs.ar_vr_map.model.Poi
import com.virtualmapdevs.ar_vr_map.utils.Constants
import com.virtualmapdevs.ar_vr_map.utils.SharedPreferencesFunctions
import com.virtualmapdevs.ar_vr_map.viewmodels.MainViewModel
import android.graphics.drawable.Drawable
import androidx.annotation.Nullable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Looper
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.ar.core.HitResult
import com.google.ar.sceneform.Scene
import com.virtualmapdevs.ar_vr_map.model.AddedPointOfInterest
import com.virtualmapdevs.ar_vr_map.model.ReducedPoi
import com.virtualmapdevs.ar_vr_map.utils.*
import com.virtualmapdevs.ar_vr_map.utils.Constants.Companion.PERMISSIONS_REQUEST_LOCATION
import com.virtualmapdevs.ar_vr_map.utils.LocationManager
import kotlinx.coroutines.*

class ArModeFragment : Fragment(), SensorEventListener {
    private lateinit var arFragment: ArFragment
    private lateinit var navView: NavigationView
    private lateinit var poiList: List<Poi>
    private lateinit var locationManager: LocationManager
    private var anchorNode: AnchorNode? = null
    private var modelNode: TransformableNode? = null
    private var cubeRenderable: ModelRenderable? = null
    private var sphereRenderable: ModelRenderable? = null
    private var modelRenderable: ModelRenderable? = null
    private var infoDashboard: ViewRenderable? = null
    private val viewModel: MainViewModel by viewModels()
    private var arItemId: String? = null
    private var userToken: String? = null
    private var arItemSaved: Boolean? = null
    private lateinit var place3dMapButton: Button
    private lateinit var saveItemButton: Button
    private lateinit var loadingModelTextView: TextView
    private lateinit var motionGesturesInstructionsCardView: CardView
    private lateinit var sensorManager: SensorManager
    private var sensorLinearAcceleration: Sensor? = null
    private var lastXAxisAccelerationValue = 0.0f
    private var lastYAxisAccelerationValue = 0.0f
    private var addedPointOfInterestList: MutableList<AddedPointOfInterest> = mutableListOf()
    private var isLocationFound = false
    private lateinit var onUpdateListener: Scene.OnUpdateListener

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

        locationManager = LocationManager(requireContext(), this)
        locationManager.initLocationClientRequestAndCallback()
        locationManager.checkSelfPermissions()

        // The ID of the item is passed from the previous fragment
        arItemId = requireArguments().getString("arItemId")
        // The user token is retrieved from the SharedPreferences
        userToken = SharedPreferencesFunctions.getUserToken(requireActivity())

        place3dMapButton = view.findViewById(R.id.place3dMapButton)
        saveItemButton = view.findViewById(R.id.saveBtn)
        loadingModelTextView = view.findViewById(R.id.loadingModelTextView)
        motionGesturesInstructionsCardView =
            view.findViewById(R.id.motionGesturesInstructionsCardView)
        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        navView = view.findViewById(R.id.nav_view)

        // Network connection is tested
        lifecycleScope.launch {
            if (NetworkVariables.isNetworkConnected) {
                checkIfItemIsAlreadySaved()
                fetchARItemData()
            } else {
                showNoConnectionDialog()
            }
        }

        // If the tutorial video is not shown yet, it will be shown
        if (SharedPreferencesFunctions.isVideoShownCheck(requireActivity()) == "no") {
            showInstructionVideo()
        }

        createCube()
        createSphere()
        setUpSensor()

        place3dMapButton.setOnClickListener {
            add3dObject()
        }

        saveItemButton.setOnClickListener {
            saveOrDeleteUserScannedItem()
        }

        view.findViewById<Button>(R.id.arModeBackButton).setOnClickListener {
            requireActivity().onBackPressed()
        }

        view.findViewById<Button>(R.id.motionGesturesInstructionsButton).setOnClickListener {
            motionGesturesInstructionsCardView.visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.closeGestureInstructionsButton).setOnClickListener {
            motionGesturesInstructionsCardView.visibility = View.GONE
        }

        view.findViewById<Button>(R.id.check_location_btn).setOnClickListener {
            // If the user has already tapped the location button, the user location won't be set again
            if (!isLocationFound) {
                findApproximateUserLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_already_added_text),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        view.findViewById<ImageView>(R.id.navDrawerIndicatorImageView).setOnClickListener {
            view.findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
        }

        view.findViewById<SwitchMaterial>(R.id.motionGesturesSwitch)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    registerSensorListener()
                } else {
                    unRegisterSensorListener()
                }
            }

        /* To avoid the arFragment being null when adding the update listener, the listener is set
        in a coroutine with a small delay before that */
        lifecycleScope.launch {
            delay(1)
            onUpdateListener = Scene.OnUpdateListener {
                /* A hit test is done every frame to find out when a plane is found in AR and the
                button for placing the 3D map is made visible */
                if (getPlaneHitResult() != null) {
                    place3dMapButton.visibility = View.VISIBLE
                }
            }
            arFragment.arSceneView.scene.addOnUpdateListener(onUpdateListener)
        }
    }

    // Creates a connection error dialog
    private fun showNoConnectionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("No connection")
        builder.setMessage("Check your Internet connection and try again")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Test connection") { _, _ ->
            if (NetworkVariables.isNetworkConnected) {
                checkIfItemIsAlreadySaved()
                fetchARItemData()
            } else {
                showNoConnectionDialog()
            }
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun findApproximateUserLocation() {
        locationManager.userLocation?.latitude ?: return
        locationManager.userLocation?.longitude ?: return

        val currentLocation = Location("currentLocation")
        currentLocation.latitude = locationManager.userLocation?.latitude!!
        currentLocation.longitude = locationManager.userLocation?.longitude!!

        val distancesList = mutableListOf<ReducedPoi>()
        poiList.forEach { poi ->
            val destinationLocation = Location("destinationLocation").also {
                it.latitude = poi.latitude
                it.longitude = poi.longitude
            }
            val distance = currentLocation.distanceTo(destinationLocation).toInt()
            distancesList.add(
                ReducedPoi(
                    poi.name,
                    distance,
                    poi.mapCoordinates.x,
                    poi.mapCoordinates.y,
                    poi.mapCoordinates.z
                )
            )
        }
        val closestPointOfInterest =
            distancesList.sortedByDescending { it.distance }.reversed().first()
        val sphereNode = Node()
        sphereNode.renderable = sphereRenderable
        sphereNode.localPosition = Vector3(
            closestPointOfInterest.x,
            closestPointOfInterest.y,
            closestPointOfInterest.z
        )

        sphereNode.setOnTapListener { _, _ ->
            Toast.makeText(
                requireContext(),
                "You are around this area! ${closestPointOfInterest.name}",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        sphereNode.parent = modelNode
        isLocationFound = true
    }

    // Checks if the item is already saved by the user and sets the button state accordingly
    private fun checkIfItemIsAlreadySaved() {
        if (userToken != null) {
            viewModel.getUserScannedItems(userToken!!)
        }

        viewModel.getUserScannedItemsMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val savedArItems = response.body()

                arItemSaved = false

                // All the user's saved items are fetched and checked if the current item is included
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

    // Sets the correct icon for the button based on if the item is already saved
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

    // Saves or deletes the item
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

    // The AR item's details are fetched from the ViewModel and the 3D model and dashboard are loaded
    private fun fetchARItemData() {
        if (userToken != null && arItemId != null) {
            viewModel.getArItemById(userToken!!, arItemId!!)
        }

        viewModel.arItembyIdMsg.observe(viewLifecycleOwner, { response ->
            if (response.isSuccessful) {
                val itemTitle = response.body()?.name
                val itemCategory = getString(
                    R.string.item_info_dashboard_category_text,
                    response.body()?.category
                )
                val itemDescription = response.body()?.description
                val itemModelUri = response.body()?.objectReference
                val logoReference = response.body()?.logoImageReference
                val pois = response.body()?.pois
                poiList = response.body()?.pois!!

                // The 3D model is loaded from the URL
                if (itemModelUri != null) {
                    val fullItemModelUri =
                        Uri.parse("${Constants.AR_ITEM_MODEL_BASE_URL}$itemModelUri")
                    load3DModel(fullItemModelUri)
                } else {
                    Log.d("ARItemFetch", "Item model Uri not found")
                    Toast.makeText(activity, "Item model Uri not found", Toast.LENGTH_SHORT).show()
                }

                // The info dashboard is loaded with the data of the item
                if (itemTitle != null && itemDescription != null && logoReference != null) {
                    loadInfoDashboard(itemTitle, itemCategory, itemDescription, logoReference)
                } else {
                    Log.d("ARItemFetch", "Item title and/or description not found")
                    Toast.makeText(
                        activity,
                        "Item title and/or description not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                initDrawerHeader(logoReference)

                if (pois?.size!! > 0) {
                    initDrawerItems(pois as MutableList<Poi>)
                } else {
                    val mMenu = navView.menu
                    val menuSize = mMenu.size()
                    mMenu.add(1, menuSize, menuSize, "No points of interest")
                }

            } else {
                Log.d("ARItemFetch", "Item fetch failed")
                Toast.makeText(activity, response.code(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initDrawerHeader(logoReference: String?) {
        logoReference ?: return
        val headerView = navView.getHeaderView(0)
        val drawerImage = headerView.findViewById<ImageView>(R.id.navDrawerImageView)

        Glide.with(requireContext()).load("${Constants.AR_ITEM_MODEL_BASE_URL}$logoReference")
            .error(R.drawable.testlogo2)
            .into(drawerImage)
    }

    private fun initDrawerItems(pois: MutableList<Poi>) {
        navView.itemIconTintList = null
        val mMenu = navView.menu
        val categories = pois.distinctBy { it.category }.map { it.category }.sortedBy { it }
        val poisSortedAlphabetically = pois.sortedBy { it.name }

        categories.forEach { category ->
            val subMenu: SubMenu = mMenu.addSubMenu(0, 0, 0, category)
            poisSortedAlphabetically.forEach { poi ->
                if (category == poi.category) {
                    subMenu.add(0, 0, 0, poi.name).let { menuItem ->
                        menuItem.setOnMenuItemClickListener {
                            setSubMenuItemClickListener(poi, menuItem)
                            false
                        }
                        setSubMenuIcon(menuItem, poi.poiImage)
                    }
                }
            }
        }
    }

    private fun setSubMenuIcon(menuItem: MenuItem, poiImage: String) {
        Glide.with(requireContext())
            .asBitmap()
            .load("${Constants.AR_ITEM_MODEL_BASE_URL}${poiImage}")
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    @Nullable transition: Transition<in Bitmap?>?
                ) {
                    val loadedIcon: Drawable = BitmapDrawable(resources, resource)
                    menuItem.icon = loadedIcon
                }

                override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                    menuItem.setIcon(R.drawable.testlogo2)
                }
            })
    }

    private fun setSubMenuItemClickListener(poi: Poi, menuItem: MenuItem) {
        Toast.makeText(requireContext(), "Added item ${poi.name} to map!", Toast.LENGTH_SHORT)
            .show()

        var pointOfInterestRenderable: ViewRenderable?

        ViewRenderable.builder()
            .setView(requireContext(), R.layout.point_of_interest_layout)
            .build()
            .thenAccept { renderable ->
                pointOfInterestRenderable = renderable
                RotatingNode(arFragment.transformationSystem).let { node ->
                    node.renderable = pointOfInterestRenderable
                    node.localPosition = Vector3(poi.mapCoordinates.x, 3f, poi.mapCoordinates.z)
                    node.scaleController.minScale = 4f
                    node.scaleController.maxScale = 15f
                    node.localScale = Vector3(8f, 8f, 8f)
                    addedPointOfInterestList.add(AddedPointOfInterest(poi, menuItem, node))
                    pointOfInterestRenderable!!.isShadowCaster = false
                    pointOfInterestRenderable!!.isShadowReceiver = false
                    node.setOnTapListener { _, _ ->
                        setNodeRemovalAlertBuilder(poi, node, menuItem)
                    }
                    node.parent = modelNode

                    val poiImageView =
                        pointOfInterestRenderable!!.view.findViewById<ImageView>(R.id.poi_iv)
                    if (poi.poiImage != "poiimages/poidefault.jpg") {
                        Glide.with(requireContext())
                            .load("${Constants.AR_ITEM_MODEL_BASE_URL}${poi.poiImage}")
                            .error(R.drawable.arrow_down)
                            .into(poiImageView)
                    } else {
                        poiImageView.setImageResource(R.drawable.arrow_down)
                    }
                    pointOfInterestRenderable!!.view.findViewById<TextView>(R.id.poi_tv).text =
                        poi.name
                    menuItem.setOnMenuItemClickListener(null)
                }
            }

    }

    private fun setNodeRemovalAlertBuilder(
        poi: Poi,
        pointOfInterestNode: TransformableNode,
        menuItem: MenuItem
    ) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(poi.name)
        builder.setMessage(poi.description)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Remove from map") { _, _ ->
            arFragment.arSceneView.scene.removeChild(pointOfInterestNode)
            pointOfInterestNode.parent = null
            pointOfInterestNode.renderable = null
            Toast.makeText(requireContext(), "Removed item from map", Toast.LENGTH_SHORT).show()
            menuItem.setOnMenuItemClickListener {
                setSubMenuItemClickListener(poi, menuItem)
                false
            }
        }
        builder.setNeutralButton("Cancel") { _, _ ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // Creates a dialog for removing all the points of interest
    private fun setRemoveAllPoisAlertBuilder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.remove_all_pois_dialog_title_text))
        builder.setMessage(getString(R.string.remove_all_pois_dialog_message_text))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { _, _ ->
            removePointsOfInterest()
            /* Registers the sensor listener again after being unregistered for avoiding multiple
            instances of the dialog */
            registerSensorListener()
        }
        builder.setNeutralButton("Cancel") { _, _ ->
            /* Registers the sensor listener again after being unregistered for avoiding multiple
            instances of the dialog */
            registerSensorListener()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // Loads the 3D model from the provided URL
    private fun load3DModel(itemModelUri: Uri) {
        // The loading needs to be done on main thread so the thread policy is modified
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

    // The info dashboard is created and set up
    private fun loadInfoDashboard(
        itemTitle: String,
        itemCategory: String,
        itemDescription: String,
        itemLogoReference: String
    ) {
        // The layout is inflated and the texts are set
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.ar_item_info_dashboard, null as ViewGroup?)
        layout.findViewById<TextView>(R.id.itemTitleTextView).text = itemTitle
        layout.findViewById<TextView>(R.id.itemCategoryTextView).text = itemCategory
        layout.findViewById<TextView>(R.id.itemDescriptionTextView).text = itemDescription

        // The logo image is loaded
        Glide.with(requireContext()).load("${Constants.AR_ITEM_MODEL_BASE_URL}$itemLogoReference")
            .error(R.drawable.testlogo2)
            .into(layout.findViewById(R.id.itemImageView))

        ViewRenderable.builder()
            .setView(context, layout)
            .build()
            .thenAccept { infoDashboard = it }
    }

    // Makes a hit test in AR space, tries to recognize a plane and returns the HitResult if one was found
    private fun getPlaneHitResult(): HitResult? {
        val frame = arFragment.arSceneView.arFrame

        if (frame != null) {
            val screenCenter = getScreenCenter()
            val hits = frame.hitTest(screenCenter.x.toFloat(), screenCenter.y.toFloat())

            if (hits.isNotEmpty()) {
                for (hit in hits) {
                    val trackable = hit.trackable

                    if (trackable is Plane) {
                        return hit
                    }
                }
            }
        }

        return null
    }

    // Adds the 3D model in the AR space and sets the scaling settings and UI changes
    private fun add3dObject() {
        val hit = getPlaneHitResult()

        if (hit != null && modelRenderable != null) {
            /* The set up of the 3D model is done in a coroutine with a small delay so that the loading
            text manages to be set visible before the UI freezes for a small time when the 3D model is
            actually loaded */
            lifecycleScope.launch {
                loadingModelTextView.visibility = View.VISIBLE
                delay(1)

                val anchor = hit.createAnchor()
                anchorNode = AnchorNode(anchor)
                anchorNode?.parent = arFragment.arSceneView.scene
                modelNode = TransformableNode(arFragment.transformationSystem)
                modelNode?.renderable = modelRenderable
                modelNode?.scaleController?.minScale = 0.5f
                modelNode?.scaleController?.maxScale = 2.5f
                anchorNode?.localScale = Vector3(0.01f, 0.01f, 0.01f)
                modelNode?.parent = anchorNode
                modelNode?.select()

                addInfoDashboard()
                /* The update listener is removed as the 3D model is placed and the place 3D model
                button is no longer needed */
                arFragment.arSceneView.scene.removeOnUpdateListener(onUpdateListener)
                place3dMapButton.visibility = View.GONE
                loadingModelTextView.visibility = View.GONE
                arFragment.arSceneView.planeRenderer.isVisible = false
            }
        } else {
            Toast.makeText(
                activity, getString(R.string.find_plane_toast_text), Toast.LENGTH_LONG
            ).show()
        }
    }

    // Adds the info dashboard to the AR space and sets the scaling
    private fun addInfoDashboard() {
        val dashboardNode = TransformableNode(arFragment.transformationSystem)
        dashboardNode.renderable = infoDashboard
        dashboardNode.scaleController.minScale = 20.0f
        dashboardNode.scaleController.maxScale = 60.0f
        dashboardNode.localScale = Vector3(40.0f, 40.0f, 40.0f)
        dashboardNode.localPosition = Vector3(0.0f, 10.0f, -30.0f)
        dashboardNode.parent = modelNode
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

    private fun createSphere() {
        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(0f, 255f, 0f))
            .thenAccept { material: Material? ->
                sphereRenderable =
                        //ShapeFactory.makeSphere(0.05f, Vector3(0.0f, 0.15f, 0.0f), material)
                    ShapeFactory.makeSphere(
                        2f, Vector3(0.0f, 1f, 0.0f), material
                    )
            }
    }

    // Removes all the added point of interest nodes from the map
    private fun removePointsOfInterest() {
        addedPointOfInterestList.forEach { addedPointOfInterest ->
            addedPointOfInterest.menuItem.setOnMenuItemClickListener {
                setSubMenuItemClickListener(addedPointOfInterest.poi, addedPointOfInterest.menuItem)
                false
            }
            modelNode?.removeChild(addedPointOfInterest.node)
        }
        addedPointOfInterestList.clear()
    }

    // Zooms the map model smaller or bigger
    private fun zoomMapModel(shrink: Boolean) {
        val currentScale = anchorNode?.localScale?.x

        if (currentScale != null) {
            val zoomLevel = 0.003f

            if (shrink) {
                // If the current scale is bigger than 0.004 the map will be shrinked by 0.003
                if (currentScale > 0.004) {
                    val newScale = currentScale - zoomLevel
                    anchorNode?.localScale = Vector3(newScale, newScale, newScale)
                } else {
                    Toast.makeText(
                        activity, getString(R.string.zoom_gesture_min_size_text), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // If the current scale is smaller than 0.02 the map will be enlarged by 0.003
                if (currentScale < 0.02) {
                    val newScale = currentScale + zoomLevel
                    anchorNode?.localScale = Vector3(newScale, newScale, newScale)
                } else {
                    Toast.makeText(
                        activity, getString(R.string.zoom_gesture_max_size_text), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Returns the center point of the screen
    private fun getScreenCenter(): Point {
        val vw = requireActivity().findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    // Sets up the linear acceleration sensor used for the motion gestures
    private fun setUpSensor() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorLinearAcceleration =
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        } else {
            Log.i("SENSOR", "Your device does not have an accelerometer sensor.")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        // The sensor values are determined if they count as gestures
        if (event.sensor == sensorLinearAcceleration) {
            determineVerticalMovement(event)
            determineHorizontalMovement(event)
        }
    }

    // Determines if a vertical movement counts as a gesture
    private fun determineVerticalMovement(event: SensorEvent?) {
        if (event != null) {
            val yAxisAccelerationValue = event.values[1]
            val childNodeCount = modelNode?.children?.toList()?.size

            /* If the sensor value is bigger or smaller than 4.0 or -4.0 and the last sensor value is
            outside that range (to avoid multiple consecutive sensor gesture triggers) the movement
             counts as a gesture */
            if ((yAxisAccelerationValue >= 4.0 && lastYAxisAccelerationValue < 4.0) ||
                (yAxisAccelerationValue <= -4.0 && lastYAxisAccelerationValue > -4.0)
            ) {
                if (childNodeCount != null) {
                    /* If the model node's child node count is bigger than 2 or 3 depending on if the
                    user location is placed, the dialog for removing all the points of interest is set */
                    if ((!isLocationFound && childNodeCount > 2) || (isLocationFound && childNodeCount > 3)) {
                        setRemoveAllPoisAlertBuilder()
                        /* Unregisters the sensor listener temporarily for avoiding multiple instances
                        of the dialog */
                        unRegisterSensorListener()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.no_pois_added_text),
                            Toast.LENGTH_SHORT
                        ).show()
                        /* Unregisters and registers the sensor listener again after a small delay for
                        avoiding multiple consecutive sensor gesture triggers */
                        reRegisterSensorListenerAfterDelay()
                    }
                }
            }

            lastYAxisAccelerationValue = yAxisAccelerationValue
        }
    }

    // Determines if a horizontal movement counts as a gesture
    private fun determineHorizontalMovement(event: SensorEvent?) {
        if (event != null) {
            val xAxisAccelerationValue = event.values[0]

            /* If the sensor value is bigger or smaller than 4.0 or -4.0 and the last sensor value is
            outside that range (to avoid multiple consecutive sensor gesture triggers) the movement
             counts as a gesture */
            if (xAxisAccelerationValue >= 4.0 && lastXAxisAccelerationValue < 4.0) {
                zoomMapModel(true)
                /* Unregisters and registers the sensor listener again after a small delay for
                avoiding multiple consecutive sensor gesture triggers */
                reRegisterSensorListenerAfterDelay()
            } else if (xAxisAccelerationValue <= -4.0 && lastXAxisAccelerationValue > -4.0) {
                zoomMapModel(false)
                /* Unregisters and registers the sensor listener again after a small delay for
                avoiding multiple consecutive sensor gesture triggers */
                reRegisterSensorListenerAfterDelay()
            }

            lastXAxisAccelerationValue = xAxisAccelerationValue
        }
    }

    private fun registerSensorListener() {
        sensorLinearAcceleration?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unRegisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    /* Unregisters and registers the sensor listener again after a small delay */
    private fun reRegisterSensorListenerAfterDelay() {
        lifecycleScope.launch {
            unRegisterSensorListener()
            delay(400)
            registerSensorListener()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("SENSOR", "Sensor accuracy changed.")
    }

    override fun onResume() {
        super.onResume()
        registerSensorListener()
    }

    override fun onPause() {
        super.onPause()
        unRegisterSensorListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.fusedLocationClient.removeLocationUpdates(locationManager.locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.fusedLocationClient.requestLocationUpdates(
                            locationManager.locationRequest!!,
                            locationManager.locationCallback, Looper.getMainLooper()
                        )

                    }
                } else {
                    TODO("Not yet implemented")
                }
            }
        }
    }

    private fun showInstructionVideo() {
        SharedPreferencesFunctions.saveVideoShown(requireActivity())
        val dialog = Dialog(this.requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.video_dialog)
        val skipBtn = dialog.findViewById(R.id.skipButton) as Button
        val video = dialog.findViewById(R.id.dialogVideoView) as VideoView

        val mediaController = MediaController(this.requireContext())
        mediaController.setAnchorView(video)

        val offlineUri: Uri =
            Uri.parse("android.resource://" + activity?.packageName + "/" + R.raw.testvideo)

        skipBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        video.setMediaController(mediaController)
        video.setVideoURI(offlineUri)
        video.requestFocus()
        video.start()

    }

    private fun resetShowInstructionVideo() {
        SharedPreferencesFunctions.resetVideoShown(requireActivity())
    }
}