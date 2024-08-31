package com.lin.comlauncher.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lin.comlauncher.util.DisplayUtils
import com.lin.comlauncher.util.GridCardConfig
import com.lin.comlauncher.util.SortUtils.getItemHeight

const val LogDebug = true
const val LogDebug_GridCardListView = true

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun GridCardListView(
    columns: MutableList<List<GridItemData>>, pagerIndex: Int = 0,
    outPadding: Dp,
    inPadding: Dp
) {
    val coroutineAnimScope = rememberCoroutineScope()
    val dragInfoState = remember { mutableStateOf<GridItemData?>(null) }
    val dragUpState = remember {
        mutableStateOf(false)
    }
    val offsetX = remember { mutableStateOf(0.dp) }
    val offsetY = remember { mutableStateOf(0.dp) }
    val currentSelect = remember { mutableIntStateOf(0) }

    val groupedColumns = columns.chunked(3)
    val pagerSize = groupedColumns.size
    val pagerState = rememberPagerState(initialPage = pagerIndex)
    if (LogDebug && LogDebug_GridCardListView) Log.d(
        "GridLayoutView",
        "GridCardListView----columns:${columns.size}, pagerSize:$pagerSize," +
                " pagerState:${pagerState.pageCount}"
    )
    HorizontalPager(
        userScrollEnabled = !dragUpState.value,
        count = pagerSize, state = pagerState, modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .pointerInput(0) {
                detectLongPress(
                    cardList = groupedColumns,
                    currentSel = currentSelect,
                    coroutineAnimScope = coroutineAnimScope,
                    dragUpState = dragUpState,
                    dragInfoState = dragInfoState,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    state = pagerState
                )
            }
    ) { page ->
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "GridCardListView----HorizontalPager----page,currentPage:${pagerState.currentPage}"
        )
        currentSelect.value = pagerState.currentPage
        GridList(
            groupedColumns[page],
            outPadding = outPadding,
            inPadding = inPadding
        )
    }

    if (dragUpState.value) {
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "GridCardListView----dragUpState.value：${dragUpState.value}"
        )
        dragInfoState.value?.let {
            if (LogDebug && LogDebug_GridCardListView) Log.d(
                "GridLayoutView",
                "GridCardListView----dragInfoState.value：${it.id}, " +
                        "posX:${it.posX}, posY:${it.posY}, posFx:${it.posFx}, posFy:${it.posFy}"
            )
            Box(
                modifier = Modifier
                    .offset(it.posX.dp, it.posY.dp)
                    .border(2.dp, Color.Red)
            ) {
                CardView(
                    it,
                    initPos = false,
                    isAlpha = false
                )
            }

        }
    }

    if (offsetX.value != 0.dp || offsetY.value != 0.dp) {
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "GridCardListView----offsetX:${offsetX.value}, offsetY:${offsetY.value}"
        )
    }
}

@Composable
fun GridList(
    columns: List<List<GridItemData>>,
    outPadding: Dp,
    inPadding: Dp
) {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp
    Column(
        modifier = Modifier
            .width(width = width.dp)
            .height(height = height.dp)
            .offset(0.dp, 0.dp)

    ) {
        Box(
            modifier = Modifier
                .size(outPadding)
        )
        Row {
            Box(
                modifier = Modifier
                    .size(outPadding)
            )
            var rowIndex = 0
            columns.forEach { rowList ->
                Column {
                    var columnIndex = 0
                    rowList.forEach { item ->
                        CardView(item)
                        val isLast = columnIndex == rowList.size - 1
                        if (!isLast) Box(
                            modifier = Modifier
                                .size(inPadding)
                        )
                        columnIndex++
                    }
                }
                val isLast = rowIndex == columns.size - 1
                if (!isLast) Box(
                    modifier = Modifier
                        .size(inPadding)
                )
                rowIndex += 1
            }
            Box(
                modifier = Modifier
                    .size(outPadding)
            )
        }
        Box(
            modifier = Modifier
                .size(outPadding)
        )
    }
}

private const val LogDebug_CardView = true

@Composable
fun CardView(
    i: GridItemData,
    isAlpha: Boolean = true,
    initPos: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(
                width = i.cellCommonWidth.dp,
                height = i.cellHeight.dp
            )
            .alpha(if (isAlpha) (if (i.isDrag) 0f else 1f) else 1f)
            .background(Color.Yellow)
            .onGloballyPositioned { layoutCoordinates ->
                if (!initPos) return@onGloballyPositioned
                val position = layoutCoordinates.positionInRoot()
                if (LogDebug && LogDebug_CardView) Log.d(
                    "GridLayoutView",
                    "CardView----onGloballyPositioned----初始化位置----id:${i.id}, " +
                            "x:${DisplayUtils.pxToDp(position.x.toInt())}, y:${
                                DisplayUtils.pxToDp(
                                    position.y.toInt()
                                )
                            }"
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = i.id.toString(),
            color = Color.Black,
            style = androidx.compose.ui.text.TextStyle(fontSize = 50.sp)
        )
    }
}


@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun GridCardListViewPreview(@PreviewParameter(GridItemDataProvider::class) list: MutableList<GridItemData>) {
    val viewHeight = 720.dp.value.toInt()
    val betweenPaddingDp = 10.dp
    val betweenPadding = betweenPaddingDp.value.toInt()
    val topBottomPaddingDp = 20.dp
    val topBottomPadding = topBottomPaddingDp.value.toInt() * 2
    val cellSize = (viewHeight - topBottomPadding - betweenPadding * 3) / 4
    val columns = reSortItems(
        viewHeight, betweenPadding, topBottomPadding, cellSize,
        outPadding = topBottomPadding / 2,
        list
    )
    GridCardListView(
        columns,
        pagerIndex = 0,
        outPadding = topBottomPaddingDp,
        inPadding = betweenPaddingDp
    )
}

