package com.lin.comlauncher.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lin.comlauncher.BuildConfig
import com.lin.comlauncher.util.DisplayUtils
import com.lin.comlauncher.util.GridCardConfig
import com.lin.comlauncher.util.SortUtils.getItemHeight

val LogDebug = BuildConfig.DEBUG
const val LogDebug_GridCardListView = false
private const val LogDebug_Tag = "GridLayoutView"

@Composable
fun GridCardListView(
    columns: MutableList<MutableList<GridItemData>>,
) {
    val animFinish = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val coroutineAnimScope = rememberCoroutineScope()
    val dragInfoState = remember { mutableStateOf<GridItemData?>(null) }
    val dragUpState = remember {
        mutableStateOf(false)
    }
    val offsetX = remember { mutableStateOf(0.dp) }
    val offsetY = remember { mutableStateOf(0.dp) }
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .pointerInput(0) {
                detectLongPressRow(
                    cardList = columns,
                    coroutineScope = coroutineScope,
                    coroutineAnimScope = coroutineAnimScope,
                    dragUpState = dragUpState,
                    dragInfoState = dragInfoState,
                    animFinish = animFinish,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    scrollState = scrollState
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(GridCardConfig.DEFAULT_TOP_PADDING.dp)
        )
        GridListAll(columns = columns)
    }

    if (dragUpState.value) {
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            LogDebug_Tag, "GridCardListView----dragUpState.value：${dragUpState.value}, " +
                    "Current scroll distance: ${scrollState.value}"
        )
        dragInfoState.value?.let {
            if (LogDebug && LogDebug_GridCardListView) Log.d(
                LogDebug_Tag,
                "GridCardListView----dragInfoState.value：${it.id}, " + "posX:${it.posX}, posY:${it.posY}, posFx:${it.posFx}, posFy:${it.posFy}"
            )
            val width = LocalConfiguration.current.screenWidthDp
            val height = LocalConfiguration.current.screenHeightDp
            val posX = it.posX - DisplayUtils.pxToDp(scrollState.value)
            val posY = it.posY
            Box(
                Modifier
                    .width(width = width.dp)
                    .height(height = height.dp)
                    .alpha(0.5f)
                    .offset(0.dp, 0.dp)
            ) {
                CardView(
                    it, offsetX = posX, initPos = false, isAlpha = false, reMove = true
                )
            }

        }
    }

    if (offsetX.value != 0.dp || offsetY.value != 0.dp) {
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            LogDebug_Tag, "GridCardListView----offsetX:${offsetX.value}, offsetY:${offsetY.value}"
        )
    }
}

@Composable
fun GridList(
    columns: List<List<GridItemData>>,
) {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp
    Box(
        Modifier
            .width(width = width.dp)
            .height(height = height.dp)
            .offset(0.dp, 0.dp)
    ) {
        columns.forEach { rowList ->
            rowList.forEach { item ->
                CardView(item)
            }
        }
    }
}

@Composable
fun GridListAll(
    columns: MutableList<MutableList<GridItemData>>,
) {
    val height = LocalConfiguration.current.screenHeightDp
    var columnsIndex = 0
    columns.forEach { rowList ->
        if (columnsIndex != 0) {
            Box(
                modifier = Modifier
                    .size((GridCardConfig.DEFAULT_TOP_PADDING / 2).dp)
            )
        }
        Column(
            Modifier
                .height(height = height.dp)
        ) {
            var rowIndex = 0
            rowList.forEach { item ->
                if (rowIndex != 0) {
                    Box(
                        modifier = Modifier
                            .size(item.betweenPadding.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(GridCardConfig.DEFAULT_TOP_PADDING.dp)
                    )
                }
                CardView(item)
                rowIndex++
            }
        }
        columnsIndex++
    }
}

private const val LogDebug_CardView = false

@Composable
fun CardView(
    i: GridItemData,
    offsetX: Int = i.posX,
    offsetY: Int = i.posY,
    isAlpha: Boolean = true,
    initPos: Boolean = true,
    reMove: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(
                width = i.cellCommonWidth.dp, height = i.cellHeight.dp
            )
            .offset(if (reMove) offsetX.dp else 0.dp, if (reMove) offsetY.dp else 0.dp)
            .alpha(if (isAlpha) (if (i.isDrag) 0f else 1f) else 1f)
            .background(Color.Yellow)
            .onGloballyPositioned { layoutCoordinates ->
                if (!initPos) return@onGloballyPositioned
                val position = layoutCoordinates.positionInRoot()
                if (LogDebug && LogDebug_CardView) Log.d(
                    LogDebug_Tag,
                    "CardView----onGloballyPositioned----初始化位置----id:${i.id}, " + "x:${
                        DisplayUtils.pxToDp(position.x.toInt())
                    }, y:${
                        DisplayUtils.pxToDp(
                            position.y.toInt()
                        )
                    }"
                )
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = i.id.toString(),
            color = Color.Black,
            style = androidx.compose.ui.text.TextStyle(fontSize = 50.sp)
        )
    }
}


