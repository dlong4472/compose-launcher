package com.lin.comlauncher.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lin.comlauncher.util.SortUtils.getItemHeight

const val LogDebug = true
const val LogDebug_GridCardListView = true

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalPagerApi::class)
@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun GridCardListView(@PreviewParameter(GridItemDataProvider::class) columns: MutableList<List<GridItemData>>) {
    var dragInfoState = remember { mutableStateOf<GridItemData?>(null) }
    var dragUpState = remember {
        mutableStateOf(false)
    }
    var offsetX = remember { mutableStateOf(0.dp) }
    var offsetY = remember { mutableStateOf(0.dp) }
    var currentSelect = remember { mutableStateOf(0) }

    val viewHeight = LocalConfiguration.current.screenHeightDp
    val betweenPadding = 10
    val topBottomPadding = 20 * 2
    val cellSize = (viewHeight - topBottomPadding - betweenPadding * 3) / 4
    if (LogDebug && LogDebug_GridCardListView) Log.d(
        "GridLayoutView",
        "GridCardListView----viewHeight:$viewHeight, cellSize:$cellSize"
    )

//    val columns = reSortItems(viewHeight, betweenPadding, topBottomPadding, cellSize, items)

    val groupedColumns = columns.chunked(3)
    val pagerSize = groupedColumns.size
    val cellCommonWidth = cellSize * 2 + betweenPadding
    val pagerState = rememberPagerState(initialPage = 0)
    if (LogDebug && LogDebug_GridCardListView) Log.d(
        "GridLayoutView",
        "GridCardListView----columns:${columns.size}, pagerSize:$pagerSize" +
                "cellCommonWidth:$cellCommonWidth, pagerState:${pagerState.pageCount}"
    )
    HorizontalPager(
        count = pagerSize, state = pagerState, modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .pointerInput(0) {
                detectLongPress(
                    cardList = groupedColumns,
                    currentSel = currentSelect,
                    dragUpState = dragUpState,
                    dragInfoState = dragInfoState,
                    offsetX = offsetX,
                    offsetY = offsetY
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
            betweenPadding,
            cellCommonWidth,
            cellSize
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(it.posX.dp, it.posY.dp)
            ) {
                CardView(
                    betweenPadding,
                    cellCommonWidth,
                    cellSize,
                    it,
                    initPos = false,
                    isAlpha = false
                )
            }

        }
    }

    if(offsetX.value != 0.dp || offsetY.value != 0.dp){
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "GridCardListView----offsetX:${offsetX.value}, offsetY:${offsetY.value}"
        )
    }
}


@Composable
fun GridList(
    columns: List<List<GridItemData>>,
    betweenPadding: Int,
    cellCommonWidth: Int,
    cellHeight: Int
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
                .size(20.dp)
        )
        Row {
            Box(
                modifier = Modifier
                    .size(20.dp)
            )
            columns.forEach { columnItems ->
                Column {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                    )
                    columnItems.forEach { item ->
                        CardView(betweenPadding, cellCommonWidth, cellHeight, item)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(20.dp)
        )
    }
}

private const val LogDebug_GridCardListView_LongPress = true

@Composable
fun CardView(
    betweenPadding: Int,
    cellCommonWidth: Int,
    cellHeight: Int,
    i: GridItemData,
    isAlpha: Boolean = true,
    initPos: Boolean = true,
) {
    val padding = if (i.height > 1) betweenPadding * (i.height - 1) else 0
    Box(
        modifier = Modifier
            .size(
                width = cellCommonWidth.dp,
                height = (cellHeight * i.height + padding).dp
            )
            .alpha(if (isAlpha) (if (i.isDrag) 0f else 1f) else 1f)
            .background(Color.Yellow)
            .onGloballyPositioned { layoutCoordinates ->
                if (!initPos) return@onGloballyPositioned
                val position = layoutCoordinates.positionInRoot()
                if (LogDebug && LogDebug_GridCardListView_LongPress) Log.d(
                    "GridLayoutView",
                    "CardView----onGloballyPositioned----id:${i.id}, " +
                            "x:${position.x}, y:${position.y}"
                )
                // 初始化位置
                if (i.posX == 0 && i.posY == 0 && position.x.toInt() > 0 && position.y.toInt() > 0) {
                    i.posX = position.x.toInt()
                    i.posY = position.y.toInt()
                    if (LogDebug && LogDebug_GridCardListView_LongPress) Log.d(
                        "GridLayoutView",
                        "CardView----onGloballyPositioned----初始化位置----id:${i.id}, " +
                                "posX:${i.posX}, posY:${i.posY}"
                    )
                    i.cellSize = cellHeight
                    i.cellCommonWidth = cellCommonWidth
                    i.betweenPadding = betweenPadding
                }
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
    return columns
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
    var cellSize: Int = 0
)

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