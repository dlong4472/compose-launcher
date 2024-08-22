package com.lin.comlauncher.util

import com.lin.comlauncher.entity.ApplicationInfo

object SortUtils {
    fun calculLeftPos(
        toList: ArrayList<ApplicationInfo>, pagePos: Int, app: ApplicationInfo
    ) {
        if (toList.size >= LauncherConfig.HOME_PAGE_CELL_NUM) {
            return
        }
        app.orignX = toList.size % 4
        app.orignY = toList.size / 4
    }

    fun calculPos(
        list: ArrayList<ApplicationInfo>, app: ApplicationInfo
    ) {
        var currentPos = findCurrentCell(app.posX, app.posY)
        if (app.position == LauncherConfig.POSITION_HOME) {

            if (currentPos < 0)
                currentPos = 0
            else if (currentPos >= list.size)
                currentPos = list.size - 1
            var isEmpty = true
            list.forEach {
                if (it.cellPos == currentPos) {
                    isEmpty = false
                    return@forEach
                }
            }
            if (isEmpty) {
                findPosByCell(currentPos)?.let {
                    app.orignX = it[0]
                    app.orignY = it[1]
                    app.cellPos = currentPos
                }
            }
        }
    }

    /**
     * 重置应用列表和工具列表中的应用位置
     */
    fun resetChoosePos(
        list: ArrayList<ApplicationInfo>, app: ApplicationInfo,
        toolList: ArrayList<ApplicationInfo>
    ) {
        // 遍历应用列表（list）中的每一个应用
        list.forEach continuing@{
            // 如果当前应用（it）是被拖动的应用（app），那么就跳过当前应用，继续处理下一个应用。
            if (app == it)
                return@continuing
            // 如果当前应用不是被拖动的应用，那么就将其原始位置（orignX 和 orignY）设置为其当前位置（posX 和 posY）。
            // 这样做的目的是将应用的原始位置更新为其当前位置，以便在下一次拖动时，应用可以从正确的位置开始移动。
            it.orignX = it.posX
            it.orignY = it.posY
        }
        // 这一行开始遍历工具列表（toolList）中的每一个应用。
        toolList.forEach continuing@{
            if (app == it)
                return@continuing
            // 当前应用不是被拖动的应用，那么就将其原始位置设置为其当前位置。
            it.orignX = it.posX
            it.orignY = it.posY
        }
        // 计算被拖动的应用的新位置。新位置是根据被拖动的应用的当前位置（posX 和 posY）计算的。
        var currentPose = findCurrentCell(app.posX, app.posY)
        // 计算被拖动的应用的原始位置。
        val prePos = findCurrentCell(app.orignX, app.orignY)
        LogUtils.e("cellIndex=${currentPose} preCell=${prePos}")
//        LogUtils.e("currentPos=${-currenPos-100} prePos=$prePos pos=${app.position} pos=${app.position}")
        // 检查被拖动的应用是否在主页上。如果是，那么就执行大括号中的代码。
        if (app.position == LauncherConfig.POSITION_HOME) {
            // 如果被拖动的应用的新位置与其原始位置相同，那么就不进行任何操作，直接返回。
            if (currentPose == prePos)
                return
            // 检查新位置是否小于等于-100。如果是，那么就执行大括号中的代码
            if (currentPose <= -100) {
                // 将新位置的值取反，然后减去100。这样做的目的是将新位置的值转换为工具栏中的位置。
                currentPose = -currentPose - 100
                // 查找工具列表中位置等于新位置的应用。如果找到了，那么就执行 let 语句块中的代码。
                toolList.firstOrNull { it.cellPos == currentPose }?.let { destApp ->
                    LogUtils.e("1  ${app.dragInfo == destApp} dragInfo=${app.dragInfo}")
                    val appCell = destApp.cellPos
                    if (destApp == app.dragInfo) {
                        return
                    } else {
                        val dragInfo = app.dragInfo
                        if (dragInfo != null) {
                            val cacheOrignX = destApp.orignX
                            val cacheOrignY = destApp.orignY
                            destApp.needMoveX = -dragInfo.orignX + destApp.posX
                            destApp.needMoveY = -dragInfo.orignY + destApp.posY
                            destApp.orignX = dragInfo.orignX
                            destApp.orignY = dragInfo.orignY
                            destApp.cellPos = dragInfo.cellPos
                            destApp.showText = true

                            dragInfo.orignX = app.orignX
                            dragInfo.orignY = app.orignY
                            dragInfo.needMoveX = dragInfo.posX - app.orignX
                            dragInfo.needMoveY = dragInfo.posY - app.orignY
                            dragInfo.cellPos = app.cellPos
                            dragInfo.showText = false

                            app.orignX = destApp.posX
                            app.orignY = destApp.posY
                            app.needMoveX = app.orignX - app.posX
                            app.needMoveY = app.orignY - app.posY
                            app.dragInfo = destApp
                            app.cellPos = appCell
                            app.showText = false
                        } else {
                            destApp.needMoveX = -app.orignX + destApp.posX
                            destApp.needMoveY = -app.orignY + destApp.posY
                            destApp.orignX = app.orignX
                            destApp.orignY = app.orignY
                            destApp.cellPos = app.cellPos
                            destApp.showText = true

                            app.orignX = destApp.posX
                            app.orignY = destApp.posY
                            app.needMoveX = app.orignX - app.posX
                            app.needMoveY = app.orignY - app.posY
                            app.dragInfo = destApp
                            app.cellPos = appCell
                            app.showText = false

                        }
                    }
                }
                return
            }
            app.dragInfo?.let { dragInfo ->
                // 这三行代码将 dragInfo 的原始位置和单元格位置保存到临时变量中。
                val cOrignX = dragInfo.orignX
                val cOrignY = dragInfo.orignY
                val appCell = dragInfo.cellPos
                // 这两行代码将 dragInfo 的原始位置设置为 app 的原始位置。
                dragInfo.orignX = app.orignX
                dragInfo.orignY = app.orignY
                // 这两行代码计算 dragInfo 需要移动的距离。
                dragInfo.needMoveX = dragInfo.posX - app.orignX
                dragInfo.needMoveY = dragInfo.posY - app.orignY
                // 这一行代码将 dragInfo 的单元格位置设置为 app 的单元格位置。
                dragInfo.cellPos = app.cellPos
                // 这一行代码将 dragInfo 的显示文本设置为 false。
                dragInfo.showText = false
                // 这两行代码将 app 的原始位置设置为 dragInfo 的原始位置。
                app.orignX = cOrignX
                app.orignY = cOrignY
                // 这两行代码计算 app 需要移动的距离。
                app.needMoveX = app.orignX - app.posX
                app.needMoveY = app.orignY - app.posY
                // 这一行代码将 app 的 dragInfo 属性设置为 null。
                app.dragInfo = null
                // 这一行代码将 app 的单元格位置设置为 dragInfo 的单元格位置。
                app.cellPos = appCell
                // 这一行代码将 app 的 showText 属性设置为 true。
                app.showText = true
            }
            // 这两行代码确保 currentPose 在有效范围内。
            if (currentPose < 0)
                currentPose = 0
            else if (currentPose >= list.size)
                currentPose = list.size - 1
            // 这一行代码将 app 的单元格位置设置为 currentPose。
            app.cellPos = currentPose
            // 这一行代码初始化一个变量 mIndex，用于在后面的循环中计算应用的位置。
            var mIndex = 0
            // 这一行开始遍历按单元格位置排序的应用列表。
            list.sortedBy { it.cellPos }.forEachIndexed { pos, ai ->
                // 这一行计算应用的新位置。
                val index = if (ai == app)
                    currentPose
                else if (currentPose < prePos) {
                    if (mIndex < currentPose) mIndex else mIndex + 1
                } else {
                    if (mIndex >= currentPose) mIndex + 1 else mIndex
                }
                // 这两行根据应用的位置计算其原始位置。
                ai.orignX = LauncherConfig.HOME_DEFAULT_PADDING_LEFT + (index % 4) * ai.width
                ai.orignY =
                    index / 4 * LauncherConfig.HOME_CELL_HEIGHT + LauncherConfig.DEFAULT_TOP_PADDING
                ai.needMoveX = ai.posX - ai.orignX
                ai.needMoveY = ai.posY - ai.orignY
                // 这一行将应用的单元格位置设置为 index。
                ai.cellPos = index
                // 如果当前应用不是被拖动的应用，那么就增加 mIndex 的值。
                if (ai != app)
                    mIndex++
            }
        } else {
            if (currentPose == prePos || currentPose >= 0 || prePos >= 0)
                return
            currentPose = -currentPose - 100
            app.cellPos = currentPose
            var mIndex = 0
            toolList.sortedBy { it.cellPos }.forEachIndexed { pos, ai ->
                val index = if (ai == app)
                    currentPose
                else if (currentPose < prePos) {
                    if (mIndex < currentPose) mIndex else mIndex + 1
                } else {
                    if (mIndex >= currentPose) mIndex + 1 else mIndex
                }

                ai.orignX = LauncherConfig.HOME_DEFAULT_PADDING_LEFT + (index) * ai.width
                ai.orignY = LauncherConfig.HOME_HEIGHT - LauncherConfig.HOME_CELL_WIDTH
                ai.needMoveX = ai.posX - ai.orignX
                ai.needMoveY = ai.posY - ai.orignY
                ai.cellPos = index
                if (ai != app)
                    mIndex++
            }
        }

    }