@Preview(widthDp = 1280 * 2, heightDp = 720)
@Composable
fun GridCardListViewPreview(@PreviewParameter(GridItemDataProvider::class) list: MutableList<GridItemData>) {
    val viewHeight = 720.dp.value.toInt()
    val betweenPaddingDp = 10.dp
    val betweenPadding = betweenPaddingDp.value.toInt()
    val topBottomPaddingDp = 20.dp
    val topBottomPadding = topBottomPaddingDp.value.toInt() * 2
    val cellSize = (viewHeight - topBottomPadding - betweenPadding * 3) / 4
    val carList = initItems(
        viewHeight,
        betweenPadding,
        topBottomPadding,
        cellSize,
        outPadding = topBottomPadding / 2,
        list
    )
    GridCardListView(
        carList,
    )
}

private const val LogDebug_initItems = false

@Synchronized
fun initItems(
    viewHeight: Int,
    betweenPadding: Int,
    topBottomPadding: Int,
    cellSize: Int,
    outPadding: Int = 0,
    items: MutableList<GridItemData>,
    startInitIndex: Int = 0,
): MutableList<MutableList<GridItemData>> {
    if (LogDebug && LogDebug_initItems) Log.d(
        LogDebug_Tag,
        "reSortItems----viewHeight:$viewHeight, betweenPadding:$betweenPadding, " +
                "topBottomPadding:$topBottomPadding, cellSize:$cellSize, outPadding:$outPadding, " +
                "items:${items.size}"
    )
    val maxColumnHeight = viewHeight - topBottomPadding // Subtract padding from view height
    // 计算每一列的高度
    var currentColumnHeight = 0
    // 当前列的所有项
    var currentColumnItems = mutableListOf<GridItemData>()
    // 所有列
    val columns = mutableListOf<MutableList<GridItemData>>()
    // 遍历所有项
    var currentColumnIndex = 0
    for (i in startInitIndex until items.size) {
        if (i > items.size - 1) {
            break
        }
        val item = items[i]
        if (LogDebug && LogDebug_initItems) Log.d(
            LogDebug_Tag,
            "reSortItems----items.indices----id:${item.id}, currentColumnIndex:$currentColumnIndex"
        )
        // 计算当前项的高度
        val itemHeightCurrent = getItemHeight(item, cellSize, betweenPadding)
        if (currentColumnHeight + itemHeightCurrent > maxColumnHeight) {// 超过当前列高度
//                // Check if there is enough space left in the column for another item
            val remainingHeight = maxColumnHeight - currentColumnHeight
            if (LogDebug && LogDebug_initItems) Log.d(
                LogDebug_Tag,
                "reSortItems----items.indices----坑位排序----超过当前高度----" + "\ncurrentColumnHeight:$currentColumnHeight, " + "remainingHeight:$remainingHeight, id:${item.id}"
            )
            // 从剩余高度中找出可以放入的项
            if (remainingHeight >= cellSize) {
                // 找出下一个可以放入当前列的项，直到超出当前列的高度或者没有找到
                val startIndexChecker = 1
                val startIndexFind = items.subList(startIndexChecker, items.size).indexOfFirst {
                    it.id == item.id
                }
                val startIndex = startIndexFind + startIndexChecker
                if (LogDebug && LogDebug_initItems) Log.d(
                    LogDebug_Tag,
                    "reSortItems----items.indices----寻找补坑---重新寻找下标:startIndex:$startIndex"
                )
                for (iChecker in startIndex until items.size) {
                    if (iChecker > items.size - 1) {
                        if (LogDebug && LogDebug_initItems) Log.d(
                            LogDebug_Tag, "reSortItems----items.indices----寻找补坑---超出范围"
                        )
                        break
                    }
                    if (LogDebug && LogDebug_initItems) Log.d(
                        LogDebug_Tag,
                        "reSortItems----items.indices----寻找补坑---从id:${items[iChecker].id}开始"
                    )
                    val nextItemIndexChecker = items.subList(iChecker, items.size).indexOfFirst {
                        getItemHeight(
                            it, cellSize, betweenPadding
                        ) <= maxColumnHeight - currentColumnHeight
                    }
                    if (nextItemIndexChecker != -1) {
                        // If found, add it to the current column and remove it from the items list
                        val position = iChecker + nextItemIndexChecker
                        val nextItem = items.removeAt(position)
                        currentColumnItems.add(nextItem)
                        val itemHeightChecker = getItemHeight(nextItem, cellSize, betweenPadding)
                        currentColumnHeight += itemHeightChecker
                        val isOverHeight = maxColumnHeight - currentColumnHeight < itemHeightChecker
                        if (LogDebug && LogDebug_initItems) Log.d(
                            LogDebug_Tag,
                            "reSortItems----items.indices----找到补坑----id:${nextItem.id},\n" + " itemHeightChecker:$itemHeightChecker, " + "currentColumnHeight:$currentColumnHeight, isOverHeight:$isOverHeight"
                        )
                        if (isOverHeight) {
                            break
                        }
                    }

                }
            }
            // If adding this item would exceed the max column height, start a new column
            columns.add(currentColumnItems)
            currentColumnItems = mutableListOf(item)
            currentColumnHeight = itemHeightCurrent
            currentColumnIndex++
        } else {// 组装当前列
            if (LogDebug && LogDebug_initItems) Log.d(
                LogDebug_Tag, "reSortItems----items.indices----坑位排序----id:${item.id}"
            )
            // Otherwise, add the item to the current column
            currentColumnItems.add(item)
            currentColumnHeight += itemHeightCurrent
        }
    }
    // Add the last column
    if (currentColumnItems.isNotEmpty()) {
        columns.add(currentColumnItems)
    }
    initCellSize(betweenPadding, cellSize, outPadding, columns)
    return columns
}

