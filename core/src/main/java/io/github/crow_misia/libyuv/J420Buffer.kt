package io.github.crow_misia.libyuv

import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * J420 (jpeg) YUV Format. 4:2:0 12bpp
 */
class J420Buffer private constructor(
    buffer: ByteBuffer?,
    val planeY: Plane,
    val planeU: Plane,
    val planeV: Plane,
    override val width: Int,
    override val height: Int,
    releaseCallback: Runnable? = null,
) : AbstractBuffer(buffer, arrayOf(planeY, planeU, planeV), releaseCallback) {
    companion object {
        @JvmStatic
        fun getStrideWithCapacity(width: Int, height: Int): IntArray {
            val halfWidth = (width + 1).shr(1)
            val capacity = width * height
            val halfCapacity = (halfWidth + 1).shr(1) * height
            return intArrayOf(width, capacity, halfWidth, halfCapacity, halfWidth, halfCapacity)
        }

        @JvmStatic
        fun allocate(width: Int, height: Int): J420Buffer {
            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val buffer = createByteBuffer(capacityY + capacityU + capacityV)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return J420Buffer(
                buffer = buffer,
                planeY = PlanePrimitive(strideY, bufferY),
                planeU = PlanePrimitive(strideU, bufferU),
                planeV = PlanePrimitive(strideV, bufferV),
                width = width,
                height = height,
            ) {
                Yuv.freeNativeBuffer(buffer)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun wrap(buffer: ByteBuffer, width: Int, height: Int, releaseCallback: Runnable? = null): J420Buffer {
            check(buffer.isDirect) { "Unsupported non-direct ByteBuffer." }

            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return J420Buffer(
                buffer = buffer,
                planeY = PlanePrimitive(strideY, bufferY),
                planeU = PlanePrimitive(strideU, bufferU),
                planeV = PlanePrimitive(strideV, bufferV),
                width = width,
                height = height,
                releaseCallback = releaseCallback,
            )
        }

        @JvmStatic
        @JvmOverloads
        fun wrap(planeY: Plane, planeU: Plane, planeV: Plane, width: Int, height: Int, releaseCallback: Runnable? = null): J420Buffer {
            return J420Buffer(
                buffer = null,
                planeY = planeY,
                planeU = planeU,
                planeV = planeV,
                width = width,
                height = height,
                releaseCallback = releaseCallback,
            )
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @JvmStatic
        @JvmName("from")
        fun Image.toJ420Buffer(): J420Buffer {
            return J420Buffer(
                buffer = null,
                planeY = PlaneNative(planes[0]),
                planeU = PlaneNative(planes[1]),
                planeV = PlaneNative(planes[2]),
                width = width,
                height = height,
            )
        }

        @JvmStatic
        @JvmName("from")
        fun ImageProxy.toJ420Buffer(): J420Buffer {
            return J420Buffer(
                buffer = null,
                planeY = PlaneProxy(planes[0]),
                planeU = PlaneProxy(planes[1]),
                planeV = PlaneProxy(planes[2]),
                width = width,
                height = height,
            )
        }
    }
}
