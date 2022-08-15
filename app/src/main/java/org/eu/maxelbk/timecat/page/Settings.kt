package org.eu.maxelbk.timecat.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import org.eu.maxelbk.timecat.R
import org.eu.maxelbk.timecat.composable.DefaultAppBar
import org.eu.maxelbk.timecat.composable.DefaultFlowLayout

@Composable
fun SettingsPage(nav: NavHostController) {
    DefaultFlowLayout(topBar = {
        DefaultAppBar(
            title = stringResource(R.string.page_settings),
            leftIcon = {
                IconButton(onClick = {
                    nav.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back))
                }
            },
            rightElements = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more))
                }
            })
    }) {
        Text("There is nothing...")
    }
}
