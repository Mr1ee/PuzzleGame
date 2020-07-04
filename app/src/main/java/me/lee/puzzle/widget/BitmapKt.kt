package me.lee.puzzle.widget

import android.graphics.Bitmap

/**
 *
 * @description    BitmapKt
 * @author         lihuayong
 * @date           2020-02-05 13:51
 * @version        1.0
 */

fun scaleBitmap(src: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(src, width, height, false)
}

fun cropBitmap(src: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(src, x, y, width, height)
}