private const val LogDebug_reSortItems = false

/**
 * 重新排序卡片列表
 */
@Synchronized
@Suppress("SameParameterValue")
fun reSortItems(
    viewHeight: Int,
    betweenPadding: Int,
    topBottomPadding: Int,
    cellSize: Int,
    outPadding: Int = 0,
    items: MutableList<GridItemData>
): MutableList<List<GridItemData>> {
    if (LogDebug && LogDebug_reSortItems) Log.d(
        "GridLayoutView",
        "reSortItems----viewHeight:$viewHeight, betweenPadding:$betweenPadding, " +
                "topBottomPadding:$topBottomPadding, cellSize:$cellSize, items:${items.size}"
    )
    val maxColumnHeight = viewHeight - topBottomPadding // Subtract padding from view height
    // 计算每一列的高度
    var currentColumnHeight = 0
    // 当前列的所有项
    var currentColumnItems = mutableListOf<GridItemData>()
    // 所有列
    val columns = mutableListOf<List<GridItemData>>()
    // 遍历所有项
    for (i in items.indices) {
        if (i > items.size - 1) {
            break
        }
        val item = items[i]
        // 计算当前项的高度
        val itemHeightCurrent = getItemHeight(item, cellSize, betweenPadding)
        if (currentColumnHeight + itemHeightCurrent > maxColumnHeight) {
//                // Check if there is enough space left in the column for another item
            val remainingHeight = maxColumnHeight - currentColumnHeight
            if (LogDebug && LogDebug_reSortItems) Log.d(
                "GridLayoutView",
                "reSortItems----items.indices----坑位排序----超过当前高度----" +
                        "\ncurrentColumnHeight:$currentColumnHeight, " +
                        "remainingHeight:$remainingHeight, id:${item.id}"
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
                    "GridLayoutView",
                    "reSortItems----items.indices----寻找补坑---重新寻找下标:startIndex:$startIndex"
                )
                for (iChecker in startIndex until items.size) {
                    if (iChecker > items.size - 1) {
                        if (LogDebug && LogDebug_reSortItems) Log.d(
                            "GridLayoutView",
                            "reSortItems----items.indices----寻找补坑---超出范围"
                        )
                        break
                    }
                    if (LogDebug && LogDebug_reSortItems) Log.d(
                        "GridLayoutView",
                        "reSortItems----items.indices----寻找补坑---从id:${items[iChecker].id}开始"
                    )
                    val nextItemIndexChecker = items.subList(iChecker, items.size).indexOfFirst {
                        getItemHeight(
                            it,
                            cellSize,
                            betweenPadding
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
                        if (LogDebug && LogDebug_reSortItems) Log.d(
                            "GridLayoutView",
                            "reSortItems----items.indices----找到补坑----id:${nextItem.id},\n" +
                                    " itemHeightChecker:$itemHeightChecker, " +
                                    "currentColumnHeight:$currentColumnHeight, isOverHeight:$isOverHeight"
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
        } else {
            if (LogDebug && LogDebug_reSortItems) Log.d(
                "GridLayoutView",
                "reSortItems----items.indices----坑位排序----id:${item.id}"
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

private const val LogDebug_initCellSize = true

private fun initCellSize(
    betweenPadding: Int,
    cellSize: Int,
    outPadding: Int,
    columns: MutableList<List<GridItemData>>
) {
    GridCardConfig.DEFAULT_TOP_PADDING = outPadding
    GridCardConfig.HOME_DEFAULT_PADDING_LEFT = GridCardConfig.DEFAULT_TOP_PADDING
    GridCardConfig.HOME_TOOLBAR_START = GridCardConfig.DEFAULT_TOP_PADDING
    val cellCommonWidth = cellSize * 2 + betweenPadding
    GridCardConfig.HOME_CELL_WIDTH = DisplayUtils.dpToPx(cellCommonWidth)
    // init common value
    val groupedColumns = columns.chunked(3)
    var groupedColumnsIndex = 0
    groupedColumns.forEach { pagerItem ->
        var pagerItemIndex = 0
        pagerItem.forEach { columnItem ->
            var columnItemIndex = 0
            var columnsHeight = 0
            columnItem.forEach {
                it.posX =
                    outPadding + betweenPadding * pagerItemIndex + cellCommonWidth * pagerItemIndex
                it.posY = outPadding + columnsHeight + betweenPadding * columnItemIndex
                it.cellSize = cellSize
                it.cellCommonWidth = cellCommonWidth
                it.betweenPadding = betweenPadding
                it.cellHeight = getItemHeight(it, cellSize, betweenPadding)
                columnsHeight += it.cellHeight
                if (LogDebug && LogDebug_initCellSize) Log.d(
                    "GridLayoutView",
                    "initCellSize----初始化位置----id:${it.id}, posX:${it.posX}, posY:${it.posY}," +
                            " 页groupedColumnsIndex:$groupedColumnsIndex, 列pagerItemIndex:$pagerItemIndex, " +
                            "组columnItemIndex:$columnItemIndex, cellSize:${it.cellSize}, " +
                            "cellCommonWidth:${it.cellCommonWidth}, betweenPadding:${it.betweenPadding}, " +
                            "cellHeight:${it.cellHeight}"
                )
                columnItemIndex++
            }
            pagerItemIndex++
        }
        groupedColumnsIndex++
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
    var cellHeight: Int = 0
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
        cellHeight = cellHeight
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