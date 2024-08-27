package com.lin.comlauncher.entity

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImagePainter

class ApplicationInfo(
    var name: String? = null,
    var pageName: String? = null,
    var activityName: String? = null,
    var icon: Bitmap? = null,
    var posX: Int = 0,
    var posY: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var isDrag: Boolean = false,
    var orignX: Int = 0,
    var orignY: Int = 0,
    var needMoveX: Int = 0,
    var needMoveY: Int = 0,
    var posFx: Float = 0f,
    var posFy: Float = 0f,
    var cellPos: Int = 0,
    var isAnimi: Boolean = false,
    var position: Int = 0,
    var iconWidth: Int = 0,
    var iconHeight: Int = 0,
    var dragInfo: ApplicationInfo? = null,
    var showText: Boolean = true,
    var imageBitmap: AsyncImagePainter? = null,
    var pagePos: Int = 0,
    var appType: Int = 0,
    var childs: ArrayList<ApplicationInfo> = ArrayList(),
    var sizeType: String = "2*1",
) {
    override fun toString(): String {
        return "${name}: position=${position}"
    }

    fun updateSize() {
        val sizes = sizeType.split("*").map { it.toInt() }
        if (sizes.size == 2) {
            width *= sizes[0]
            height *= sizes[1]
            iconWidth *= sizes[0]
            iconHeight *= sizes[1]
        }
    }

}

class AppInfoBaseBean(
    var homeList: ArrayList<ArrayList<ApplicationInfo>> = ArrayList(),
    var toobarList: ArrayList<ApplicationInfo> = ArrayList(),
)

data class AppPos(
    var x: Int = 0,
    var y: Int = 0,
    var appName: String? = null
)

data class CellBean(
    var x: Int = 0,
    var y: Int = 0,
    var page: Int = 0
)

class AppOrignBean(
    var name: String? = null,
    var activityName: String?,
    var packageName: String? = "",
    var drawable: Drawable?,
    var appType: Int = 0,
)

class AppManagerBean(
    var startX: Int = 0,
    var startY: Int = 0,
    var applicationInfo: ApplicationInfo
)