private const val LogDebug_reSortItems = false

/**
 * 重新排序卡片列表
 */
@Synchronized
fun reSortItems(
    viewHeight: Int,
    betweenPadding: Int,
    topBottomPadding: Int,
    cellSize: Int,
    outPadding: Int = 0,
    items: MutableList<GridItemData>,
    selectItem: GridItemData? = null,
    replaceItem: GridItemData? = null,
    ignoreReSortList: MutableList<GridItemData>? = null,
    ignoreReSortColumnIndex: Int = -1,
): MutableList<MutableList<GridItemData>> {
    var ignoreReSortListStr = ""
    ignoreReSortList?.forEach {
        ignoreReSortListStr += "${it.id},"
    }
    if (LogDebug && LogDebug_reSortItems) Log.d(
        LogDebug_Tag,
        "reSortItems----viewHeight:$viewHeight, betweenPadding:$betweenPadding, " +
                "topBottomPadding:$topBottomPadding, cellSize:$cellSize, outPadding:$outPadding, " +
                "items:${items.size}, selectItem:${selectItem?.id}, replaceItem:${replaceItem?.id}, " +
                "ignoreReSortColumnIndex:$ignoreReSortColumnIndex" + " ignoreReSortList:$ignoreReSortList"
    )
    val maxColumnHeight = viewHeight - topBottomPadding // Subtract padding from view height
    // 计算每一列的高度
    var currentColumnHeight = 0
    // 当前列的所有项
    var currentColumnItems = mutableListOf<GridItemData>()
    // 所有列
    val columns = mutableListOf<MutableList<GridItemData>>()
    // 是否重新计算排序
    var isReSort = false
    val replaceIndex: Int
    // isReSort为ture，列表items中selectItem取出插入到replaceItem前面，记录下标，从replaceItem开始重新排序
    if (selectItem != null && replaceItem != null) {
        isReSort = true
        // 更新拖动卡片需要移动的位置
        selectItem.needMoveX = replaceItem.posX
        selectItem.needMoveY = replaceItem.posY
        val selectIndex = items.indexOf(selectItem)
        replaceIndex = items.indexOf(replaceItem)
        if (selectIndex != -1) {
            items.removeAt(selectIndex)
            if (replaceIndex <= items.size) {
                items.add(replaceIndex, selectItem)
            } else {
                items.add(selectItem)
            }
        }
        if (LogDebug && LogDebug_reSortItems) Log.d(
            LogDebug_Tag,
            "reSortItems----ignoreReSortColumnIndex:$ignoreReSortColumnIndex, ignoreReSortList:${ignoreReSortList?.size}"
        )
    }
    // 遍历所有项
    var currentColumnIndex = 0
    for (i in items.indices) {
        if (i > items.size - 1) {
            break
        }
        val item = items[i]
        if (LogDebug && LogDebug_reSortItems) Log.d(
            LogDebug_Tag,
            "reSortItems----items.indices----id:${item.id}, currentColumnIndex:$currentColumnIndex"
        )
        // 计算当前项的高度
        val itemHeightCurrent = getItemHeight(item, cellSize, betweenPadding)
        if (isReSort) {
            if (ignoreReSortColumnIndex == currentColumnIndex) { // 忽略重新排序
                if (LogDebug && LogDebug_reSortItems) Log.d(
                    LogDebug_Tag,
                    "reSortItems----items.indices----忽略重新排序"
                )
                var lastItem: GridItemData? = null
                if (currentColumnItems.isNotEmpty()) {
                    lastItem = currentColumnItems.last()
                }

                currentColumnItems.clear()
                if (ignoreReSortList != null) {
                    currentColumnItems.addAll(ignoreReSortList)
                    currentColumnHeight = 0
                    ignoreReSortList.forEach {
                        currentColumnHeight += getItemHeight(it, cellSize, betweenPadding)
                    }
                }
                columns.add(currentColumnItems)
                currentColumnIndex++

//                if (currentColumnHeight >= maxColumnHeight) {
//                    currentColumnIndex++
//                } else {
//                    val startIndexChecker = 1
//                    val startIndexFind = items.subList(startIndexChecker, items.size).indexOfFirst {
//                        it.id == item.id
//                    }
//                    val startIndex = startIndexFind + startIndexChecker
//                    val nextItemIndexChecker = items.subList(startIndex, items.size).indexOfFirst {
//                        var isIgnore = false
//                        ignoreReSortList?.forEach { ignoreReSortDetail ->
//                            if (ignoreReSortDetail.id == it.id) {
//                                isIgnore = true
//                            }
//                        }
//                        getItemHeight(
//                            it, cellSize, betweenPadding
//                        ) <= maxColumnHeight - currentColumnHeight && !isIgnore
//                    }
//                    if (nextItemIndexChecker != -1) {
//                        val position = startIndex + nextItemIndexChecker
//                        val nextItem = items.removeAt(position)
//                        currentColumnItems.add(nextItem)
//                        val itemHeightChecker = getItemHeight(nextItem, cellSize, betweenPadding)
//                        currentColumnHeight += itemHeightChecker
//                    }
//                    currentColumnIndex++
//                }
//
//                lastItem?.let { lastItem ->
//                    currentColumnItems = mutableListOf(lastItem)
//                    currentColumnHeight = getItemHeight(lastItem, cellSize, betweenPadding)
//                }

//                continue
            } else {
                var isIgnore = false
                ignoreReSortList?.forEach {
                    if (it.id == item.id) {
                        isIgnore = true
                    }
                }
                if (isIgnore) {
                    if (LogDebug && LogDebug_reSortItems) Log.d(
                        LogDebug_Tag,
                        "reSortItems----items.indices----忽略重新排序, id:${item.id}"
                    )
                    continue
                }
            }
        }
        if (currentColumnHeight + itemHeightCurrent > maxColumnHeight) {// 超过当前列高度
//                // Check if there is enough space left in the column for another item
            val remainingHeight = maxColumnHeight - currentColumnHeight
            if (LogDebug && LogDebug_reSortItems) Log.d(
                LogDebug_Tag,
                "reSortItems----items.indices----坑位排序----超过当前高度----" + "\ncurrentColumnHeight:$currentColumnHeight, " + "remainingHeight:$remainingHeight, id:${item.id}"
            )
            // 从剩余高度中找出可以放入的项
            if (remainingHeight >= cellSize) {
                // 找出下一个可以放入当前列的项，直到超出当前列的高度或者没有找到
                val startIndexChecker = 1
                val startIndexFind = items.subList(startIndexChecker, items.size).indexOfFirst {
                    it.id == item.id
                }
                val startIndex = startIndexFind + startIndexChecker
                if (LogDebug && LogDebug_reSortItems) Log.d(
                    LogDebug_Tag,
                    "reSortItems----items.indices----寻找补坑---重新寻找下标:startIndex:$startIndex"
                )
                for (iChecker in startIndex until items.size) {
                    if (iChecker > items.size - 1) {
                        if (LogDebug && LogDebug_reSortItems) Log.d(
                            LogDebug_Tag, "reSortItems----items.indices----寻找补坑---超出范围"
                        )
                        break
                    }
                    if (LogDebug && LogDebug_reSortItems) Log.d(
                        LogDebug_Tag,
                        "reSortItems----items.indices----寻找补坑---从id:${items[iChecker].id}开始"
                    )
                    val nextItemIndexChecker = items.subList(iChecker, items.size).indexOfFirst {
                        var isIgnore = false
                        if (isReSort) {
                            ignoreReSortList?.forEach { ignoreReSortDetail ->
                                if (ignoreReSortDetail.id == it.id) {
                                    isIgnore = true
                                }
                            }
                        }
                        getItemHeight(
                            it, cellSize, betweenPadding
                        ) <= maxColumnHeight - currentColumnHeight && !isIgnore
                    }
                    if (nextItemIndexChecker != -1) {
                        // If found, add it to the current column and remove it from the items list
                        val position = iChecker + nextItemIndexChecker
                        val nextItem = items.removeAt(position)
                        currentColumnItems.add(nextItem)
                        val itemHeightChecker = getItemHeight(nextItem, cellSize, betweenPadding)
                        currentColumnHeight += itemHeightChecker
                        val isOverHeight = maxColumnHeight - currentColumnHeight < itemHeightChecker
                        if (LogDebug && LogDebug_reSortItems) Log.d(
                            LogDebug_Tag,
                            "reSortItems----items.indices----找到补坑----id:${nextItem.id},\n" + " itemHeightChecker:$itemHeightChecker, " + "currentColumnHeight:$currentColumnHeight, isOverHeight:$isOverHeight"
                        )
                        if (isOverHeight) {
                            break
                        }
                    }

                }
            }
            // If adding this item would exceed the max column height, start a new column
            columns.add(currentColumnItems)
            currentColumnItems = mutableListOf(item)
            currentColumnHeight = itemHeightCurrent
            currentColumnIndex++
        } else {// 组装当前列
            if (LogDebug && LogDebug_reSortItems) Log.d(
                LogDebug_Tag, "reSortItems----items.indices----坑位排序----id:${item.id}"
            )
            // Otherwise, add the item to the current column
            currentColumnItems.add(item)
            currentColumnHeight += itemHeightCurrent
        }
    }
    // Add the last column
    if (currentColumnItems.isNotEmpty()) {
        columns.add(currentColumnItems)
    }
    initCellSize(betweenPadding, cellSize, outPadding, columns, isReSort = isReSort)
    return columns
}

