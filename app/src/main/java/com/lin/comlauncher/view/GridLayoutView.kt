package com.lin.comlauncher.view

import android.annotation.SuppressLint
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun PaddingExample() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        val viewHeight = maxHeight.value.toInt()
        val betweenPadding = 10
        val cellSize = (viewHeight - (20 * 2) - (betweenPadding * 3)) / 4
        val cellCommonWidth = (cellSize * 2 + betweenPadding).dp
        val cellHeight = cellSize.dp
        Row {
            Box(
                modifier = Modifier
                    .size(10.dp)
            )
            Column {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellHeight)
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellSize.dp * 2 + betweenPadding.dp)
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellHeight)
                        .background(Color.Red)

                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
            )
            Column {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellSize.dp * 2 + betweenPadding.dp)
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellSize.dp * 2 + betweenPadding.dp)
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
            )
            Column {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(
                            width = cellCommonWidth,
                            height = cellSize.dp * 3 + betweenPadding.dp * 2
                        )
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(width = cellCommonWidth, height = cellHeight)
                        .background(Color.Red)
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun PaddingExampleDynamic(@PreviewParameter(GridItemDataProvider::class) items: MutableList<GridItemData>) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        val viewHeight = maxHeight.value.toInt()
        val betweenPadding = 10
        val cellSize = (viewHeight - (20 * 2) - (betweenPadding * 3)) / 4
        val cellCommonWidth = (cellSize * 2 + betweenPadding).dp
        val cellHeight = cellSize.dp
        val maxColumnHeight = viewHeight - 40 // Subtract padding from view height
        // 计算每一列的高度
        var currentColumnHeight = 0
        // 当前列的所有项
        var currentColumnItems = mutableListOf<GridItemData>()
        // 所有列
        val columns = mutableListOf<List<GridItemData>>()
        // 遍历所有项
//        items.forEach { item ->
//            // 计算当前项的高度
//            val padding = if (item.height > 1) betweenPadding * (item.height - 1) else 0
//            val itemHeight =
//                cellHeight.value.toInt() * item.height + padding
//            if (currentColumnHeight + itemHeight > maxColumnHeight) {
//                // If adding this item would exceed the max column height, start a new column
//                columns.add(currentColumnItems)
//                currentColumnItems = mutableListOf(item)
//                currentColumnHeight = itemHeight
//            } else {
//                // Otherwise, add the item to the current column
//                currentColumnItems.add(item)
//                currentColumnHeight += itemHeight
//            }
//        }
        // 遍历所有项
        for (i in items.indices) {
            if (i > items.size - 1) {
                break
            }
            val item = items[i]
            // 计算当前项的高度
            val padding = if (item.height > 1) betweenPadding * (item.height - 1) else 0
            val itemHeight = cellHeight.value.toInt() * item.height + padding
            if (currentColumnHeight + itemHeight > maxColumnHeight) {
//                // Check if there is enough space left in the column for another item
                val remainingHeight = maxColumnHeight - currentColumnHeight
                Log.d("items.indices", "remainingHeight $remainingHeight")
                // Find the next item that fits the remaining height
                val nextItemIndex = items.subList(i + 1, items.size).indexOfFirst {
                    val padding = if (it.height > 1) betweenPadding * (it.height - 1) else 0
                    val itemHeight = cellHeight.value.toInt() * it.height + padding
                    itemHeight <= remainingHeight
                }
                if (nextItemIndex != -1) {
                    // If found, add it to the current column and remove it from the items list
                    val nextItem = items.removeAt(i + 1 + nextItemIndex)
                    val padding =
                        if (nextItem.height > 1) betweenPadding * (nextItem.height - 1) else 0
                    val itemHeight = cellHeight.value.toInt() * nextItem.height + padding
                    currentColumnItems.add(nextItem)
                    currentColumnHeight += itemHeight
                    continue
                } else {
                    // 没有找到，回退上一个，重新寻找高度刚刚好的 TODO 未完成
//                    if (currentColumnItems.isNotEmpty()) {
//                        val lastItem = currentColumnItems[currentColumnItems.size - 1]
//                        val paddingLastItem = if (lastItem.height > 1) betweenPadding * (lastItem.height - 1) else 0
//                        val itemHeightLastItem = cellHeight.value.toInt() * lastItem.height + paddingLastItem
//                        val nextItemIndex = items.subList(i + 1, items.size).indexOfFirst {
//                            val padding = if (it.height > 1) betweenPadding * (it.height - 1) else 0
//                            val itemHeight = cellHeight.value.toInt() * it.height + padding
//                            Log.d("重新寻找高度刚刚好","itemHeight: $itemHeight, equls:${maxColumnHeight - currentColumnHeight + itemHeightLastItem}, maxColumnHeight: $maxColumnHeight, currentColumnHeight: $currentColumnHeight, itemHeightLastItem: $itemHeightLastItem")
//                            itemHeight == maxColumnHeight - currentColumnHeight + itemHeightLastItem
//                        }
//                        if (nextItemIndex != -1) {
//                            currentColumnItems.removeAt(currentColumnItems.size - 1)
//                            currentColumnHeight -= itemHeightLastItem
//
//                            val nextItem = items.removeAt(i + 1 + nextItemIndex)
//                            val padding =
//                                if (nextItem.height > 1) betweenPadding * (nextItem.height - 1) else 0
//                            val itemHeight = cellHeight.value.toInt() * nextItem.height + padding
//                            currentColumnItems.add(nextItem)
//                            currentColumnHeight += itemHeight
//                            items.add(i, lastItem)
//                            continue
//                        }
//                    }
                }
                // If adding this item would exceed the max column height, start a new column
                columns.add(currentColumnItems)
                currentColumnItems = mutableListOf(item)
                currentColumnHeight = itemHeight
            } else {
                // Otherwise, add the item to the current column
                currentColumnItems.add(item)
                currentColumnHeight += itemHeight
            }
        }
        // Add the last column
        if (currentColumnItems.isNotEmpty()) {
            columns.add(currentColumnItems)
        }
        val groupedColumns = columns.chunked(3)
        val pagerState = rememberPagerState(initialPage = 1)
        HorizontalPager(count = groupedColumns.size, state = pagerState) { page ->
            GridList(
                groupedColumns[page],
                betweenPadding,
                cellCommonWidth.value.toInt(),
                cellHeight.value.toInt()
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

data class GridItemData(val id: Int, val height: Int)

class GridItemDataProvider : PreviewParameterProvider<MutableList<GridItemData>> {
    override val values: Sequence<MutableList<GridItemData>>
        get() = sequenceOf(
            getItemData()
        )
}

fun getItemData(): MutableList<GridItemData> {
    return mutableListOf(
        GridItemData(7, 1),
        GridItemData(1, 1),
        GridItemData(1, 1),
        GridItemData(2, 2),
        GridItemData(3, 1),
        GridItemData(4, 2),
        GridItemData(5, 2),
        GridItemData(6, 3),
        GridItemData(7, 1),
        GridItemData(8, 1),
        GridItemData(9, 2),
        GridItemData(4, 2),
        GridItemData(5, 2),
        GridItemData(6, 3),
        GridItemData(7, 1),
        GridItemData(1, 1),
        GridItemData(1, 1),
        GridItemData(2, 2),
        GridItemData(3, 1),
        GridItemData(4, 2),
        GridItemData(5, 2),
    )
}