package org.eu.maxelbk.timecat.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eu.maxelbk.timecat.ui.theme.topBarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultAppBar(
    title0: @Composable () -> Unit,
    leftIcon: @Composable () -> Unit = {},
    rightElements: @Composable RowScope.() -> Unit = {},
) {
    SmallTopAppBar(
        title = title0,
        navigationIcon = leftIcon,
        actions = rightElements,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = topBarBackground)
    )
}

@Composable
fun DefaultAppBar(
    title: String = "",
    leftIcon: @Composable () -> Unit = {},
    rightElements: @Composable RowScope.() -> Unit = {},
)
= DefaultAppBar(
    title0 = { Text(title) },
    leftIcon, rightElements)

@Composable
fun DefaultAppBar(
    title: @Composable RowScope.() -> Unit,
    leftIcon: @Composable () -> Unit = {},
    rightElements: @Composable RowScope.() -> Unit = {},
)
= DefaultAppBar(
    title0 = { Row(content = title, modifier = Modifier.height(IntrinsicSize.Max)) },
    leftIcon, rightElements)

@Composable
fun AppListItem(
    icon: Bitmap?,
    name: String,
    nameTextSize: TextUnit = 16.sp,
    nameHeight: Dp = 26.dp,
    packageName: String,
    packageNameTextSize: TextUnit = 12.sp,
    packageNameHeight: Dp = 22.dp,
    separatorWidth: Dp = 5.dp,
    padding: Dp = 8.dp,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
    rightContent: @Composable RowScope.() -> Unit,
) {
    val contentHeight = nameHeight + packageNameHeight
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(padding * 2 + contentHeight)
            .clickable(clickable, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            val modifier = Modifier
                .width(contentHeight)
                .fillMaxHeight()
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = "App",
                    modifier
                )
            } else {
                Image(
                    imageVector = Icons.Default.Close,
                    contentDescription = "App",
                    modifier
                )
            }
            Column(
                modifier = Modifier
                    .weight(10f)
                    .padding(start = separatorWidth)
            ) {
                Text(
                    name,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = nameTextSize
                    ),
                    maxLines = 1,
                    modifier = Modifier.height(nameHeight)
                )
                Text(
                    packageName,
                    color = Color.Gray,
                    style = TextStyle(fontSize = packageNameTextSize),
                    maxLines = 1,
                    modifier = Modifier.height(packageNameHeight)
                )
            }
            rightContent()
        }
    }
}