private const val LogDebug_reSortItemsV2 = true

/**
 * 重新排序卡片列表
 */
@Synchronized
fun reSortItemsV2(
    viewHeight: Int,
    betweenPadding: Int,
    topBottomPadding: Int,
    cellSize: Int,
    outPadding: Int = 0,
    items: MutableList<GridItemData>,
    selectItem: GridItemData? = null,
    replaceItem: GridItemData? = null,
    ignoreReSortList: MutableList<GridItemData>,
    ignoreReSortColumnIndex: Int,
): MutableList<MutableList<GridItemData>> {
    if (LogDebug && LogDebug_reSortItemsV2) {
        var ignoreReSortListStr = ""
        var itemsStr = ""
        ignoreReSortList.forEach {
            ignoreReSortListStr += "${it.id},"
        }
        items.forEach {
            itemsStr += "${it.id},"
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----ignoreReSortColumnIndex:$ignoreReSortColumnIndex, " +
                    "ignoreReSortListStr:$ignoreReSortListStr itemsStr:$itemsStr"
        )
    }
    ignoreReSortList.forEach { ignoreReSortListBean ->
        val delId = ignoreReSortListBean.id
        items.removeAll { itemsBean ->
            itemsBean.id == delId
        }
    }
    if (LogDebug && LogDebug_reSortItemsV2) {
        var itemsStr = ""
        items.forEach {
            itemsStr += "${it.id},"
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----从items中删除ignoreReSortList----items:$itemsStr"
        )
    }
    var initList = initItems(
        viewHeight,
        betweenPadding,
        topBottomPadding,
        cellSize,
        outPadding,
        items
    )
    if (LogDebug && LogDebug_reSortItemsV2) {
        var initListStr = ""
        initList.forEach {
            it.forEach { gridItemData ->
                initListStr += "${gridItemData.id},"
            }
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----重新排序----initListStr:$initListStr"
        )
    }
    val ignoreReSortFrontList = mutableListOf<MutableList<GridItemData>>()
    var initListIndex = 0
    run {
        initList.forEach {
            if (initListIndex == ignoreReSortColumnIndex) return@run
            ignoreReSortFrontList.add(it)
            initListIndex++
        }
    }
    initList.add(ignoreReSortColumnIndex, ignoreReSortList)
    if (LogDebug && LogDebug_reSortItemsV2) {
        var initListStr = ""
        initList.forEach {
            it.forEach { gridItemData ->
                initListStr += "${gridItemData.id},"
            }
        }
        var ignoreReSortFrontListStr = ""
        ignoreReSortFrontList.forEach {
            it.forEach { gridItemData ->
                ignoreReSortFrontListStr += "${gridItemData.id},"
            }
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----将ignoreReSortList插入到指定列----initListStr:$initListStr " +
                    "ignoreReSortFrontListStr:$ignoreReSortFrontListStr"
        )
    }
    // 如果ignoreReSortList已经满格，则插入后更新各个信息后返回
    var ignoreReSortListHeight = 0
    ignoreReSortList.forEach {
        ignoreReSortListHeight += getItemHeight(it, cellSize, betweenPadding)
    }
    val maxColumnHeight = viewHeight - topBottomPadding
    if (LogDebug && LogDebug_reSortItemsV2) Log.d(
        LogDebug_Tag,
        "reSortItemsV2----检查ignoreReSortList高度----maxColumnHeight:$maxColumnHeight, " +
                "cellSize:$cellSize, ignoreReSortListHeight:$ignoreReSortListHeight"
    )
    if (maxColumnHeight - ignoreReSortListHeight < cellSize) {
        if (LogDebug && LogDebug_reSortItemsV2) Log.d(
            LogDebug_Tag,
            "reSortItemsV2----如果ignoreReSortList已经满格，则插入后更新各个信息后返回"
        )
        initCellSize(betweenPadding, cellSize, outPadding, initList)
        return initList
    }
    val initListNew = mutableListOf<GridItemData>()
    initList.forEach {
        initListNew.addAll(it)
    }
    if (LogDebug && LogDebug_reSortItemsV2) {
        var initListNewStr = ""
        initListNew.forEach {
            initListNewStr += "${it.id},"
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----将initList转换为一维数组----initListNewStr:$initListNewStr"
        )
    }
    var ignoreReSortListLastIndex = 0
    val ignoreReSortListLastBean = ignoreReSortList.first()
    initListNew.forEach {
        if (it.id == ignoreReSortListLastBean.id) {
            ignoreReSortListLastIndex = initListNew.indexOf(it)
        }
    }
    if (LogDebug && LogDebug_reSortItemsV2) {
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----从下标 ignoreReSortListLastIndex:$ignoreReSortListLastIndex 开始重新排序"
        )
    }
    initList = initItems(
        viewHeight,
        betweenPadding,
        topBottomPadding,
        cellSize,
        outPadding,
        initListNew,
        ignoreReSortListLastIndex
    )
    if (ignoreReSortFrontList.isNotEmpty()) {
        initList.addAll(0, ignoreReSortFrontList)
        // 重新计算各个元素位置信息
        initCellSize(betweenPadding, cellSize, outPadding, initList)
    }
    if (LogDebug && LogDebug_reSortItemsV2) {
        var initListStr = ""
        initList.forEach {
            it.forEach { gridItemData ->
                initListStr += "${gridItemData.id},"
            }
        }
        Log.d(
            LogDebug_Tag,
            "reSortItemsV2----重新排序----initListStr:$initListStr"
        )
    }
    return initList
}

