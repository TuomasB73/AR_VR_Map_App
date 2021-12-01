package com.virtualmapdevs.ar_vr_map.fragments

import android.Manifest
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
import com.virtualmapdevs.ar_vr_map.model.ReducedPoi
import com.virtualmapdevs.ar_vr_map.utils.Constants.Companion.PERMISSIONS_REQUEST_LOCATION
import com.virtualmapdevs.ar_vr_map.utils.LocationManager
import com.virtualmapdevs.ar_vr_map.utils.NetworkVariables
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
    private lateinit var showArSceneButton: Button
    private lateinit var saveItemButton: Button
    private lateinit var loadingModelTextView: TextView
    private lateinit var motionGesturesInstructionsCardView: CardView
    private lateinit var sensorManager: SensorManager
    private var sensorLinearAcceleration: Sensor? = null
    private var lastXAxisAccelerationValue = 0.0f
    private var lastYAxisAccelerationValue = 0.0f

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

        arItemId = requireArguments().getString("arItemId")
        userToken = SharedPreferencesFunctions.getUserToken(requireActivity())

        showArSceneButton = view.findViewById(R.id.showArSceneButton)
        saveItemButton = view.findViewById(R.id.saveBtn)
        loadingModelTextView = view.findViewById(R.id.loadingModelTextView)
        motionGesturesInstructionsCardView =
            view.findViewById(R.id.motionGesturesInstructionsCardView)
        arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        navView = view.findViewById(R.id.nav_view)

        /*if (NetworkVariables.isNetworkConnected) {
            checkIfItemIsAlreadySaved()
            fetchARItemData()
        } else {
            Toast.makeText(activity, getString(R.string.network_error_text), Toast.LENGTH_LONG).show()
        }*/

        checkIfItemIsAlreadySaved()
        fetchARItemData()

        createCube()

        createSphere()

        setUpSensor()

        showArSceneButton.setOnClickListener {
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
            findApproximateUserLocation()
        }

        view.findViewById<ImageView>(R.id.navDrawerIndicatorImageView).setOnClickListener {
            view.findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
        }
    }

    private fun findApproximateUserLocation() {
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
            distancesList.add(ReducedPoi(poi.name, distance, poi.x, poi.y, poi.z))
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
                val itemCategory = response.body()?.category
                val itemDescription = getString(
                    R.string.item_info_dashboard_category_text,
                    response.body()?.description
                )
                val itemModelUri = response.body()?.objectReference
                val logoReference = response.body()?.logoImageReference
                val pois = response.body()?.pois
                poiList = response.body()?.pois!!

                if (itemModelUri != null) {
                    val fullItemModelUri =
                        Uri.parse("${Constants.AR_ITEM_MODEL_BASE_URL}$itemModelUri")
                    load3DModel(fullItemModelUri)
                } else {
                    Log.d("ARItemFetch", "Item model Uri not found")
                    Toast.makeText(activity, "Item model Uri not found", Toast.LENGTH_SHORT).show()
                }

                if (itemTitle != null && itemCategory != null && itemDescription != null) {
                    //loadInfoDashboard(itemTitle, itemCategory, itemDescription)
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
                    subMenu.add(0, 0, 0, poi.name).setOnMenuItemClickListener {
                        setSubMenuItemClickListener(poi)
                        false
                    }.also {
                        setSubMenuIcon(it, poi.poiImage)
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

    private fun setSubMenuItemClickListener(poi: Poi) {
        val cubeNode = Node()
        cubeNode.renderable = cubeRenderable
        // TODO: Add a property in ARItem response which indicates the size of the item? (x, y, z)
        //cubeNode.localScale = Vector3(25f, 25f, 25f)
        cubeNode.localPosition = Vector3(poi.x, poi.y, poi.z)

        cubeNode.setOnTapListener { _, _ ->
            setNodeRemovalAlertBuilder(poi, cubeNode)
        }

        cubeNode.parent = modelNode
    }

    private fun setNodeRemovalAlertBuilder(poi: Poi, cubeNode: Node) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(poi.name)
        builder.setMessage(poi.description)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Remove from map") { _, _ ->
            arFragment.arSceneView.scene.removeChild(cubeNode)
            cubeNode.parent = null
            cubeNode.renderable = null
        }
        builder.setNeutralButton("Cancel") { _, _ ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setRemoveAllPoisAlertBuilder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.remove_all_pois_dialog_title_text))
        builder.setMessage(getString(R.string.remove_all_pois_dialog_message_text))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { _, _ ->
            removePointsOfInterest()
            sensorLinearAcceleration?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        builder.setNeutralButton("Cancel") { _, _ ->
            sensorLinearAcceleration?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
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

    private fun loadInfoDashboard(
        itemTitle: String,
        itemCategory: String,
        itemDescription: String
    ) {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.ar_item_info_dashboard, null as ViewGroup?)
        layout.findViewById<TextView>(R.id.itemTitleTextView).text = itemTitle
        layout.findViewById<TextView>(R.id.itemCategoryTextView).text = itemCategory
        layout.findViewById<TextView>(R.id.itemDescriptionTextView).text = itemDescription

        ViewRenderable.builder()
            .setView(context, layout)
            .build()
            .thenAccept { infoDashboard = it }
    }

    private fun add3dObject() {
        val frame = arFragment.arSceneView.arFrame

        if (frame != null && modelRenderable != null) {
            val screenCenter = getScreenCenter()
            val hits = frame.hitTest(screenCenter.x.toFloat(), screenCenter.y.toFloat())

            if (hits.isEmpty()) {
                Toast.makeText(
                    activity, getString(R.string.find_plane_toast_text), Toast.LENGTH_LONG
                ).show()
            } else {
                for (hit in hits) {
                    val trackable = hit.trackable

                    if (trackable is Plane) {
                        GlobalScope.launch(Dispatchers.Main) {
                            loadingModelTextView.visibility = View.VISIBLE
                            delay(1)

                            val anchor = hit!!.createAnchor()
                            anchorNode = AnchorNode(anchor)
                            anchorNode?.parent = arFragment.arSceneView.scene
                            modelNode = TransformableNode(arFragment.transformationSystem)
                            modelNode?.renderable = modelRenderable
                            modelNode?.scaleController?.minScale = 0.5f
                            modelNode?.scaleController?.maxScale = 2.5f
                            anchorNode?.localScale = Vector3(0.01f, 0.01f, 0.01f)
                            modelNode?.parent = anchorNode
                            modelNode?.select()

                            addInfoDashboard(anchorNode!!)
                            showArSceneButton.visibility = View.GONE
                            loadingModelTextView.visibility = View.GONE
                            arFragment.arSceneView.planeRenderer.isVisible = false
                        }
                        break
                    } else {
                        Toast.makeText(
                            activity, getString(R.string.find_plane_toast_text), Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun addInfoDashboard(anchorNode: AnchorNode) {
        val dashboardNode = TransformableNode(arFragment.transformationSystem)
        dashboardNode.renderable = infoDashboard
        dashboardNode.scaleController.minScale = 10.0f
        dashboardNode.scaleController.maxScale = 30.0f
        dashboardNode.localScale = Vector3(20.0f, 20.0f, 20.0f)
        dashboardNode.localPosition = Vector3(0.0f, 10.0f, -10.0f)
        dashboardNode.parent = anchorNode
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

    private fun removePointsOfInterest() {
        val allChildNodes = modelNode?.children?.toList()

        if (allChildNodes != null) {
            for (i in allChildNodes.indices) {
                // The first child node is the info dashboard, which won't be removed.
                if (i != 0) {
                    modelNode?.removeChild(allChildNodes[i])
                }
            }
        }
    }

    private fun zoomMapModel(shrink: Boolean) {
        val currentScale = anchorNode?.localScale?.x

        if (currentScale != null) {
            val zoomLevel = 0.003f

            if (shrink) {
                if (currentScale > 0.004) {
                    val newScale = currentScale - zoomLevel
                    anchorNode?.localScale = Vector3(newScale, newScale, newScale)
                } else {
                    Toast.makeText(
                        activity, getString(R.string.zoom_gesture_min_size_text), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
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

    private fun getScreenCenter(): Point {
        val vw = requireActivity().findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

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
        if (event.sensor == sensorLinearAcceleration) {
            Log.d("sensor", event.values[0].toString())
            determineVerticalMovement(event)
            determineHorizontalMovement(event)
        }
    }

    private fun determineVerticalMovement(event: SensorEvent?) {
        if (event != null) {
            val yAxisAccelerationValue = event.values[1]
            val childNodeCount = modelNode?.children?.toList()?.size

            if ((yAxisAccelerationValue >= 4.0 && lastYAxisAccelerationValue < 4.0) ||
                (yAxisAccelerationValue <= -4.0 && lastYAxisAccelerationValue > -4.0)
            ) {
                if (childNodeCount != null) {
                    if (childNodeCount > 1) {
                        setRemoveAllPoisAlertBuilder()
                        sensorManager.unregisterListener(this)
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.no_pois_added_text),
                            Toast.LENGTH_SHORT
                        ).show()
                        reRegisterSensorListenerAfterDelay()
                    }
                }
            }

            lastYAxisAccelerationValue = yAxisAccelerationValue
        }
    }

    private fun determineHorizontalMovement(event: SensorEvent?) {
        if (event != null) {
            val xAxisAccelerationValue = event.values[0]

            if (xAxisAccelerationValue >= 4.0 && lastXAxisAccelerationValue < 4.0) {
                zoomMapModel(true)
                reRegisterSensorListenerAfterDelay()
            } else if (xAxisAccelerationValue <= -4.0 && lastXAxisAccelerationValue > -4.0) {
                zoomMapModel(false)
                reRegisterSensorListenerAfterDelay()
            }

            lastXAxisAccelerationValue = xAxisAccelerationValue
        }
    }

    private fun reRegisterSensorListenerAfterDelay() {
        GlobalScope.launch(Dispatchers.Main) {
            sensorManager.unregisterListener(this@ArModeFragment)
            delay(400)
            sensorLinearAcceleration?.also {
                sensorManager.registerListener(
                    this@ArModeFragment,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("SENSOR", "Sensor accuracy changed.")
    }

    override fun onResume() {
        super.onResume()
        sensorLinearAcceleration?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
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
}