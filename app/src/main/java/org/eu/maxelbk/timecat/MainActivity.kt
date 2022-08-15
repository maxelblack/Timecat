package org.eu.maxelbk.timecat

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eu.maxelbk.timecat.page.*
import org.eu.maxelbk.timecat.ui.theme.TimecatTheme
import org.eu.maxelbk.timecat.ui.theme.colorScheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!configLoaded) {
            loadConfig()
            configLoaded = true
        }
        setContent {
            TimecatTheme {
                Main()
            }
        }
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class)
    fun Main() {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            val nav = rememberAnimatedNavController()
            AnimatedNavHost(navController = nav, startDestination = "main") {
                composable("main") { MainPage(nav, this@MainActivity) }

                val settingsDurationMills = 500
                composable(
                    route = "settings",
                    enterTransition = {
                        when (initialState.destination.route) {
                            "main" ->
                                slideIntoContainer(
                                    AnimatedContentScope.SlideDirection.Left,
                                    animationSpec = tween(settingsDurationMills))
                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            "main" ->
                                slideOutOfContainer(
                                    AnimatedContentScope.SlideDirection.Left,
                                    animationSpec = tween(settingsDurationMills))
                            else -> null
                        }
                    },
                    popEnterTransition = {
                        when (initialState.destination.route) {
                            "main" ->
                                slideIntoContainer(AnimatedContentScope.SlideDirection.Right,
                                    animationSpec = tween(settingsDurationMills))
                            else -> null
                        }
                    },
                    popExitTransition = {
                        when (targetState.destination.route) {
                            "main" ->
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right,
                                    animationSpec = tween(settingsDurationMills))
                            else -> null
                        }
                    }
                ) { SettingsPage(nav) }

                val appListDurationMills = 700
                composable(
                    route = "app-list",
                    enterTransition = {
                        when (initialState.destination.route) {
                            "main" ->
                                slideIntoContainer(
                                    AnimatedContentScope.SlideDirection.Up,
                                    animationSpec = tween(appListDurationMills))
                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            "main" ->
                                slideOutOfContainer(
                                    AnimatedContentScope.SlideDirection.Up,
                                    animationSpec = tween(appListDurationMills))
                            else -> null
                        }
                    },
                    popEnterTransition = {
                        when (initialState.destination.route) {
                            "main" ->
                                slideIntoContainer(AnimatedContentScope.SlideDirection.Down,
                                    animationSpec = tween(appListDurationMills))
                            else -> null
                        }
                    },
                    popExitTransition = {
                        when (targetState.destination.route) {
                            "main" ->
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Down,
                                    animationSpec = tween(appListDurationMills))
                            else -> null
                        }
                    }
                ) { AppListPage(nav, this@MainActivity) }
            }
        }
    }

    internal fun createConfirmDialog(
        title: String? = null,
        message: String,
        cancelable: Boolean = true,
        positiveButtonText: String = getString(android.R.string.ok),
        negativeButtonText: String? = getString(android.R.string.cancel),
        neutralButtonText: String? = null,
        negativeEvent: ((dialog: DialogInterface, id: Int) -> Unit) = { d, _ -> d.cancel() },
        neutralEvent: ((dialog: DialogInterface, id: Int) -> Unit)? = null,
        positiveEvent: (dialog: DialogInterface, id: Int) -> Unit,
    ): AlertDialog
    = this.let {
        val builder = MaterialAlertDialogBuilder(it).apply {
            if (title != null) setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonText,
                DialogInterface.OnClickListener(positiveEvent))
            if (negativeButtonText != null) {
                setNegativeButton(negativeButtonText,
                    DialogInterface.OnClickListener(negativeEvent))
            }
            if (neutralButtonText != null) {
                if (neutralEvent == null) {
                    setNeutralButton(neutralButtonText) { _, _ -> }
                } else {
                    setNeutralButton(neutralButtonText,
                        DialogInterface.OnClickListener(neutralEvent))
                }
            }
            setCancelable(cancelable)
        }
        builder.create()
    }

    // --- config ---

    companion object {
        private lateinit var configJson: String

        private var configLoaded = false

        private lateinit var config0: AppConfig
        internal val config get() = config0

        internal fun saveConfig(runAfter: suspend () -> Unit = {}) {
            CoroutineScope(Dispatchers.IO).launch {
                saveConfig0()
                runAfter()
            }
        }

        private fun saveConfig0() {
            synchronized(config0) {
                Log.i("T-AppConfig", "Start saving config")
                val configJsonContent = Json.encodeToString(config)
                Log.d("T-AppConfig", "Generated JSON content: $configJsonContent")
                val output = FileOutputStream(configJson)
                val string = StringReader(configJsonContent)
                var i = string.read()
                while (i != -1) {
                    output.write(i)
                    i = string.read()
                }
                output.close()
                Log.i("T-AppConfig", "Config saved")
            }
        }
    }

    @Serializable
    internal data class AppConfig(
        var enabled: Boolean = true,
        val packages: HashSet<String> = hashSetOf(),
    )

    private fun loadConfig() {
        configJson = filesDir.path + "config.json"
        Log.i("T-AppConfig", "Start loading config")
        val file = File(configJson)
        if (file.exists()) {
            val buffer = StringBuffer()
            val input = FileInputStream(file)
            var i = input.read()
            while (i != -1) {
                buffer.append(i.toChar())
                i = input.read()
            }
            input.close()
            val configJsonContent = buffer.toString()
            Log.d("T-AppConfig", "Read JSON content: $configJsonContent")
            config0 = Json.decodeFromString(configJsonContent)
        } else {
            Log.i("T-AppConfig", "Config file not found, use defaults")
            config0 = AppConfig()
        }
        Log.i("T-AppConfig", "Config loaded")
    }
}
