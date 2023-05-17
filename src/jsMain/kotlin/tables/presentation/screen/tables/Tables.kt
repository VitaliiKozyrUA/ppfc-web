package tables.presentation.screen.tables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coreui.compose.base.Box
import coreui.compose.base.Column
import coreui.util.rememberNavController
import org.jetbrains.compose.web.css.*
import org.koin.compose.getKoin
import tables.presentation.compose.TableTopBar
import tables.presentation.navigation.TablesNavHost
import tables.presentation.navigation.TablesScreen

@Composable
fun Tables() {
    val viewModel: TablesViewModel = getKoin().get()
    val viewState by viewModel.state.collectAsState()
    val navController by rememberNavController(root = TablesScreen.Changes)

    Box(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
            }
        }
    ) {
        Column(
            attrs = {
                style {
                    width(100.percent)
                    paddingLeft(16.px)
                    paddingRight(16.px)
                }
            }
        ) {
            TableTopBar(
                attrs = {
                    style {
                        width(100.percent)
                    }
                },
                selectedScreen = navController.currentScreen.value,
                colorSchemeMode = viewState.preferences.colorSchemeMode,
                onScreenSelected = { screen ->
                    navController.navigate(screen = screen)
                },
                onColorSchemeModeChanged = { colorSchemeMode ->
                    viewModel.setColorSchemeMode(colorSchemeMode = colorSchemeMode)
                },
                onLogOut = {
                    viewModel.logOut()
                }
            )

            TablesNavHost(navController = navController)
        }
    }
}