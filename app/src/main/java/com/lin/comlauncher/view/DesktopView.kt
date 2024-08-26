package com.lin.comlauncher.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lin.comlauncher.entity.AppInfoBaseBean
import com.lin.comlauncher.entity.AppManagerBean
import com.lin.comlauncher.entity.ApplicationInfo
import com.lin.comlauncher.ui.theme.MyBasicColumn
import com.lin.comlauncher.ui.theme.pagerLazyFlingBehavior

@SuppressLint("MutableCollectionMutableState")
@Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DesktopView(@PreviewParameter(DesktopViewPreviewProvider::class) lists: AppInfoBaseBean) {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp
    val state = rememberLazyListState()
    val foldOpenState = remember { mutableStateOf<MutableList<ApplicationInfo>>(mutableListOf()) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val coroutineAnimScope = rememberCoroutineScope()

    val dragInfoState = remember { mutableStateOf<ApplicationInfo?>(null) }
    val dragUpState = remember {
        mutableStateOf(false)
    }

    val offsetX = remember { mutableStateOf(0.dp) }
    val offsetY = remember { mutableStateOf(0.dp) }
    val currentSelect = remember { mutableIntStateOf(0) }
    val animFinish = remember { mutableStateOf(false) }
    val appManagerState = remember { mutableStateOf<AppManagerBean?>(null) }

    val homeList = lists.homeList
    val toolBarList = lists.toobarList

    //draw dot
    val dotWidth = 8
    val indicationDot = homeList.size * dotWidth + (homeList.size - 1) * 6
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(width = indicationDot.dp)
            .height(height = height.dp)
            .offset(
                (width.dp - indicationDot.dp) / 2, (height - 150).dp
            )
    ) {
        homeList.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(dotWidth.dp)
                    .clip(CircleShape)
                    .background(Color(if (currentSelect.intValue == index) 0xccffffff else 0x66ffffff))
            )
        }
    }


    // draw toolbar
    lists.toobarList.let { appList ->
        MyBasicColumn(
            modifier = Modifier.zIndex(zIndex = 0f)
        ) {
            appList.forEachIndexed { _, it ->
                IconView(
                    it = it, dragUpState = dragUpState, foldOpen = foldOpenState
                )
            }
        }
//
    }

    LazyRow(

        modifier = Modifier
            .offset(0.dp, 0.dp)
            .width(width = width.dp)
            .height(height = height.dp)
            .pointerInteropFilter {
                if (it.action == MotionEvent.ACTION_DOWN) {
                    appManagerState.value = null
                }
                false
            }
            .pointerInput(0) {
                detectLongPress(
                    context = context,
                    toolList = toolBarList,
                    homeList = homeList,
                    currentSel = currentSelect,
                    coroutineScope = coroutineScope,
                    coroutineAnimScope = coroutineAnimScope,
                    dragInfoState = dragInfoState,
                    animFinish = animFinish,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    dragUpState = dragUpState,
                    state = state,
                    foldOpen = foldOpenState,
                    appManagerState = appManagerState
                )
            }, state = state, flingBehavior = pagerLazyFlingBehavior(
            state, lists.homeList.size
        )
    ) {
        currentSelect.intValue = state.firstVisibleItemIndex
        lists.homeList.let { homeList ->
            if (homeList.size == 0) return@let

            lists.homeList.forEachIndexed { _, appList ->
                item {
                    Column(
                        modifier = Modifier
                            .width(width = width.dp)
                            .height(height = height.dp)
                            .offset(0.dp, 0.dp)

                    ) {
                        MyBasicColumn {
                            appList.forEach {
                                IconView(
                                    it = it, dragUpState = dragUpState, foldOpen = foldOpenState
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //draw fold
    if (foldOpenState.value.size > 0) {
        Box(modifier = Modifier
            .size(width.dp, height.dp)
            .clickable {
                foldOpenState.value = mutableListOf()
            }) {
            Box(modifier = Modifier
                .size(width.dp - 20.dp, 320.dp)
                .offset(10.dp, (height.dp - 320.dp) / 2)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(0) {
                    detectLongPress(
                        context = context,
                        toolList = toolBarList,
                        homeList = homeList,
                        currentSel = currentSelect,
                        coroutineScope = coroutineScope,
                        coroutineAnimScope = coroutineAnimScope,
                        dragInfoState = dragInfoState,
                        animFinish = animFinish,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        dragUpState = dragUpState,
                        state = state,
                        foldOpen = foldOpenState,
                        appManagerState = appManagerState
                    )
                }
                .background(Color(0.3f, 0.3f, 0.3f, 0.8f))) {
                foldOpenState.value.forEach {
                    IconView(
                        it = it, dragUpState = dragUpState, foldOpen = foldOpenState
                    )
                }
            }
        }
    }

    //app more info
    appManagerState.value?.let {
        MoreInfoView(
            context = context,
            homeList = homeList,
            currentSel = currentSelect,
            appManagerState = appManagerState,
            coroutineScope = coroutineScope,
            coroutineAnimScope = coroutineAnimScope,
            offsetX = offsetX,
            offsetY = offsetY
        )
    }

    //current drag app
    if (dragUpState.value) {
        dragInfoState.value?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .size(it.width.dp, it.height.dp)
                    .offset(it.posX.dp, it.posY.dp)
            ) {
                IconViewDetail(it = it)
            }
        }
    }
}

class DesktopViewPreviewProvider : PreviewParameterProvider<AppInfoBaseBean> {
    override val values: Sequence<AppInfoBaseBean> = sequenceOf(
        AppInfoBaseBean(
            homeList = arrayListOf(
                arrayListOf(
                    ApplicationInfo(name = "App 1"), ApplicationInfo(name = "App 2")
                ), arrayListOf(
                    ApplicationInfo(name = "App 3"), ApplicationInfo(name = "App 4")
                )
            ), toobarList = arrayListOf(
                ApplicationInfo(name = "App 5"), ApplicationInfo(name = "App 6")
            )
        )
    )
}