    fun findCurrentCellByPos(posX: Int, posY: Int): Int {
        val padding = 10
        if (posY < LauncherConfig.DEFAULT_TOP_PADDING) {
            return -1
        }
        if (posX <= LauncherConfig.HOME_DEFAULT_PADDING_LEFT)
            return LauncherConfig.CELL_POS_HOME_LEFT
        if (posX >= LauncherConfig.HOME_WIDTH - LauncherConfig.HOME_DEFAULT_PADDING_LEFT)
            return LauncherConfig.CELL_POS_HOME_RIGHT

        if (posY >= LauncherConfig.HOME_TOOLBAR_START - 40) {
            val pos = (posX + LauncherConfig.HOME_CELL_WIDTH / 2) / LauncherConfig.HOME_CELL_WIDTH
            return -pos - 100
        }

        val cellX = (posX - LauncherConfig.HOME_DEFAULT_PADDING_LEFT) / (LauncherConfig.HOME_CELL_WIDTH)


        val cellY = (posY - LauncherConfig.DEFAULT_TOP_PADDING) / LauncherConfig.HOME_CELL_HEIGHT

        LogUtils.e("cell=$cellX  cellY=$cellY de=${posX / (LauncherConfig.HOME_WIDTH / 8)}")

        return cellX + cellY * 4
    }

