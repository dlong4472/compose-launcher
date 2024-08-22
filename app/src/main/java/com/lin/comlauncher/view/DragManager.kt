package com.lin.comlauncher.view

import android.content.Context
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lin.comlauncher.entity.AppManagerBean
import com.lin.comlauncher.entity.AppPos
import com.lin.comlauncher.entity.ApplicationInfo
import com.lin.comlauncher.util.DisplayUtils
import com.lin.comlauncher.util.DoTranslateAnim
import com.lin.comlauncher.util.LauncherConfig
import com.lin.comlauncher.util.LauncherUtils
import com.lin.comlauncher.util.LogUtils
import com.lin.comlauncher.util.SortUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Suppress("unused")
class DragManager

/**
 * 处理长按拖动手势的函数。它在用户长按屏幕后开始拖动时被触发，并执行一系列操作来处理拖动事件。
 * 1.在拖动开始时（onDragStart）：
 * 根据是否有文件夹打开，从文件夹或应用列表中找到被拖动的应用。
 * 保存应用的原始位置，并更新应用的当前位置。
 * 启动一个协程，用于异步处理拖动过程中的操作，如检测应用是否仍在被拖动，是否被拖动到屏幕边缘，是否需要启动动画等。
 * 2.在拖动进行中（onDrag）：
 * 更新被拖动的应用的位置。
 * 检查应用是否被拖动了一定的距离，如果是，清除应用管理器的状态。
 * 3.在拖动结束时（onDragEnd）：
 * 如果应用被拖动到了新的页面，将应用从旧的页面移除，并添加到新的页面，然后启动一个动画，将应用从当前位置移动到新的位置。
 * 如果应用没有被拖动到新的页面，启动一个动画，将应用从当前位置移动回到原来的位置，然后清除拖动信息，并交换应用的位置。
 * 4.在拖动被取消时（onDragCancel）：
 * 清除拖动信息，并重置拖动状态。
 */