private const val LogDebug_initCellSize = false

private fun initCellSize(
    betweenPadding: Int,
    cellSize: Int,
    outPadding: Int,
    columns: MutableList<MutableList<GridItemData>>,
    isReSort: Boolean = false
) {
    GridCardConfig.DEFAULT_TOP_PADDING = outPadding
    GridCardConfig.HOME_DEFAULT_PADDING_LEFT = GridCardConfig.DEFAULT_TOP_PADDING
    GridCardConfig.HOME_TOOLBAR_START = GridCardConfig.DEFAULT_TOP_PADDING
    val cellCommonWidth = cellSize * 2 + betweenPadding
    GridCardConfig.HOME_CELL_WIDTH = DisplayUtils.dpToPx(cellCommonWidth)

    var rowItemIndex = 0
    columns.forEach { columnsList ->
        var columnItemIndex = 0
        var columnsHeight = 0
        columnsList.forEach {
            if (isReSort) {
                it.needMoveX =
                    outPadding + betweenPadding * rowItemIndex + cellCommonWidth * rowItemIndex
                it.needMoveX -= it.posX
                it.needMoveY = outPadding + columnsHeight + betweenPadding * columnItemIndex
                it.needMoveY -= it.posY
            } else {
                it.posX =
                    outPadding + betweenPadding * rowItemIndex + cellCommonWidth * rowItemIndex
                it.orignX = it.posX
                it.posY = outPadding + columnsHeight + betweenPadding * columnItemIndex
                it.orignY = it.posY
            }
            it.cellSize = cellSize
            it.cellCommonWidth = cellCommonWidth
            it.betweenPadding = betweenPadding
            it.cellHeight = getItemHeight(it, cellSize, betweenPadding)
            columnsHeight += it.cellHeight
            if (LogDebug && LogDebug_initCellSize) Log.d(
                LogDebug_Tag,
                "initCellSize----初始化位置----id:${it.id}, posX:${it.posX}, posY:${it.posY}, " +
                        "needMoveX:${it.needMoveX}, needMoveY:${it.needMoveY}, " +
                        "orignX:${it.orignX}, orignY:${it.orignY}, 组rowItemIndex:$rowItemIndex" +
                        " 列columnItemIndex:$columnItemIndex, cellSize:${it.cellSize}, " +
                        "cellCommonWidth:${it.cellCommonWidth}, betweenPadding:${it.betweenPadding}, "
                        + "cellHeight:${it.cellHeight}"
            )
            columnItemIndex++
        }
        rowItemIndex++
    }
}


