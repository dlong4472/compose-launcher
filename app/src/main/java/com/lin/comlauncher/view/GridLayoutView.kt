package com.lin.comlauncher.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

private const val LogDebug = true
private const val LogDebug_GridCardListView = true

@OptIn(ExperimentalPagerApi::class)
@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun GridCardListView(@PreviewParameter(GridItemDataProvider::class) items: MutableList<GridItemData>) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        val viewHeight = maxHeight.value.toInt()
        val betweenPadding = 10
        val topBottomPadding = 20 * 2
        val cellSize = (viewHeight - topBottomPadding - betweenPadding * 3) / 4
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "PaddingExampleDynamic----viewHeight:$viewHeight, cellSize:$cellSize"
        )

        val columns = reSortItems(viewHeight, betweenPadding, topBottomPadding, cellSize, items)

        val groupedColumns = columns.chunked(3)
        val pagerSize = groupedColumns.size
        val cellCommonWidth = cellSize * 2 + betweenPadding
        val pagerState = rememberPagerState(initialPage = 0)
        if (LogDebug && LogDebug_GridCardListView) Log.d(
            "GridLayoutView",
            "PaddingExampleDynamic----columns:${columns.size}, pagerSize:$pagerSize" +
                    "cellCommonWidth:$cellCommonWidth, pagerState:${pagerState.pageCount}"
        )
        HorizontalPager(count = pagerSize, state = pagerState) { page ->
            GridList(
                groupedColumns[page],
                betweenPadding,
                cellCommonWidth,
                cellSize
            )
        }
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
                        val padding = if (item.height > 1) betweenPadding * (item.height - 1) else 0
                        Box(
                            modifier = Modifier
                                .size(
                                    width = cellCommonWidth.dp,
                                    height = (cellHeight * item.height + padding).dp
                                )
                                .background(Color.Yellow)
                        )
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


private const val LogDebug_reSortItems = true

/**
 * 重新排序卡片列表
 */
@Suppress("SameParameterValue")
private fun reSortItems(
    viewHeight: Int,
    betweenPadding: Int,
    topBottomPadding: Int,
    cellSize: Int,
    items: MutableList<GridItemData>
): MutableList<List<GridItemData>> {
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
                "items.indices",
                "remainingHeight $remainingHeight"
            )
            // Find the next item that fits the remaining height
            val nextItemIndexChecker = items.subList(i + 1, items.size).indexOfFirst {
                getItemHeight(it, cellSize, betweenPadding) <= remainingHeight
            }
            if (nextItemIndexChecker != -1) {
                // If found, add it to the current column and remove it from the items list
                val nextItem = items.removeAt(i + 1 + nextItemIndexChecker)
                currentColumnItems.add(nextItem)
                currentColumnHeight += getItemHeight(nextItem, cellSize, betweenPadding)
            } else {
                // 没有找到，回退上一个，重新寻找高度刚刚好的 TODO 未完成
//                if (currentColumnItems.isNotEmpty()) {
//                    val lastItem = currentColumnItems[currentColumnItems.size - 1]
//                    val paddingLastItem =
//                        if (lastItem.height > 1) betweenPadding * (lastItem.height - 1) else 0
//                    val itemHeightLastItem = cellSize * lastItem.height + paddingLastItem
//                    val nextItemIndex = items.subList(i + 1, items.size).indexOfFirst {
//                        val itemHeight = getItemHeight(it, cellSize, betweenPadding)
//                        if (LogDebug && LogDebug_reSortItems) Log.d(
//                            "重新寻找高度刚刚好", "itemHeight: $itemHeight, " +
//                                    "equals:${maxColumnHeight - currentColumnHeight + itemHeightLastItem}," +
//                                    " maxColumnHeight: $maxColumnHeight, " +
//                                    "currentColumnHeight: $currentColumnHeight, " +
//                                    "itemHeightLastItem: $itemHeightLastItem"
//                        )
//                        itemHeight == maxColumnHeight - currentColumnHeight + itemHeightLastItem
//                    }
//                    if (nextItemIndex != -1) {
//                        currentColumnItems.removeAt(currentColumnItems.size - 1)
//                        currentColumnHeight -= itemHeightLastItem
//                        val nextItem = items.removeAt(i + 1 + nextItemIndex)
//                        currentColumnItems.add(nextItem)
//                        currentColumnHeight += getItemHeight(nextItem, cellSize, betweenPadding)
//                        items.add(i, lastItem)
//                        continue
//                    }
//                }
            }
            // If adding this item would exceed the max column height, start a new column
            columns.add(currentColumnItems)
            currentColumnItems = mutableListOf(item)
            currentColumnHeight = itemHeightCurrent
        } else {
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

private fun getItemHeight(item: GridItemData, cellSize: Int, betweenPadding: Int): Int {
    val padding = if (item.height > 1) betweenPadding * (item.height - 1) else 0
    return cellSize * item.height + padding
}

data class GridItemData(val id: Int, val height: Int)

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
        GridItemData(3, 1),
        GridItemData(4, 2),
        GridItemData(5, 1),
        GridItemData(6, 2),
        GridItemData(7, 2),
        GridItemData(8, 3),
        GridItemData(9, 1),
        GridItemData(10, 1),
        GridItemData(11, 2),
        GridItemData(12, 2),
    )
}