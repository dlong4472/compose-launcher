package com.lin.comlauncher

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.gyf.immersionbar.ImmersionBar
import com.lin.comlauncher.ui.theme.ComposeLauncherTheme
import com.lin.comlauncher.util.DisplayUtils
import com.lin.comlauncher.util.GridCardConfig
import com.lin.comlauncher.util.LauncherConfig
import com.lin.comlauncher.util.LauncherUtils
import com.lin.comlauncher.util.LogUtils
import com.lin.comlauncher.util.PermissionsUtil
import com.lin.comlauncher.view.GridCardListView
import com.lin.comlauncher.view.InitView
import com.lin.comlauncher.view.getItemData
import com.lin.comlauncher.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        ImmersionBar.with(this).transparentStatusBar().init()
        initView()
        setContent {
            ComposeLauncherTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val height =
                        DisplayUtils.getScreenHeightCanUse(this) + ImmersionBar.getStatusBarHeight(
                            this
                        )
                    LocalConfiguration.current.screenHeightDp = DisplayUtils.pxToDp(height)
                    LogUtils.e(
                        "height=${LocalConfiguration.current.screenWidthDp}  width=${
                            DisplayUtils.pxToDp(
                                DisplayUtils.getRealWidth(this)
                            )
                        }"
                    )
                    CreateView(homeViewModel)
                }
            }
        }
    }

    private fun initView() {
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val arrayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) arrayOf(
            Manifest.permission.QUERY_ALL_PACKAGES,
            Manifest.permission.VIBRATE
        ) else arrayOf(Manifest.permission.VIBRATE)
        if (!PermissionsUtil.checkPermissions(arrayPermission, this@MainActivity)) {
            requestPermissionLauncher.launch(arrayPermission)
        } else {
            initData()
        }
    }

    private fun initData() {
        val width = resources.displayMetrics.widthPixels
        val height = LauncherUtils.getScreenHeight3(this)
        homeViewModel.loadApp(packageManager, width = width, height = height, resources)
        homeViewModel.loadCardList(screenHeightDp = DisplayUtils.pxToDp(height), getItemData())
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // 在这里处理权限请求的结果
            var allPermissionsGranted = true
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (!isGranted) {
                    allPermissionsGranted = false
                    Log.d("MainActivity", "$permissionName is denied.")
                }
            }
            if (allPermissionsGranted) {
                // 如果所有权限都被授予，你可以进行需要这些权限的操作
                Log.d("MainActivity", "All permissions are granted.")
                initData()
            } else {
                // 如果有权限被拒绝，你可以向用户显示一个解��，或者禁用需要这些权限的功能
                Log.d("MainActivity", "Not all permissions are granted.")
            }
        }
}

@SuppressLint("UnrememberedMutableState", "SuspiciousIndentation")
@Composable
fun CreateView(homeViewModel: HomeViewModel) {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp
    LauncherConfig.HOME_WIDTH = width
    LauncherConfig.HOME_HEIGHT = height
    GridCardConfig.HOME_WIDTH = width
    GridCardConfig.HOME_HEIGHT = height
    val versionLiveState = homeViewModel.appVersionLiveData.observeAsState()
    val appList = homeViewModel.infoBaseBean
    val cardList = homeViewModel.carList

    LogUtils.e("recreate ${versionLiveState.value} ")

    ComposeLauncherTheme {
        Scaffold(content = { padding ->
            Image(
                painter = painterResource(id = R.drawable.wall_paper),
                contentDescription = "",
                modifier = Modifier
                    .padding(padding)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            val version = versionLiveState.value

            LogUtils.e("init view $version")
            if (appList.homeList.size == 0) {
                InitView()
            } else {
//                DesktopView(lists = appList)
                GridCardListView(cardList)
            }
        })

    }
}
