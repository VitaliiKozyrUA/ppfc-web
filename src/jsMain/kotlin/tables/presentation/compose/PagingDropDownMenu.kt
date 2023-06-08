/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.compose

import androidx.compose.runtime.*
import app.cash.paging.LoadStateLoading
import coreui.compose.*
import coreui.compose.base.Alignment
import coreui.compose.base.Column
import coreui.compose.base.Row
import coreui.extensions.elementContext
import coreui.theme.AppIconClass
import coreui.util.LazyPagingItems
import coreui.util.ScrollState
import coreui.util.scrollStateListener
import kotlinx.browser.document
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

@Composable
fun <T : Any> PagingDropDownMenu(
    state: PagingDropDownMenuState<T>,
    lazyPagingItems: LazyPagingItems<T>,
    label: String,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    itemLabel: (item: T) -> String,
    onStateChanged: (state: PagingDropDownMenuState<T>) -> Unit
) {
    var localState by remember { mutableStateOf(state) }
    var menuElement by remember { mutableStateOf<Element?>(null) }
    val itemsNumber = lazyPagingItems.itemCount
    val isAppending = lazyPagingItems.loadState.append == LoadStateLoading

    LaunchedEffect(localState) {
        onStateChanged(localState)
    }

    LaunchedEffect(state.scrollState == ScrollState.BOTTOM) {
        try {
            lazyPagingItems[(itemsNumber - 1).coerceAtLeast(0)]
        } catch (_: IndexOutOfBoundsException) {
        }
    }

    LaunchedEffect(isAppending) {
        localState = state.copy(
            isLoading = isAppending
        )
    }

    Column(
        attrs = {
            style {
                position(Position.Relative)
                display(DisplayStyle.InlineBlock)
                overflowY(Overflow.Visible)
                zIndex(1)
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            OutlinedTextField(
                attrs = {
                    elementContext { element ->
                        document.addEventListener(
                            type = "click",
                            callback = { event ->
                                val clickedOutside = !element.contains(event.target.asDynamic() as? Node)
                                if (clickedOutside) {
                                    localState = localState.copy(
                                        isExpanded = false
                                    )
                                }
                            }
                        )
                    }

                    onFocusIn {
                        localState = localState.copy(
                            isExpanded = true
                        )
                    }

                    applyAttrs(attrs)
                },
                value = state.selectedItem?.let { item ->
                    itemLabel(item)
                } ?: state.searchQuery,
                label = label,
                error = state.error,
                trailingIcon = if (localState.selectedItem != null) {
                    AppIconClass.Cancel
                } else null,
                onTrailingIconClick = {
                    localState = localState.copy(
                        selectedItem = null,
                        searchQuery = "",
                        isExpanded = false
                    )
                },
                onValueChange = { text ->
                    menuElement?.scroll(x = 0.0, y = 0.0)

                    localState = localState.copy(
                        selectedItem = null,
                        searchQuery = text
                    )
                }
            )
        }

        if (!localState.isExpanded) return@Column

        Menu(
            attrs = {
                elementContext { element ->
                    menuElement = element

                    element.scrollStateListener { scrollState ->
                        localState = localState.copy(
                            scrollState = scrollState
                        )
                    }
                }
            },
            isLoading = state.isLoading,
            values = lazyPagingItems.itemSnapshotList.items.associateWith { item -> itemLabel(item) }
        ) { item ->
            localState = localState.copy(
                selectedItem = item,
                isExpanded = false
            )
        }
    }
}