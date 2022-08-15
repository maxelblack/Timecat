package org.eu.maxelbk.timecat.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp

private val floatingButtonPadding = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultLayout(
    topBar: @Composable () -> Unit = {},
    floatingContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    if (floatingContent == null) {
        Column {
            topBar()
            content()
        }
    } else {
        Scaffold(
            topBar = topBar,
            content = {
                Column(modifier = Modifier.padding(top = it.calculateTopPadding())) {
                    content()
                }
            },
            floatingActionButton = {
                Box(Modifier.padding(floatingButtonPadding), content = floatingContent) },
            floatingActionButtonPosition = FabPosition.End,
        )
    }
}

@Composable
fun DefaultFlowLayout(
    topBar: @Composable () -> Unit = {},
    floatingContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    DefaultLayout(
        topBar = topBar,
        floatingContent = floatingContent,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            content = content,
        )
    }
}
