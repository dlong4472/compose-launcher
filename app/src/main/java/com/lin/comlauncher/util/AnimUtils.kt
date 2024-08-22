package com.lin.comlauncher.util

import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.lin.comlauncher.entity.AppPos

class AnimUtils {
}

/**
 * 这个方法是一个挂起函数，用于执行一个从起始位置到结束位置的动画。可以在协程中调用并暂停执行，而不会阻塞当前的线程
 * 它接受四个参数：起始位置（startPos），结束位置（endPos），动画持续时间（duration），以及一个回调函数（block）
 */
suspend fun DoTranslateAnim(
    startPos: AppPos,
    endPos: AppPos,
    duration: Int,
    block: (value: AppPos, velocity: AppPos) -> Unit
) {
    // 这是一个用于执行动画的函数。它接受一个类型转换器，一个初始值，一个目标值，一个初始速度，一个动画规格，以及一个回调函数
    animate(
        // 这是一个类型转换器，用于在 AppPos 对象和 AnimationVector2D 对象之间进行转换
        typeConverter = TwoWayConverter(
            // 这是一个将 AppPos 对象转换为 AnimationVector2D 对象的函数。
            convertToVector = { size: AppPos ->
                AnimationVector2D(
                    size.x.toFloat(),
                    size.y.toFloat()
                )
            },
            convertFromVector = { vector: AnimationVector2D ->
                AppPos(
                    vector.v1.toInt(),
                    vector.v2.toInt()
                )
            }
        ),
        // 动画的初始值，即应用的起始位置
        initialValue = startPos,
        // 动画的目标值，即应用的结束位置
        targetValue = endPos,
        // 动画的初始速度。在这个例子中，初始速度是0，表示应用在动画开始时是静止的。
        initialVelocity = AppPos(0, 0),
        // 这是动画的规格。tween 是一个创建时间插值动画的函数，它接受一个持续时间作为参数
        animationSpec = tween(duration),
        // 动画的回调函数。每当动画的值更新时，这个函数就会被调用。在这个例子中，回调函数接受两个参数：当前的位置和当前的速度。
        block = block
    )
}