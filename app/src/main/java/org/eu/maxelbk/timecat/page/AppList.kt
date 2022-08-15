package org.eu.maxelbk.timecat.page

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*
import org.eu.maxelbk.timecat.MainActivity
import org.eu.maxelbk.timecat.R
import org.eu.maxelbk.timecat.composable.AppListItem
import org.eu.maxelbk.timecat.composable.DefaultAppBar
import org.eu.maxelbk.timecat.composable.DefaultLayout
import java.util.*

internal val appList = Collections.synchronizedList(arrayListOf<AppItem>())

private var changed = false
private var refreshed: MutableState<Boolean>? = null
private var showSystem = false

internal fun requireRefresh() {
    refreshed?.value = false
}

@Composable
fun AppListPage(nav: NavHostController, activity: MainActivity) {
    DefaultLayout(
        topBar = {
            val navTopBar = rememberNavController()
            NavHost(navController = navTopBar, startDestination = "title") {
                composable("title") { TitleBar(nav, navTopBar, activity) }
                composable("search") { SearchBar(navTopBar) }
            }
        },
        floatingContent = {
            FloatingActionButton(
                onClick = { finish(nav) },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.finish))
            }
        }
    ) {
        if (refreshed == null) refreshed = remember { mutableStateOf(false) }
        if (searchText == null) searchText = remember { mutableStateOf("") }
        if (refreshed!!.value) {
            LazyColumn(Modifier
                .fillMaxWidth()
            ) {
                item {
                    Column(Modifier.fillMaxWidth()
                        .clickable { showSystem = !showSystem; cancel(nav, activity, true) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(Modifier.padding(5.dp)) {
                            val showSystemOptionText =
                                if (showSystem) stringResource(R.string.search_hideSystem)
                                else stringResource(R.string.search_showSystem)
                            Text(
                                text = showSystemOptionText,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    textDecoration = TextDecoration.Underline),
                                modifier = Modifier.padding(start = 5.dp),
                            )
                        }
                    }
                }
                items(appList) {
                    if (it.contains(searchText!!.value)) {
                        val checked0 = remember { mutableStateOf(it.realChecked) }
                        fun check() {
                            checked0.value = !checked0.value
                            it.checkedTmp = checked0.value
                            changed = true
                        }

                        AppListItem(
                            icon = it.icon,
                            name = it.name,
                            packageName = it.packageName,
                            clickable = true,
                            onClick = { check() },
                        ) {
                            Checkbox(
                                checked = checked0.value,
                                onCheckedChange = null,
                                Modifier
                                    .fillMaxHeight()
                                    .padding(5.dp)
                            )
                        }
                    }
                }
                item { Box(Modifier.padding(bottom = 100.dp)) }
            }
            BackHandler(true) {
                cancel(nav, activity)
            }
        } else {
            val progress = remember { mutableStateOf(activity.getString(R.string.loading)) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 50.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(Modifier.size(50.dp))
                Text(progress.value, Modifier.padding(10.dp))
            }
            LaunchedEffect(progress) {
                withContext(Dispatchers.IO) {
                    refreshAppList(activity, progress)
                }
            }
        }
    }
}

private fun finish(nav: NavHostController) {
    if (refreshed!!.value) {
        if (changed) {
            appList.forEach {
                it.checkedTmp?.let { _ ->
                    it.selected = it.checkedTmp!!
                    it.checkedTmp = null
                    if (it.selected) it.add()
                    else {
                        it.delete()
                    }
                    return@let
                }
            }
            requireReload()
            changed = false
            MainActivity.saveConfig()
        }
        nav.popBackStack()
    }
}

private fun cancel(nav: NavHostController, activity: MainActivity, refresh: Boolean = false) {
    fun back() {
        if (refresh) { if (refreshed!!.value) refreshed!!.value = false }
        else nav.popBackStack()
        changed = false
    }

    if (changed) {
        activity.createConfirmDialog(
            title = activity.getString(R.string.cancel),
            message = activity.getString(R.string.cancel_confirm),
            positiveButtonText = activity.getString(R.string.confirm_yes),
            negativeButtonText = activity.getString(R.string.confirm_no),
        ) { dialog, _ ->
            dialog.cancel()
            back()
            appList.forEach { it.checkedTmp = null }
        }.show()
    } else {
        back()
    }
}

@Suppress("DEPRECATION") // Fuck your mother shit, deprecated Google
private fun refreshAppList(
    activity: MainActivity,
    progress: MutableState<String>,
) {
    appList.clear()
    Log.i("T-AppList", "Cleared")
    val pm = activity.packageManager
    val packages = pm.getInstalledPackages(0)
    val sum = packages.size
    val loadingText = activity.getString(R.string.loading)
    Log.i("T-AppList", "Loading app list from $sum app(s)")
    packages.forEachIndexed { index, it ->
        val isUserApp = it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
        val packageName = it.packageName
        if (
            isUserApp || showSystem
            && !packageName.startsWith("android")
            && !packageName.contains("launcher")
        ) {
            val name = it.applicationInfo.loadLabel(pm).toString()
            val icon = drawableToBitmap(it.applicationInfo.loadIcon(pm))
            val checked = MainActivity.config.packages.contains(packageName)
            if (name != "Timecat")
            appList += AppItem(icon, name, packageName, isUserApp, checked)
            Log.d("T-AppList", "Add [${if (isUserApp) "User" else "System"}] $packageName")
        }
        progress.value = "$loadingText ${index + 1}/$sum"
    }
    Log.i("T-AppList", "Loaded ${appList.size} app(s)")
    progress.value = activity.getString(R.string.sorting)
    appList.sort()
    Log.i("T-AppList", "Sort finished")
    refreshed!!.value = true
    progress.value = loadingText
}

internal class AppItem(
    icon: Bitmap?,
    name: String,
    packageName: String,
    val isUserApp: Boolean,
    var selected: Boolean,
    var checkedTmp: Boolean? = null,
) : ComparableAppItem(icon, name, packageName) {
    val realChecked: Boolean get() {
        return if (checkedTmp == null) selected
        else checkedTmp!!
    }

    fun contains(other: String): Boolean {
        return name.contains(other = other, ignoreCase = true)
                || packageName.contains(other = other)
    }
}

// --- TopBar ---

@Composable
private fun TitleBar(nav: NavHostController, navTopBar: NavHostController, activity: MainActivity) {
    DefaultAppBar(
        title = stringResource(R.string.page_appList),
        leftIcon = {
            IconButton(onClick = { cancel(nav, activity) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel))
            }
        },
        rightElements = {
            IconButton(onClick = { cancel(nav, activity, true) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.back))
            }
            IconButton(onClick = { if (refreshed!!.value) navTopBar.navigate("search") }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search))
            }
        }
    )
}

var searchText: MutableState<String>? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(navTopBar: NavHostController) {
    DefaultAppBar(
        title = {
            TextField(
                value = searchText!!.value,
                onValueChange = { searchText!!.value = it },
                placeholder = { Text(stringResource(R.string.search_hint), color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0)),
                textStyle = TextStyle(fontSize = 16.sp),
            )
        },
        rightElements = {
            IconButton(onClick = { navTopBar.popBackStack(); searchText!!.value = "" }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel))
            }
        }
    )
}