    fun findCurrentActorPix(list: List<ApplicationInfo>, pixX: Int, pixY: Int): ApplicationInfo? {
        val posX = DisplayUtils.pxToDp(pixX)
        val posY = DisplayUtils.pxToDp(pixY)
        list.forEach {
            if (posX >= it.posX && posX < it.posX + it.width && posY >= it.posY && posY < it.posY + it.height) {
                return it
            }
        }
        return null
    }

    fun findCurrentActorDp(list: List<ApplicationInfo>, dpX: Int, dpY: Int): ApplicationInfo? {
        val posX = dpX
        val posY = dpY
        list.forEach {
            if (!it.isDrag && posX >= it.posX && posX < it.posX + it.width && posY >= it.posY && posY < it.posY + it.height) {
                return it
            }
        }
        return null
    }

    fun findCurrentActorFolder(list: List<ApplicationInfo>, pixX: Int, pixY: Int): ApplicationInfo? {
        val posX = DisplayUtils.pxToDp(pixX)
        val posY = DisplayUtils.pxToDp(pixY)
        list.forEach {
            if (posX >= it.posX && posX < it.posX + it.width && posY >= it.posY && posY < it.posY + it.height) {
                return it
            }
        }
        return null
    }

    fun findCurrentActorCell(list: List<ApplicationInfo>, cellX: Int, cellY: Int): ApplicationInfo? {
        list.forEach {
            if (cellX + cellY * 4 == it.cellPos) {
                return it
            }
        }
        return null
    }

    fun findCurrentCell(posX: Int, posY: Int): Int {
        if (posY < LauncherConfig.DEFAULT_TOP_PADDING - LauncherConfig.CELL_ICON_WIDTH / 2) {
            return -1
        }
        val centerX = posX + LauncherConfig.HOME_CELL_WIDTH
//        LogUtils.e("posX = $posX width=${LauncherConfig.HOME_WIDTH}")

        if (posX <= -LauncherConfig.HOME_CELL_WIDTH / 3) {
            return LauncherConfig.CELL_POS_HOME_LEFT
        } else if (posX >= LauncherConfig.HOME_WIDTH - LauncherConfig.HOME_CELL_WIDTH * 2 / 3) {
            return LauncherConfig.CELL_POS_HOME_RIGHT
        }
        if (posY >= LauncherConfig.HOME_TOOLBAR_START - 40) {
            val pos = (posX + LauncherConfig.HOME_CELL_WIDTH / 2) / LauncherConfig.HOME_CELL_WIDTH
            return -pos - 100
        }

        val cellX = (posX + LauncherConfig.HOME_CELL_WIDTH / 2) / LauncherConfig.HOME_CELL_WIDTH


        val cellY = (posY - LauncherConfig.DEFAULT_TOP_PADDING
                + LauncherConfig.HOME_CELL_HEIGHT / 2) / LauncherConfig.HOME_CELL_HEIGHT
//        LogUtils.e("cell=$cellX  cellY=$cellY de=${posX / (LauncherConfig.HOME_WIDTH/8)}")

        return cellX + cellY * 4
    }

    fun findPosByCell(currentCell: Int): Array<Int>? {
        if (currentCell > 100 && currentCell < 0)
            return null
        val cellX = currentCell % 4
        val cellY = currentCell / 4
        val posX = cellX * LauncherConfig.HOME_CELL_WIDTH + LauncherConfig.HOME_DEFAULT_PADDING_LEFT
        val posY = cellY * LauncherConfig.HOME_CELL_HEIGHT + LauncherConfig.DEFAULT_TOP_PADDING
        return arrayOf(posX, posY)
    }

    fun swapChange(applist: ArrayList<ApplicationInfo>, toolList: ArrayList<ApplicationInfo>, app: ApplicationInfo) {
        val app1 = app
        val app2 = app.dragInfo
        if (app1 != null && app2 != null) {
            val index = toolList.indexOf(app2)
            toolList.remove(app2)
            toolList.add(index, app1)
            applist.remove(app1)
            applist.add(app2)

            app.position = LauncherConfig.POSITION_TOOLBAR
            app2.position = LauncherConfig.POSITION_HOME
            LogUtils.e("swap")
        }

        app.dragInfo = null
    }

}

