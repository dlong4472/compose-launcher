package com.lin.comlauncher.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    var infoBaseBean = AppInfoBaseBean();

    val channel = Channel<Int>(UNLIMITED)

    var currentVersion = 0;

    var loadInfoLiveData = MutableLiveData<Int>(currentVersion)

    var appVersionLiveData: LiveData<Int> = loadInfoLiveData


    suspend fun sendData(value: Int) {
        channel.send(value)
    }

    var uiState by mutableStateOf<String>("111")
        private set

    fun loadApp(pm: PackageManager, width: Int, height: Int, resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            var startTime = System.currentTimeMillis();
            var dpWidth = DisplayUtils.pxToDp(width)
            var dpHeight = DisplayUtils.pxToDp(height)
            LauncherConfig.APP_INFO_DRAG_DIS = DisplayUtils.dpToPx(10)
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            var appInfoBaseBean = AppInfoBaseBean()

            var mlist = ArrayList<ArrayList<ApplicationInfo>>();
            var cacheList = ArrayList<ApplicationInfo>()
            var mToolBarList = ArrayList<ApplicationInfo>()

            var findSet = HashSet<String>()
            var index = 0
            var cellWidth = (dpWidth - LauncherConfig.HOME_DEFAULT_PADDING_LEFT * 2) / 4
            var cellMax = LauncherConfig.HOME_PAGE_CELL_NUM
            var orignList = mutableListOf<AppOrignBean>()
            pm.queryIntentActivities(intent, 0)?.forEach {
                orignList.add(
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
            orignList.add(
                orignList.size / 2, AppOrignBean(
                    name = "文件夹",
                    packageName = "app1",
                    appType = LauncherConfig.CELL_TYPE_FOLD,
                    drawable = null,
                    activityName = ""
                )
            )

            //add setting
            orignList.add(
                orignList.size / 2 + 1, AppOrignBean(
                    name = "SetLauncher",
                    packageName = LauncherConfig.APP_TYPE_FUNCTION,
                    appType = LauncherConfig.CELL_TYPE_APP,
                    drawable = resources.getDrawable(R.drawable.app_setting, null),
                    activityName = LauncherConfig.APP_TYPE_SETTING
                )
            )


            orignList.forEach continuing@{ resolveInfo ->
                if (findSet.contains(resolveInfo.packageName))
                    return@continuing
//                if(mlist.size>1){
//                    return@continuing
//                }
                if (index == 10)
                    findSet.add(resolveInfo.packageName ?: "")
                index %= cellMax;
                var ai = ApplicationInfo(
                    name = resolveInfo.name,
                    resolveInfo.packageName
                )
                ai.appType = resolveInfo.appType
                if (resolveInfo.appType == LauncherConfig.CELL_TYPE_APP) {
                    ai.icon = getBitmapFromDrawable(resolveInfo.drawable!!)
                } else if (resolveInfo.appType == LauncherConfig.CELL_TYPE_FOLD) {
                    //add test fold app
                    for (i in 0 until 7) {
                        var child = ApplicationInfo().apply {
                            var rInfo = orignList.get(i)
                            name = rInfo.name
                            pageName = rInfo.packageName;
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

                LauncherConfig.HOME_TOOLBAR_START = dpHeight - dpWidth / 4;
                ai.iconWidth = LauncherConfig.CELL_ICON_WIDTH;
                ai.iconHeight = LauncherConfig.CELL_ICON_WIDTH;
                if (LauncherUtils.isToolBarApplication(ai.pageName) && mToolBarList.size < 4) {
                    ai.width = cellWidth;
                    ai.height = cellWidth;
                    ai.posY = dpHeight - cellWidth
                    ai.posX =
                        LauncherConfig.HOME_DEFAULT_PADDING_LEFT + mToolBarList.size % 4 * cellWidth
                    ai.position = LauncherConfig.POSITION_TOOLBAR
                    ai.showText = false
                    ai.cellPos = mToolBarList.size;
                    mToolBarList.add(ai)
                } else {
                    ai.width = cellWidth;
                    ai.height = LauncherConfig.HOME_CELL_HEIGHT
                    ai.posX = LauncherConfig.HOME_DEFAULT_PADDING_LEFT + (index % 4) * cellWidth
                    ai.posY =
                        index / 4 * LauncherConfig.HOME_CELL_HEIGHT + LauncherConfig.DEFAULT_TOP_PADDING
                    ai.position = LauncherConfig.POSITION_HOME
                    cacheList.add(ai)
                    if (index == cellMax - 1) {
                        cacheList = ArrayList()
                    }
                    if (index == 0) {
                        mlist.add(cacheList)
                    }
                    ai.cellPos = index
                    ai.pagePos = mlist.size - 1
                    index++;
                }
                ai.orignX = ai.posX
                ai.orignY = ai.posY
                LauncherConfig.HOME_CELL_WIDTH = ai.width
            }

            appInfoBaseBean.homeList.clear()
            appInfoBaseBean.homeList.addAll(mlist)
            appInfoBaseBean.toobarList = mToolBarList
            var userTime = System.currentTimeMillis() - startTime;
            Log.e("linlog", "loadA==${mlist.size} toolbar=${mToolBarList.size} time=$userTime")
            infoBaseBean = appInfoBaseBean;
            loadInfoLiveData.postValue(++currentVersion)
        }
    }

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        var image = drawable!!
        if (image is BitmapDrawable)
            return image?.bitmap
        else {
            var iWidth = image.intrinsicWidth
            var iHeight = image.intrinsicHeight
            if (iWidth < 0)
                iWidth = 1
            if (iHeight < 0)
                iHeight < 1
            var bmp = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            image.setBounds(0, 0, canvas.width, canvas.height)
            image.draw(canvas)
            return bmp
        }
    }


}
