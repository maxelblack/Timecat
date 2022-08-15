package org.eu.maxelbk.timecat.page

import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.eu.maxelbk.timecat.MainActivity
import org.eu.maxelbk.timecat.R
import org.eu.maxelbk.timecat.composable.AppListItem
import org.eu.maxelbk.timecat.composable.DefaultAppBar
import org.eu.maxelbk.timecat.composable.DefaultLayout
import org.eu.maxelbk.timecat.ui.theme.topBarBackground

private var loaded: MutableState<Boolean>? = null
private var refreshed: MutableState<Boolean>? = null

internal fun requireReload() {
    loaded?.value = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(nav: NavHostController, activity: MainActivity) {
    DefaultLayout(
        topBar = {
            val appName = stringResource(R.string.app_name)
            DefaultAppBar(
                title = {
                    Text(appName, Modifier.padding(top = 2.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = appName,
                        Modifier.width(36.dp)
                    )
                },
                rightElements = {
                    IconButton(onClick = {
                        nav.navigate("settings")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.page_settings))
                    }
                }
            )
        },
        floatingContent = {
            FloatingActionButton(
                onClick = { nav.navigate("app-list") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Application")
            }
        },
    ) {
        if (loaded == null) loaded = remember { mutableStateOf(false) }
        if (refreshed == null) refreshed = remember { mutableStateOf(true) }
        if (loaded!!.value) {
            if (refreshed!!.value) {
                LazyColumn {
                    item {
                        Box(Modifier.background(topBarBackground)) {
                            val config = MainActivity.config
                            var enabled0 by remember { mutableStateOf(config.enabled) }
                            Card(
                                onClick = {
                                    config.enabled = !config.enabled
                                    enabled0 = config.enabled
                                    MainActivity.saveConfig()
                                },
                                modifier = Modifier.height(80.dp)
                                    .padding(top = 2.dp, start = 10.dp, end = 10.dp, bottom = 12.dp)
                            ) {
                                Row(
                                    Modifier.padding(10.dp).fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Enabled",
                                        Modifier.weight(10f),
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                    Switch(checked = enabled0, onCheckedChange = null)
                                }
                            }
                        }
                    }
                    items(selectedAppList) {
                        AppListItem(
                            icon = it.icon,
                            name = it.name,
                            packageName = it.packageName,
                            padding = 10.dp,
                            separatorWidth = 10.dp,
                            clickable = true,
                        ) {
                            val enabled = remember { mutableStateOf(true) }
                            IconButton(
                                enabled = enabled.value,
                                onClick = { deleteItem(activity, it, enabled) },
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                    }
                    item { Box(Modifier.padding(bottom = 100.dp)) }
                }
            } else {
                refreshed!!.value = true
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.loading))
                Box(Modifier.padding(start = 90.dp, end = 90.dp, top = 10.dp)) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
            LaunchedEffect(loaded) {
                withContext(Dispatchers.IO) {
                    loadApps(activity)
                }
            }
        }
    }
}

private var selectedAppList = arrayListOf<ComparableAppItem>()

@Suppress("DEPRECATION")
private fun loadApps(activity: MainActivity) {
    selectedAppList.clear()
    val packageNameMap = hashMapOf<String, PackageInfo>()
    val pm = activity.packageManager
    pm.getInstalledPackages(0).forEach { packageNameMap[it.packageName] = it }
    MainActivity.config.packages.forEach {
        val name: String
        val icon: Bitmap?
        val pkg = packageNameMap[it]
        if (pkg == null) {
            name = it
            icon = null
        } else {
            name = pkg.applicationInfo.loadLabel(pm).toString()
            icon = drawableToBitmap(pkg.applicationInfo.loadIcon(pm))
        }
        selectedAppList.add(ComparableAppItem(icon, name, it))
    }
    selectedAppList.sort()
    packageNameMap.clear()
    loaded!!.value = true
}

private fun deleteItem(
    activity: MainActivity,
    item: ComparableAppItem,
    buttonEnabled: MutableState<Boolean>,
) {
    activity.createConfirmDialog(
        title = activity.getString(R.string.delete),
        message = activity.getString(R.string.delete_confirm),
        positiveButtonText = activity.getString(R.string.confirm_yes),
        negativeButtonText = activity.getString(R.string.confirm_no),
    ) { dialog, _ ->
        buttonEnabled.value = false
        item.delete()
        requireRefresh()
        MainActivity.saveConfig {
            selectedAppList.remove(item)
            delay(100)
            refreshed!!.value = false
        }
        dialog.cancel()
    }.show()
}

internal open class ComparableAppItem(
    val icon: Bitmap?,
    val name: String,
    val packageName: String,
) : Comparable<ComparableAppItem> {
    private val nameToSort = name.lowercase()

    override fun compareTo(other: ComparableAppItem): Int {
        return nameToSort.compareTo(other.nameToSort)
    }

    fun add() {
        MainActivity.config.packages.add(packageName)
        Log.i("T-AppSelected", "Added $packageName ($name)")
    }

    fun delete() {
        MainActivity.config.packages.remove(packageName)
        Log.i("T-AppSelected", "Removed $packageName ($name)")
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }
    val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
