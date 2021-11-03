package com.virtualmapdevs.ar_vr_map

import android.content.ContentValues
import android.graphics.Point
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARModeActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_armode)

        findViewById<Button>(R.id.addModelButton).setOnClickListener {
            add3dObject()
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        ModelRenderable.builder()
            .setSource(
                this,
                Uri.parse("file:///android_asset/storage_box.gltf")
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

    private fun add3dObject() {
        val frame = arFragment.arSceneView.arFrame
        if (frame != null && modelRenderable != null) {
            val pt = getScreenCenter()
            val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane) {
                    val anchor = hit!!.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)
                    val mNode = TransformableNode(arFragment.transformationSystem)
                    mNode.renderable = modelRenderable
                    mNode.scaleController.minScale = 0.05f
                    mNode.scaleController.maxScale = 0.15f
                    mNode.localScale = Vector3(0.1f, 0.1f, 0.1f)
                    mNode.setParent(anchorNode)
                    mNode.select()
                    break
                }
            }
        }
    }

    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }
}