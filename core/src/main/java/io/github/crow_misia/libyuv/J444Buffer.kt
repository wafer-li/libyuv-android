package io.github.crow_misia.libyuv

import java.nio.ByteBuffer
import kotlin.math.min

/**
 * J444 (jpeg) YUV Format. 4:4:4 24bpp
 */
class J444Buffer private constructor(
    buffer: ByteBuffer?,
    override val planeY: Plane,
    override val planeU: Plane,
    override val planeV: Plane,
    override val width: Int,
    override val height: Int,
    releaseCallback: Runnable?,
) : AbstractBuffer(buffer, arrayOf(planeY, planeU, planeV), releaseCallback), BufferX444<J444Buffer>, BufferY<J400Buffer> {
    fun convertTo(dst: ArgbBuffer) {
        Yuv.convertJ444ToARGB(
            srcY = planeY.buffer, srcStrideY = planeY.rowStride,
            srcU = planeU.buffer, srcStrideU = planeU.rowStride,
            srcV = planeV.buffer, srcStrideV = planeV.rowStride,
            dstARGB = dst.plane.buffer, dstStrideARGB = dst.plane.rowStride,
            width = min(width, dst.width), height = min(height, dst.height),
        )
    }

    fun convertTo(dst: AbgrBuffer) {
        Yuv.convertJ444ToABGR(
            srcY = planeY.buffer, srcStrideY = planeY.rowStride,
            srcU = planeU.buffer, srcStrideU = planeU.rowStride,
            srcV = planeV.buffer, srcStrideV = planeV.rowStride,
            dstABGR = dst.plane.buffer, dstStrideABGR = dst.plane.rowStride,
            width = min(width, dst.width), height = min(height, dst.height),
        )
    }

    companion object Factory : BufferFactory<J444Buffer> {
        private fun getStrideWithCapacity(width: Int, height: Int): IntArray {
            val capacity = width * height
            return intArrayOf(width, capacity, width, capacity, width, capacity)
        }

        override fun allocate(width: Int, height: Int): J444Buffer {
            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val buffer = createByteBuffer(capacityY + capacityU + capacityV)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return J444Buffer(
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

        override fun wrap(buffer: ByteBuffer, width: Int, height: Int): J444Buffer {
            check(buffer.isDirect) { "Unsupported non-direct ByteBuffer." }

            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return J444Buffer(
                buffer = buffer,
                planeY = PlanePrimitive(strideY, bufferY),
                planeU = PlanePrimitive(strideU, bufferU),
                planeV = PlanePrimitive(strideV, bufferV),
                width = width,
                height = height,
                releaseCallback = null,
            )
        }

        fun wrap(planeY: Plane, planeU: Plane, planeV: Plane, width: Int, height: Int): J444Buffer {
            return J444Buffer(
                buffer = null,
                planeY = planeY,
                planeU = planeU,
                planeV = planeV,
                width = width,
                height = height,
                releaseCallback = null,
            )
        }
    }
}
