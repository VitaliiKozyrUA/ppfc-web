/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package onboarding.presentation.screen.changepassword

import androidx.compose.runtime.*
import coreui.compose.*
import coreui.compose.base.*
import coreui.theme.AppTheme
import coreui.theme.Typography
import coreui.util.CollectUiEvents
import coreui.util.UiMessage
import coreui.util.rememberGet
import org.jetbrains.compose.web.css.*

@Composable
fun ChangePassword() {
    val viewModel: ChangePasswordViewModel by rememberGet()
    val viewState by viewModel.state.collectAsState()
    var uiMessage by remember { mutableStateOf<UiMessage?>(null) }

    CollectUiEvents(
        event = viewState.event,
        onEvent = { event ->
            when (event) {
                is ChangePasswordViewEvent.Message -> uiMessage = event.message
            }
        },
        onClear = { id ->
            viewModel.clearEvent(id = id)
        }
    )

    UiMessageHost(message = uiMessage)

    Box(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
                backgroundColor(AppTheme.colors.background)
            }
        },
        contentAlignment = Alignment.Box.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Surface(
                attrs = {
                    style {
                        width(250.px)
                        padding(20.px)
                    }
                },
                shadowElevation = ShadowElevation.Level3,
                tonalElevation = TonalElevation.Level3
            ) {
                Column(
                    attrs = {
                        style {
                            width(100.percent)
                        }
                    },
                ) {
                    var isPasswordVisible by remember { mutableStateOf(false) }

                    Text(
                        text = AppTheme.stringResources.changePasswordTitle,
                        fontSize = Typography.headlineSmall
                    )

                    Spacer(height = 10.px)

                    OutlinedTextField(
                        attrs = {
                            style {
                                width(100.percent)
                            }
                        },
                        textFieldType = if(isPasswordVisible) TextFieldType.TEXT else TextFieldType.PASSWORD,
                        value = viewState.password.text,
                        error = viewState.password.error,
                        label = AppTheme.stringResources.changePasswordPasswordFieldLabel
                    ) { text ->
                        viewModel.setPassword(
                            password = viewState.password.copy(text = text)
                        )
                    }

                    Spacer(height = 10.px)

                    Row(
                        attrs = {
                            style {
                                width(100.percent)
                            }
                        },
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalArrangement = Arrangement.Horizontal.Start
                    ) {
                        Checkbox(isPasswordVisible) {
                            isPasswordVisible = !isPasswordVisible
                        }

                        Spacer(width = 5.px)

                        Text(text = "Показати пароль")
                    }

                    Spacer(height = 18.px)

                    ButtonWithLoader(
                        attrs = {
                            style {
                                width(100.percent)
                            }
                        },
                        enabled = !(viewState.isFormBlank || viewState.isLoading),
                        loader = viewState.isLoading,
                        onClick = {
                            viewModel.changePassword()
                        }
                    ) {
                        Text(
                            text = AppTheme.stringResources.changePasswordChangePasswordButton
                        )
                    }
                }
            }

            Spacer(height = 10.px)

            Row {
                TextLink(text = AppTheme.stringResources.changePasswordLogIn + " ") {
                    viewModel.navigateToLoginScreen()
                }

                Text(
                    text = AppTheme.stringResources.changePasswordInAccount,
                    color = AppTheme.colors.onBackground
                )
            }
        }
    }
}