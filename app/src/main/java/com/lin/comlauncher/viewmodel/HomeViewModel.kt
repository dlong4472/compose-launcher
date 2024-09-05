package com.lin.comlauncher.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lin.comlauncher.R
import com.lin.comlauncher.entity.AppInfoBaseBean
import com.lin.comlauncher.entity.AppOrignBean
import com.lin.comlauncher.entity.ApplicationInfo
import com.lin.comlauncher.util.DisplayUtils
import com.lin.comlauncher.util.LauncherConfig
import com.lin.comlauncher.util.LauncherUtils
import com.lin.comlauncher.view.GridItemData
import com.lin.comlauncher.view.LogDebug
import com.lin.comlauncher.view.initItems
import com.lin.comlauncher.view.reSortItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    var infoBaseBean = AppInfoBaseBean()
    var carList = mutableListOf<MutableList<GridItemData>>()

    private var currentVersion = 0

    private val loadInfoLiveData = MutableLiveData(currentVersion)

    val appVersionLiveData: LiveData<Int> = loadInfoLiveData


    @SuppressLint("UseCompatLoadingForDrawables")
    fun loadApp(pm: PackageManager, width: Int, height: Int, resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val dpWidth = DisplayUtils.pxToDp(width)
            val dpHeight = DisplayUtils.pxToDp(height)
            LauncherConfig.APP_INFO_DRAG_DIS = DisplayUtils.dpToPx(10)
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val appInfoBaseBean = AppInfoBaseBean()

            val list = ArrayList<ArrayList<ApplicationInfo>>()
            var cacheList = ArrayList<ApplicationInfo>()
            val mToolBarList = ArrayList<ApplicationInfo>()

            val findSet = HashSet<String>()
            var index = 0
            val cellWidth = (dpWidth - LauncherConfig.HOME_DEFAULT_PADDING_LEFT * 2) / 4
            val cellMax = LauncherConfig.HOME_PAGE_CELL_NUM
            val originList = mutableListOf<AppOrignBean>()
            pm.queryIntentActivities(intent, 0).forEach {
                originList.add(
                    AppOrignBean(
                        name = it.loadLabel(pm).toString(),
                        activityName = it.activityInfo.name,
                        packageName = it.activityInfo.packageName,
                        drawable = it.activityInfo.loadIcon(pm),
                        appType = LauncherConfig.CELL_TYPE_APP
                    )
                )
            }
            //add fold
            originList.add(
                originList.size / 2, AppOrignBean(
                    name = "文件夹",
                    packageName = "app1",
                    appType = LauncherConfig.CELL_TYPE_FOLD,
                    drawable = null,
                    activityName = ""
                )
            )

            //add setting
            originList.add(
                originList.size / 2 + 1, AppOrignBean(
                    name = "SetLauncher",
                    packageName = LauncherConfig.APP_TYPE_FUNCTION,
                    appType = LauncherConfig.CELL_TYPE_APP,
                    drawable = resources.getDrawable(R.drawable.app_setting, null),
                    activityName = LauncherConfig.APP_TYPE_SETTING
                )
            )


            originList.forEach continuing@{ resolveInfo ->
                if (findSet.contains(resolveInfo.packageName))
                    return@continuing
                if (index == 10)
                    findSet.add(resolveInfo.packageName ?: "")
                index %= cellMax
                val ai = ApplicationInfo(
                    name = resolveInfo.name,
                    resolveInfo.packageName
                )
                ai.appType = resolveInfo.appType
                if (resolveInfo.appType == LauncherConfig.CELL_TYPE_APP) {
                    ai.icon = getBitmapFromDrawable(resolveInfo.drawable!!)
                } else if (resolveInfo.appType == LauncherConfig.CELL_TYPE_FOLD) {
                    //add test fold app
                    for (i in 0 until 7) {
                        val child = ApplicationInfo().apply {
                            val rInfo = originList[i]
                            name = rInfo.name
                            pageName = rInfo.packageName
                            activityName = rInfo.activityName
                            icon = rInfo.drawable?.let { getBitmapFromDrawable(it) }
                            this.width = cellWidth
                            this.height = LauncherConfig.HOME_CELL_HEIGHT
                            iconWidth = LauncherConfig.CELL_ICON_WIDTH
                            iconHeight = LauncherConfig.CELL_ICON_WIDTH
                            position = LauncherConfig.POSITION_FOLD
                        }
                        ai.childs.add(child)
                    }
                    LauncherUtils.changeFoldPosition(ai.childs)
                    LauncherUtils.createFoldIcon(ai)

                }

                ai.activityName = resolveInfo.activityName
                ai.pageName = resolveInfo.packageName

                LauncherConfig.HOME_TOOLBAR_START = dpHeight - dpWidth / 4
                ai.iconWidth = LauncherConfig.CELL_ICON_WIDTH
                ai.iconHeight = LauncherConfig.CELL_ICON_WIDTH
                if (LauncherUtils.isToolBarApplication(ai.pageName) && mToolBarList.size < 4) {
                    ai.width = cellWidth
                    ai.height = cellWidth
                    ai.posY = dpHeight - cellWidth
                    ai.posX =
                        LauncherConfig.HOME_DEFAULT_PADDING_LEFT + mToolBarList.size % 4 * cellWidth
                    ai.position = LauncherConfig.POSITION_TOOLBAR
                    ai.showText = false
                    ai.cellPos = mToolBarList.size
                    mToolBarList.add(ai)
                } else {
                    ai.width = cellWidth
                    ai.height = LauncherConfig.HOME_CELL_HEIGHT
                    ai.posX = LauncherConfig.HOME_DEFAULT_PADDING_LEFT + (index % 4) * cellWidth
                    ai.posY =
                        index / 4 * LauncherConfig.HOME_CELL_HEIGHT + LauncherConfig.DEFAULT_TOP_PADDING
                    ai.position = LauncherConfig.POSITION_HOME
                    ai.updateSize()
                    cacheList.add(ai)
                    if (index == cellMax - 1) {
                        cacheList = ArrayList()
                    }
                    if (index == 0) {
                        list.add(cacheList)
                    }
                    ai.cellPos = index
                    ai.pagePos = list.size - 1
                    index++
                }
                ai.orignX = ai.posX
                ai.orignY = ai.posY
                LauncherConfig.HOME_CELL_WIDTH = ai.width
            }

            appInfoBaseBean.homeList.clear()
            appInfoBaseBean.homeList.addAll(list)
            appInfoBaseBean.toobarList = mToolBarList
            val userTime = System.currentTimeMillis() - startTime
            Log.e("linlog", "loadA==${list.size} toolbar=${mToolBarList.size} time=$userTime")
            infoBaseBean = appInfoBaseBean
            loadInfoLiveData.postValue(++currentVersion)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable)
            return drawable.bitmap
        else {
            var iWidth = drawable.intrinsicWidth
            val iHeight = drawable.intrinsicHeight
            if (iWidth < 0)
                iWidth = 1
            if (iHeight < 0)
                iHeight < 1
            val bmp = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bmp
        }
    }

    fun loadCardList(screenHeightDp: Int, items: MutableList<GridItemData>) {
        val betweenPadding = 10.dp.value.toInt()
        val topBottomPadding = 20.dp.value.toInt() * 2
        if(LogDebug)Log.d("loadCardList", "betweenPadding:$betweenPadding, pxToDp:${DisplayUtils.pxToDp(10)}")
        val cellSize = (screenHeightDp - topBottomPadding - betweenPadding * 3) / 4
        carList = initItems(
            screenHeightDp,
            betweenPadding,
            topBottomPadding,
            cellSize,
            topBottomPadding / 2,
            items
        )
        loadInfoLiveData.postValue(++currentVersion)
    }

}