suspend fun PointerInputScope.detectLongPress(
    context: Context,
    toolList: ArrayList<ApplicationInfo>,
    homeList: ArrayList<ArrayList<ApplicationInfo>>,
    currentSel: MutableState<Int>,
    coroutineScope: CoroutineScope, coroutineAnimScope: CoroutineScope,
    dragInfoState: MutableState<ApplicationInfo?>, animFinish: MutableState<Boolean>,
    offsetX: MutableState<Dp>, offsetY: MutableState<Dp>,
    dragUpState: MutableState<Boolean>,
    state: LazyListState,
    foldOpen: MutableState<MutableList<ApplicationInfo>>,
    appManagerState: MutableState<AppManagerBean?>
) {
    detectDragGesturesAfterLongPress(
        onDragStart = { off ->
            // 定义一个可为空的 ApplicationInfo 变量 dragApp，用于存储被拖动的应用
            val dragApp: ApplicationInfo?
            // 获取当前选中的页面的应用列表。
            val appList = homeList[currentSel.value]
            // 检查是否有文件夹被打开。如果有，那么就在文件夹中查找被拖动的应用。
            if (foldOpen.value.size > 0) {
                // 计算文件夹的起始位置。这是文件夹的高度减去应用的高度的一半。
                //app in folder
                val startFolderPos =
                    (LauncherConfig.HOME_HEIGHT.dp - LauncherConfig.HOME_FOLDER_HEIGHT.dp) / 2
                // 获取手势开始时的纵坐标。
                val startY = off.y.toInt()
                // 在文件夹中查找被拖动的应用。
                dragApp =
                    SortUtils.findCurrentActorFolder(foldOpen.value, off.x.toInt(), startY)
                        ?.also { app ->
                            // 保存应用的原始位置
                            app.orignX = app.posX
                            app.orignY = app.posY
                            // 更新应用的浮点位置。这是应用的当前位置加上文件夹的起始位置。
                            app.posFx = app.posX.dp.toPx()
                            app.posFy = app.posY.dp.toPx() + startFolderPos.toPx()
                            // 更新应用的纵坐标。这是应用的当前纵坐标加上文件夹的起始位置。
                            app.posY = (app.posY + startFolderPos.value).toInt()
                        }
            } else {// 如果没有文件夹被打开，那么就在应用列表或工具列表中查找被拖动的应用。
                // 如果手势开始时的纵坐标大于或等于工具栏的起始位置，那么就在工具列表中查找被拖动的应用，否则就在应用列表中查找。
                dragApp = if (off.y.toDp().value >= LauncherConfig.HOME_TOOLBAR_START) {
                    SortUtils.findCurrentActorPix(toolList, off.x.toInt(), off.y.toInt())
                } else
                    SortUtils.findCurrentActorPix(appList, off.x.toInt(), off.y.toInt())
                dragApp?.also { app -> // 如果找到了被拖动的应用，那么就执行大括号中的代码。
                    app.orignX = app.posX
                    app.orignY = app.posY
                    app.posFx = app.posX.dp.toPx()
                    app.posFy = app.posY.dp.toPx()

                    // 显示应用管理器的布局。这是一个包含应用位置和应用本身的 AppManagerBean 对象。
                    //display app manager layout
                    appManagerState.value = AppManagerBean(app.posX, app.posY, app)
                }

            }
            // 如果没有找到被拖动的应用，那么就返回并结束拖动手势。
            val it = dragApp ?: return@detectDragGesturesAfterLongPress
            // 示应用正在被拖动。
            it.isDrag = true
            // 启动一个新的协程。这个协程会并发地执行大括号中的代码，而不会阻塞当前的线程。
            coroutineScope.launch {
                // 使设备振动。这是为了给用户一个反馈，表示应用正在被拖动。
                LauncherUtils.vibrator(context = context)
            }
            LogUtils.e("drag app ${it.name}")
            // 更新拖动信息。这是一个包含被拖动的应用的 ApplicationInfo 对象。
            dragInfoState.value = it
            // 表示拖动手势已经开始。
            dragUpState.value = true
            /**
             * 为了在拖动开始时异步执行一些操作，而不阻塞主线程
             * 1.检测应用是否仍在被拖动。
             * 2.如果应用在同一个单元格内停留的时间超过1个时间单位，
             * 那么就检查应用是否被拖动到了屏幕的边缘，如果是，那么就滚动屏幕。
             * 3.如果应用在同一个单元格内停留的时间超过1个时间单位，
             * 并且没有移动到新的单元格，那么就启动一个动画，将应用从当前位置移动到新的位置，并更新所有应用的位置。
             */
            coroutineAnimScope.launch {
                // 获取当前指针位置对应的单元格。
                var preCell =
                    SortUtils.findCurrentCellByPos(
                        DisplayUtils.pxToDp(off.x.toInt()),
                        DisplayUtils.pxToDp(off.y.toInt())
                    )
                var disPlayTime = 0
                var dragStop = false
                // 还在被拖动，就会一直执行
                while (it.isDrag) {
                    val preX = it.posX
                    val preY = it.posY
                    // 让当前的协程暂停150毫秒，这是为了让拖动手势有一个平滑的效果
                    delay(150)
                    if (!it.isDrag)
                        break
                    val curX = it.posX
                    val curY = it.posY
                    var movePage = false
                    // 位置变化不大，并且没有动画正在执行时，才会执行这个条件块
                    if (abs(preX - curX) < 10 && abs(
                            preY - curY
                        ) < 10 && !animFinish.value
                    ) {
                        val cellIndex =
                            SortUtils.findCurrentCell(
                                curX,
                                curY
                            )
                        val appInfo = SortUtils.findCurrentActorDp(list = appList, curX, curY)
                        // 当前位置的应用是一个文件夹，那么就跳过这次循环
                        if (appInfo?.appType == LauncherConfig.CELL_TYPE_FOLD) {
                            continue
                        }
                        LogUtils.e("cellIndex=${curX} preCell=${curY} name=${appInfo?.name} height=${appInfo?.height}")
                        // 没有移动到新的单元格，并且没有停止拖动，那么就跳过这次循环
                        if (preCell == cellIndex && !dragStop) {
                            dragStop = true
                            disPlayTime = 0
                            continue
                        } else if (preCell != cellIndex) {
                            dragStop = false
                            disPlayTime = 0
                        } else {
                            disPlayTime++
                        }
                        preCell = cellIndex
                        // 在同一个单元格内停留的时间超过1个时间单位，才会执行这个条件块
                        if (disPlayTime >= 1) {
                            // 检查应用是否被拖动到了屏幕的边缘，如果是，那么就滚动屏幕
                            if (cellIndex == LauncherConfig.CELL_POS_HOME_LEFT) {
                                if (state.firstVisibleItemIndex - 1 >= 0) {
                                    // 如果是，那么就滚动屏幕
                                    state.animateScrollToItem(state.firstVisibleItemIndex - 1)
                                    movePage = true
                                }

                            } else if (cellIndex == LauncherConfig.CELL_POS_HOME_RIGHT) {
                                if (state.firstVisibleItemIndex + 1 < state.layoutInfo.totalItemsCount) {
                                    // 如果是，那么就滚动屏幕
                                    state.animateScrollToItem(state.firstVisibleItemIndex + 1)
                                    movePage = true
                                }
                            }

                            if (movePage) {
//                                LogUtils.e("movePage")
                                delay(800)
                                continue
                            }
                            if (disPlayTime == 1) {
                                run {
                                    // 重置被拖动的应用的位置
                                    SortUtils.resetChoosePos(
                                        appList,
                                        it, toolList
                                    )
                                    val xScale = 100
                                    val yScale = 100
                                    animFinish.value = true
                                    DoTranslateAnim(
                                        AppPos(0, 0),
                                        AppPos(100, 100),
                                        300
                                    ) { appPos, _ ->
                                        appList.forEach continuing@{ appInfo ->
                                            // 检查当前应用（appInfo）是否是被拖动的应用（it），
                                            // 或者当前应用的原始位置（orignX 和 orignY）是否与其当前位置（posX 和 posY）相同
                                            if (appInfo == it || (appInfo.orignX == appInfo.posX && appInfo.orignY == appInfo.posY))
                                                return@continuing
                                            appInfo.posX =
                                                appInfo.orignX + (xScale - appPos.x) * appInfo.needMoveX / xScale
                                            appInfo.posY =
                                                appInfo.orignY + (yScale - appPos.y) * appInfo.needMoveY / yScale
                                        }
                                        toolList.forEach continuing@{ appInfo ->
                                            if (appInfo == it || (appInfo.orignX == appInfo.posX && appInfo.orignY == appInfo.posY))
                                                return@continuing
                                            appInfo.posX =
                                                appInfo.orignX + (xScale - appPos.x) * appInfo.needMoveX / xScale

                                            appInfo.posY =
                                                appInfo.orignY + (yScale - appPos.y) * appInfo.needMoveY / yScale
                                        }
                                        offsetX.value = appPos.x.dp
                                        offsetY.value = appPos.y.dp
                                    }
                                    // 更新所有应用的位置
                                    appList.forEach { appInfo ->
                                        if (appInfo == it)
                                            return@forEach
                                        appInfo.orignY = appInfo.posY
                                        appInfo.orignX = appInfo.posX
                                    }
                                    // 更新所有应用的位置
                                    toolList.forEach { appInfo ->
                                        if (appInfo == it)
                                            return@forEach
//                                        LogUtils.e("name=${appInfo.name}  orix = ${appInfo.orignX} pox=${  appInfo.posX}")
                                        appInfo.orignY = appInfo.posY
                                        appInfo.orignX = appInfo.posX
                                    }
                                    animFinish.value = false
                                }

                            }


                        }
                    }

                }
            }
        },
        onDragEnd = {
            dragInfoState.value?.let {
                if (it.position == LauncherConfig.POSITION_FOLD) {
                    it.isDrag = false
                    it.posX = it.orignX
                    it.posY = it.orignY
                    offsetX.value = 200.dp
                    dragInfoState.value = null
                    return@let
                }
                val appList = homeList[currentSel.value]
                LogUtils.e("current=${currentSel.value} pagePos =${it.pagePos}")
                // 在处理拖动手势结束时，如果应用被拖动到了新的页面
                if (it.pagePos != currentSel.value) {
                    // 获取当前选中的页面的应用列表
                    val toList = homeList[currentSel.value]
                    // 检查新页面是否已经满了。如果满了，那么就重置应用的位置和状态，并结束拖动。
                    if (toList.size >= LauncherConfig.HOME_PAGE_CELL_MAX_NUM) {
                        it.posX = it.orignX
                        it.posY = it.orignY
                        it.isDrag = false
                        return@let
                    }
                    // 计算应用在新页面中的位置。这里假设每个页面有4列，所以使用了模运算和整除运算。
                    it.orignX =
                        toList.size % 4 * LauncherConfig.HOME_CELL_WIDTH + LauncherConfig.HOME_DEFAULT_PADDING_LEFT
                    it.orignY =
                        toList.size / 4 * LauncherConfig.HOME_CELL_HEIGHT + LauncherConfig.DEFAULT_TOP_PADDING
                    // 更新应用的单元格位置。这里假设每个新添加的应用都会放在列表的末尾
                    it.cellPos = toList.size
                    // 更新偏移量。偏移量是指应用的当前位置与其原始位置的差值
                    offsetX.value = it.posX.dp
                    offsetY.value = it.posY.dp
                    // 重置拖动状态。这表示应用已经停止拖动。
                    dragUpState.value = false
                    /**
                     * 启动一个新的协程。这个协程会并发地执行大括号中的代码，而不会阻塞当前的线程:
                     * 如果应用被拖动到了新的页面，那么会将应用从旧的页面移除，并添加到新的页面。
                     * 然后启动一个动画，将应用从当前位置移动到新的位置。
                     */
                    coroutineScope.launch {
                        if (animFinish.value)
                            delay(200)
                        // 将应用从旧的页面移除
                        homeList[it.pagePos].remove(it)
                        // ，并添加到新的页面。
                        toList.add(it)
                        // 更新应用的页面位置。
                        it.pagePos = currentSel.value
                        // 启动一个动画，将应用从当前位置移动到新的位置。
                        DoTranslateAnim(
                            AppPos(it.posX, it.posY),
                            AppPos(it.orignX, it.orignY),
                            200
                        )
                        // 动画函数的回调函数参数: appPos 是应用的新位置，velocity 是应用的速度
                        { appPos, _ ->
                            it.posX = appPos.x
                            it.posY = appPos.y
                            offsetX.value = appPos.x.dp
                            offsetY.value = appPos.y.dp
                        }
                        // 在动画结束后，重置偏移量。这可能是为了准备下一次的拖动操作
                        offsetX.value = 200.dp
                    }

                } else {// 处理拖动手势结束时，如果应用没有被拖动到新的页面，进行的一系列操作
                    // 查找当前位置的应用
                    val appInfo = SortUtils.findCurrentActorDp(list = appList, it.posX, it.posY)
                    // 打印当前位置的应用的类型和名称。
                    LogUtils.e("appInfo=${appInfo?.appType} name=${appInfo?.name}")
                    // 计算应用的位置。
                    SortUtils.calculPos(appList, it)
                    // 检查当前位置的应用是否是一个文件夹，并且被拖动的应用是一个普通应用，并且文件夹还有空位。如果是，那么就将应用添加到文件夹中。
                    if (appInfo?.appType == LauncherConfig.CELL_TYPE_FOLD && it.appType == LauncherConfig.CELL_TYPE_APP && appInfo.childs.size < 12) {
                        // 将应用添加到文件夹中。
                        appInfo.childs.add(it)
                        // 如果文件夹中的应用数量不超过9个，那么就创建一个新的文件夹图标。
                        if (appInfo.childs.size <= 9) {
                            LauncherUtils.createFoldIcon(appInfo)
                        }
                        // 更新文件夹中的应用的位置。
                        LauncherUtils.changeFoldPosition(appInfo.childs)
                        // 将文件夹的位置设置为折叠状态。
                        appInfo.position = LauncherConfig.POSITION_FOLD
                        // 从应用列表中移除被拖动的应用。
                        appList.remove(it)
                        // 清除拖动信息。
                        dragInfoState.value = null
                        // 更新偏移量。偏移量是指应用的当前位置与其原始位置的差值
                        offsetX.value = it.posX.dp
                        offsetY.value = it.posY.dp
                    } else { // 如果当前位置的应用不是一个文件夹，或者被拖动的应用不是一个普通应用，或者文件夹已经满了，那么就执行这个条件块
                        // 更新偏移量
                        offsetX.value = it.posX.dp
                        offsetY.value = it.posY.dp
                        LogUtils.e("dragEnd ")
                        // 重置拖动状态。这表示应用已经停止拖动。
                        dragUpState.value = false
                        /**
                         * 启动一个新的协程。这个协程会并发地执行大括号中的代码，而不会阻塞当前的线程:
                         * 如果应用没有被拖动到新的页面，那么会启动一个动画，
                         * 将应用从当前位置移动回到原来的位置。然后清除拖动信息，并交换应用的位置
                         */
                        coroutineScope.launch {
                            if (animFinish.value)
                                delay(200)
                            // 启动一个动画，将应用从当前位置移动回到原来的位置。
                            DoTranslateAnim(
                                AppPos(it.posX, it.posY),
                                AppPos(it.orignX, it.orignY),
                                200
                            )
                            { appPos, _ ->
                                it.posX = appPos.x
                                it.posY = appPos.y
                                offsetX.value = appPos.x.dp
                                offsetY.value = appPos.y.dp
                            }
                            // 在动画结束后，清除拖动信息。
                            dragInfoState.value = null
                            // 交换应用的位置。
                            SortUtils.swapChange(applist = appList, toolList = toolList, app = it)
                            // 在动画结束后，重置偏移量。这可能是为了准备下一次的拖动操作。
                            offsetX.value = 200.dp
                        }
                    }
                }
                it.isDrag = false
            }
        },
        onDragCancel = {
            dragInfoState.value?.let {
                // 已经停止拖动。
                it.isDrag = false
                // 拖动手势已经结束。
                dragUpState.value = false
                LogUtils.e("drag cancle")
                // 没有应用正在被拖动。
                dragInfoState.value = null
            }
//            appManagerState.value = null
        }
    ) { change, dragAmount ->
        change.consume()
        dragInfoState.value?.let {
            // 更新被拖动的应用的浮点位置（posFx 和 posFy）
            it.posFx += dragAmount.x
            it.posFy += dragAmount.y
            // dragAmount.x 和 dragAmount.y 是指针位置的变化量，它们被加到当前的位置上，从而更新应用的位置
//                                        LogUtils.e("offx=${ it.posFx.toDp()} offy=${it.posFy.toDp()}")
            // 将浮点位置转换为整数位置（posX 和 posY）。这是因为在Android中，位置通常以像素为单位，而像素必须是整数
            it.posX = it.posFx.toDp().value.toInt()
            it.posY = it.posFy.toDp().value.toInt()
//            LogUtils.e("drag cellX = ${it.posX}  cellY=${it.posY}")
            // 更新偏移量（offsetX 和 offsetY）。偏移量是指应用的当前位置与其原始位置的差值。
            offsetX.value = dragAmount.x.toDp() + offsetX.value
            offsetY.value = dragAmount.y.toDp() + offsetY.value
            // 检查应用是否被拖动了一定的距离（APP_INFO_DRAG_DIS）。如果是，那么就清除应用管理器的状态（appManagerState）。
            if (abs(it.posX - it.orignX) > LauncherConfig.APP_INFO_DRAG_DIS || abs(it.posY - it.orignY) > LauncherConfig.APP_INFO_DRAG_DIS) {
                // 拖动了足够的距离，所以不再需要应用管理器的状态。
                appManagerState.value = null
            }
        }

    }
}
