/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.classrooms

import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import core.extensions.combine
import coreui.common.ApiCommonErrorMapper
import coreui.extensions.onSuccess
import coreui.extensions.withErrorMapper
import coreui.model.TextFieldState
import coreui.theme.AppTheme
import coreui.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import tables.domain.interactor.DeleteClassrooms
import tables.domain.interactor.SaveClassroom
import tables.domain.model.Classroom
import tables.domain.model.Id
import tables.domain.observer.ObservePagedClassrooms
import tables.presentation.screen.classrooms.mapper.toDomain
import tables.presentation.screen.classrooms.model.ClassroomState

@OptIn(FlowPreview::class)
class ClassroomsViewModel(
    private val observePagedClassrooms: ObservePagedClassrooms,
    private val saveClassroom: SaveClassroom,
    private val deleteClassrooms: DeleteClassrooms,
    private val apiCommonErrorMapper: ApiCommonErrorMapper
) {

    private val loadingState = ObservableLoadingCounter()
    private val savingLoadingState = ObservableLoadingCounter()
    private val deletingLoadingState = ObservableLoadingCounter()
    private val uiEventManager = UiEventManager<ClassroomsViewEvent>()
    private val _dialog = MutableStateFlow<ClassroomsDialog?>(null)
    private val _searchQuery = MutableStateFlow(TextFieldState.Empty)
    private val _rowsSelection = MutableStateFlow(mapOf<Id, Boolean>())

    val pagedClassrooms: Flow<PagingData<Classroom>> =
        observePagedClassrooms.flow.onEach {
            _rowsSelection.value = emptyMap()
        }.cachedIn(CoroutineScope(Dispatchers.Default))

    val state: StateFlow<ClassroomsViewState> = combine(
        _searchQuery,
        _rowsSelection,
        loadingState.observable,
        savingLoadingState.observable,
        deletingLoadingState.observable,
        _dialog,
        uiEventManager.event
    ) { searchQuery, rowsSelection, isLoading, isSaving, isDeleting, dialog, event ->
        ClassroomsViewState(
            searchQuery = searchQuery,
            rowsSelection = rowsSelection,
            isLoading = isLoading,
            isSaving = isSaving,
            isDeleting = isDeleting,
            dialog = dialog,
            event = event
        )
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = ClassroomsViewState.Empty,
    )

    init {
        _searchQuery.debounce(100).onEach { searchQuery ->
            observePagedClassrooms(searchQuery = searchQuery.text)
        }.launchIn(CoroutineScope(Dispatchers.Default))
    }

    private fun observePagedClassrooms(
        searchQuery: String = ""
    ) {
        observePagedClassrooms(
            params = ObservePagedClassrooms.Params(
                searchQuery = searchQuery,
                pagingConfig = PAGING_CONFIG
            )
        )
    }

    fun setSearchQuery(searchQuery: String) {
        _searchQuery.update {
            it.copy(text = searchQuery)
        }
    }

    fun setRowSelection(id: Id, isSelected: Boolean) {
        _rowsSelection.update { rowsSelection ->
            rowsSelection.toMutableMap().apply {
                this[id] = isSelected
            }
        }
    }

    fun handlePagingError(cause: Throwable) {
        val message = apiCommonErrorMapper.map(cause = cause)
            ?: AppTheme.stringResources.unexpectedErrorException

        sendEvent(
            event = ClassroomsViewEvent.Message(
                message = UiMessage(message = message)
            )
        )
    }

    fun saveClassroom(classroomState: ClassroomState) = launchWithLoader(savingLoadingState) {
        val classroom = classroomState.toDomain().let {
            it.copy(
                name = it.name.trim()
            )
        }

        saveClassroom(
            params = SaveClassroom.Params(
                classroom = classroom
            )
        ).onSuccess {
            sendEvent(
                event = ClassroomsViewEvent.ClassroomSaved
            )
        }.withErrorMapper(
            defaultMessage = AppTheme.stringResources.unexpectedErrorException,
            errorMapper = apiCommonErrorMapper
        ) { message ->
            sendEvent(
                event = ClassroomsViewEvent.Message(
                    message = UiMessage(message = message)
                )
            )
        }.collect()
    }

    fun deleteClassrooms() = launchWithLoader(deletingLoadingState) {
        val idsToDelete = _rowsSelection.value.filter { it.value }.map { it.key }.toSet()

        deleteClassrooms(
            params = DeleteClassrooms.Params(ids = idsToDelete)
        ).onSuccess {
            sendEvent(
                event = ClassroomsViewEvent.ClassroomDeleted
            )
        }.withErrorMapper(
            defaultMessage = AppTheme.stringResources.unexpectedErrorException,
            errorMapper = apiCommonErrorMapper
        ) { message ->
            sendEvent(
                event = ClassroomsViewEvent.Message(
                    message = UiMessage(message = message)
                )
            )
        }.collect()
    }

    private fun sendEvent(event: ClassroomsViewEvent) {
        uiEventManager.emitEvent(
            event = UiEvent(
                event = event
            )
        )
    }

    fun dialog(dialog: ClassroomsDialog?) {
        _dialog.value = dialog
    }

    fun clearEvent(id: Long) {
        uiEventManager.clearEvent(id = id)
    }

    private companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 10,
            prefetchDistance = 20
        )
    }
}