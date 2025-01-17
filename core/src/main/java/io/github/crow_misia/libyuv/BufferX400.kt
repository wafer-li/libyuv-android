package io.github.crow_misia.libyuv

import kotlin.math.min

/**
 * YUV(4:0:0 8bpp) Buffer.
 */
interface BufferX400<BUFFER : BufferX400<BUFFER, BUFFERX420>, BUFFERX420 : BufferX420<BUFFERX420>> : BufferY<BUFFER> {
    fun convertTo(dst: BUFFERX420) {
        Yuv.convertI400Copy(
            srcY = planeY.buffer, srcStrideY = planeY.rowStride,
            dstY = dst.planeY.buffer, dstStrideY = dst.planeY.rowStride,
            width = min(width, dst.width), height = min(height, dst.height),
        )
    }

    fun mirrorTo(dst: BUFFER) {
        Yuv.planerI400Mirror(
            srcY = planeY.buffer, srcStrideY = planeY.rowStride,
            dstY = dst.planeY.buffer, dstStrideY = dst.planeY.rowStride,
            width = min(width, dst.width), height = min(height, dst.height),
        )
    }

    fun rotate(dst: BUFFER, rotateMode: RotateMode) {
        Yuv.rotateRotatePlane(
            src = planeY.buffer, srcStride = planeY.rowStride,
            dst = dst.planeY.buffer, dstStride = dst.planeY.rowStride,
            width = rotateMode.calculateWidth(this, dst),
            height = rotateMode.calculateHeight(this, dst),
            rotateMode = rotateMode.degrees,
        )
    }

    fun scale(dst: BUFFER, filterMode: FilterMode) {
        Yuv.scaleScalePlane(
            src = planeY.buffer, srcStride = planeY.rowStride,
            srcWidth = width, srcHeight = height,
            dst = dst.planeY.buffer, dstStride = dst.planeY.rowStride,
            dstWidth = dst.width, dstHeight = dst.height,
            filterMode = filterMode.mode,
        )
    }
}
