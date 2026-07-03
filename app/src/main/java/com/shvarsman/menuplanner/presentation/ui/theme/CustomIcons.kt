package com.shvarsman.menuplanner.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CustomIcons {

    val Banana: ImageVector
        get() {
            if (_LucideBanana != null) return _LucideBanana!!

            _LucideBanana = ImageVector.Builder(
                name = "banana",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(4f, 13f)
                    curveToRelative(3.5f, -2f, 8f, -2f, 10f, 2f)
                    arcToRelative(5.5f, 5.5f, 0f, false, true, 8f, 5f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(5.15f, 17.89f)
                    curveToRelative(5.52f, -1.52f, 8.65f, -6.89f, 7f, -12f)
                    curveTo(11.55f, 4f, 11.5f, 2f, 13f, 2f)
                    curveToRelative(3.22f, 0f, 5f, 5.5f, 5f, 8f)
                    curveToRelative(0f, 6.5f, -4.2f, 12f, -10.49f, 12f)
                    curveTo(5.11f, 22f, 2f, 22f, 2f, 20f)
                    curveToRelative(0f, -1.5f, 1.14f, -1.55f, 3.15f, -2.11f)
                    close()
                }
            }.build()

            return _LucideBanana!!
        }

    val Apple: ImageVector
        get() {
            if (_Apple != null) return _Apple!!

            _Apple = ImageVector.Builder(
                name = "apple",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 6.528f)
                    verticalLineTo(3f)
                    arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                    horizontalLineToRelative(0f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(18.237f, 21f)
                    arcTo(15f, 15f, 0f, false, false, 22f, 11f)
                    arcToRelative(6f, 6f, 0f, false, false, -10f, -4.472f)
                    arcTo(6f, 6f, 0f, false, false, 2f, 11f)
                    arcToRelative(15.1f, 15.1f, 0f, false, false, 3.763f, 10f)
                    arcToRelative(3f, 3f, 0f, false, false, 3.648f, 0.648f)
                    arcToRelative(5.5f, 5.5f, 0f, false, true, 5.178f, 0f)
                    arcTo(3f, 3f, 0f, false, false, 18.237f, 21f)
                }
            }.build()

            return _Apple!!
        }

    val Bean: ImageVector
        get() {
            if (_LucideBean != null) return _LucideBean!!

            _LucideBean = ImageVector.Builder(
                name = "bean",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(10.165f, 6.598f)
                    curveTo(9.954f, 7.478f, 9.64f, 8.36f, 9f, 9f)
                    curveToRelative(-0.64f, 0.64f, -1.521f, 0.954f, -2.402f, 1.165f)
                    arcTo(6f, 6f, 0f, false, false, 8f, 22f)
                    curveToRelative(7.732f, 0f, 14f, -6.268f, 14f, -14f)
                    arcToRelative(6f, 6f, 0f, false, false, -11.835f, -1.402f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(5.341f, 10.62f)
                    arcToRelative(4f, 4f, 0f, true, false, 5.279f, -5.28f)
                }
            }.build()

            return _LucideBean!!
        }

    val Beef: ImageVector
        get() {
            if (_LucideBeef != null) return _LucideBeef!!

            _LucideBeef = ImageVector.Builder(
                name = "beef",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(16.4f, 13.7f)
                    arcTo(6.5f, 6.5f, 0f, true, false, 6.28f, 6.6f)
                    curveToRelative(-1.1f, 3.13f, -0.78f, 3.9f, -3.18f, 6.08f)
                    arcTo(3f, 3f, 0f, false, false, 5f, 18f)
                    curveToRelative(4f, 0f, 8.4f, -1.8f, 11.4f, -4.3f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(18.5f, 6f)
                    lineToRelative(2.19f, 4.5f)
                    arcToRelative(6.48f, 6.48f, 0f, false, true, -2.29f, 7.2f)
                    curveTo(15.4f, 20.2f, 11f, 22f, 7f, 22f)
                    arcToRelative(3f, 3f, 0f, false, true, -2.68f, -1.66f)
                    lineTo(2.4f, 16.5f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(15f, 8.5f)
                    arcTo(2.5f, 2.5f, 0f, false, true, 12.5f, 11f)
                    arcTo(2.5f, 2.5f, 0f, false, true, 10f, 8.5f)
                    arcTo(2.5f, 2.5f, 0f, false, true, 15f, 8.5f)
                    close()
                }
            }.build()

            return _LucideBeef!!
        }

    val BottleWine: ImageVector
        get() {
            if (_LucideBottleWine != null) return _LucideBottleWine!!

            _LucideBottleWine = ImageVector.Builder(
                name = "bottle-wine",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(10f, 3f)
                    arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                    horizontalLineToRelative(2f)
                    arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
                    verticalLineToRelative(2f)
                    arcToRelative(6f, 6f, 0f, false, false, 1.2f, 3.6f)
                    lineToRelative(0.6f, 0.8f)
                    arcTo(6f, 6f, 0f, false, true, 17f, 13f)
                    verticalLineToRelative(8f)
                    arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
                    horizontalLineTo(8f)
                    arcToRelative(1f, 1f, 0f, false, true, -1f, -1f)
                    verticalLineToRelative(-8f)
                    arcToRelative(6f, 6f, 0f, false, true, 1.2f, -3.6f)
                    lineToRelative(0.6f, -0.8f)
                    arcTo(6f, 6f, 0f, false, false, 10f, 5f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(17f, 13f)
                    horizontalLineToRelative(-4f)
                    arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                    verticalLineToRelative(3f)
                    arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                    horizontalLineToRelative(4f)
                }
            }.build()

            return _LucideBottleWine!!
        }

    val Cake: ImageVector
        get() {
            if (_LucideCake != null) return _LucideCake!!

            _LucideCake = ImageVector.Builder(
                name = "cake",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(20f, 21f)
                    verticalLineToRelative(-8f)
                    arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
                    horizontalLineTo(6f)
                    arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
                    verticalLineToRelative(8f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(4f, 16f)
                    reflectiveCurveToRelative(0.5f, -1f, 2f, -1f)
                    reflectiveCurveToRelative(2.5f, 2f, 4f, 2f)
                    reflectiveCurveToRelative(2.5f, -2f, 4f, -2f)
                    reflectiveCurveToRelative(2.5f, 2f, 4f, 2f)
                    reflectiveCurveToRelative(2f, -1f, 2f, -1f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(2f, 21f)
                    horizontalLineToRelative(20f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(7f, 8f)
                    verticalLineToRelative(3f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 8f)
                    verticalLineToRelative(3f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(17f, 8f)
                    verticalLineToRelative(3f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(7f, 4f)
                    horizontalLineToRelative(0.01f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 4f)
                    horizontalLineToRelative(0.01f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(17f, 4f)
                    horizontalLineToRelative(0.01f)
                }
            }.build()

            return _LucideCake!!
        }

    val Candy: ImageVector
        get() {
            if (_LucideCandy != null) return _LucideCandy!!

            _LucideCandy = ImageVector.Builder(
                name = "candy",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(10f, 7f)
                    verticalLineToRelative(10.9f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(14f, 6.1f)
                    verticalLineTo(17f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(16f, 7f)
                    verticalLineTo(3f)
                    arcToRelative(1f, 1f, 0f, false, true, 1.707f, -0.707f)
                    arcToRelative(2.5f, 2.5f, 0f, false, false, 2.152f, 0.717f)
                    arcToRelative(1f, 1f, 0f, false, true, 1.131f, 1.131f)
                    arcToRelative(2.5f, 2.5f, 0f, false, false, 0.717f, 2.152f)
                    arcTo(1f, 1f, 0f, false, true, 21f, 8f)
                    horizontalLineToRelative(-4f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(16.536f, 7.465f)
                    arcToRelative(5f, 5f, 0f, false, false, -7.072f, 0f)
                    lineToRelative(-2f, 2f)
                    arcToRelative(5f, 5f, 0f, false, false, 0f, 7.07f)
                    arcToRelative(5f, 5f, 0f, false, false, 7.072f, 0f)
                    lineToRelative(2f, -2f)
                    arcToRelative(5f, 5f, 0f, false, false, 0f, -7.07f)
                }
                path(
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(8f, 17f)
                    verticalLineToRelative(4f)
                    arcToRelative(1f, 1f, 0f, false, true, -1.707f, 0.707f)
                    arcToRelative(2.5f, 2.5f, 0f, false, false, -2.152f, -0.717f)
                    arcToRelative(1f, 1f, 0f, false, true, -1.131f, -1.131f)
                    arcToRelative(2.5f, 2.5f, 0f, false, false, -0.717f, -2.152f)
                    arcTo(1f, 1f, 0f, false, true, 3f, 16f)
                    horizontalLineToRelative(4f)
                }
            }.build()

            return _LucideCandy!!
        }

    private var _LucideCandy: ImageVector? = null

    private var _LucideCake: ImageVector? = null

    private var _LucideBottleWine: ImageVector? = null

    private var _LucideBeef: ImageVector? = null

    private var _LucideBean: ImageVector? = null

    private var _Apple: ImageVector? = null

    private var _LucideBanana: ImageVector? = null
}