data class GridItemData(
    val id: Int,
    val height: Int,
    var isDrag: Boolean = false,
    var posX: Int = 0,
    var posY: Int = 0,
    var posFx: Float = 0f,
    var posFy: Float = 0f,
    var betweenPadding: Int = 0,
    var cellCommonWidth: Int = 0,
    var cellSize: Int = 0,
    var cellHeight: Int = 0,
    var orignX: Int = 0,
    var orignY: Int = 0,
    var needMoveX: Int = 0,
    var needMoveY: Int = 0,
    var pageIndex: Int = -1,
    var columnsIndex: Int = -1,
) {
    fun deepCopy() = GridItemData(
        id = id,
        height = height,
        isDrag = isDrag,
        posX = posX,
        posY = posY,
        posFx = posFx,
        posFy = posFy,
        betweenPadding = betweenPadding,
        cellCommonWidth = cellCommonWidth,
        cellSize = cellSize,
        cellHeight = cellHeight,
        orignX = orignX,
        orignY = orignY,
        needMoveX = needMoveX,
        needMoveY = needMoveY,
    )
}

class GridItemDataProvider : PreviewParameterProvider<MutableList<GridItemData>> {
    override val values: Sequence<MutableList<GridItemData>>
        get() = sequenceOf(
            getItemData()
        )
}

fun getItemData(): MutableList<GridItemData> {
    return mutableListOf(
        GridItemData(1, 1),
        GridItemData(2, 1),
        GridItemData(3, 3),
        GridItemData(4, 1),
        GridItemData(5, 2),
        GridItemData(6, 1),
        GridItemData(7, 3),
        GridItemData(8, 2),
        GridItemData(9, 2),
        GridItemData(10, 3),
        GridItemData(11, 1),
        GridItemData(12, 1),
        GridItemData(13, 2),
        GridItemData(14, 3),
        GridItemData(15, 2),
        GridItemData(16, 2),
        GridItemData(17, 1),
        GridItemData(18, 3),
        GridItemData(19, 2),
        GridItemData(20, 2),
        GridItemData(21, 3),
    )
}