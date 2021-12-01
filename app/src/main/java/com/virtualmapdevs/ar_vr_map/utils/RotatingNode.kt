package com.virtualmapdevs.ar_vr_map.utils

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem


class RotatingNode(
    transformationSystem: TransformationSystem?
) : TransformableNode(transformationSystem) {
    @Nullable
    private var pointOfInterestImageAnimation: ObjectAnimator? = null
    private var speed = 90.0f
    private val animationDuration: Long = (1000 * 360 / speed).toLong()

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }


    private fun startAnimation() {
        if (pointOfInterestImageAnimation != null) {
            return
        }
        pointOfInterestImageAnimation = createAnimator()
        pointOfInterestImageAnimation!!.target = this
        pointOfInterestImageAnimation!!.duration = animationDuration
        pointOfInterestImageAnimation!!.start()
    }

    private fun stopAnimation() {
        pointOfInterestImageAnimation ?: return

        pointOfInterestImageAnimation!!.cancel()
        pointOfInterestImageAnimation = null
    }

    companion object {
        /** Returns an ObjectAnimator that makes this node rotate.  */
        private fun createAnimator(): ObjectAnimator {
            // Node's setLocalRotation method accepts Quaternions as parameters.
            // First, set up orientations that will animate a circle.
            val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
            val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
            val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
            val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
            val pointOfInterestImageAnimation = ObjectAnimator()
            pointOfInterestImageAnimation.setObjectValues(
                orientation1,
                orientation2,
                orientation3,
                orientation4
            )

            // Next, give it the localRotation property. Required
            pointOfInterestImageAnimation.setPropertyName("localRotation")

            // Use Sceneform's QuaternionEvaluator.
            pointOfInterestImageAnimation.setEvaluator(QuaternionEvaluator())

            //  Allow animation to repeat forever
            pointOfInterestImageAnimation.repeatCount = ObjectAnimator.INFINITE
            pointOfInterestImageAnimation.repeatMode = ObjectAnimator.RESTART
            pointOfInterestImageAnimation.interpolator = LinearInterpolator()
            pointOfInterestImageAnimation.setAutoCancel(true)
            return pointOfInterestImageAnimation
        }
    }

}
