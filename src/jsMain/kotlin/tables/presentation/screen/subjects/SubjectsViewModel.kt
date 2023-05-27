/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.subjects

import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.map
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
import tables.domain.interactor.DeleteSubjects
import tables.domain.interactor.SaveSubject
import tables.domain.model.Id
import tables.domain.observer.ObservePagedSubjects
import tables.presentation.screen.subjects.mapper.toDomain
import tables.presentation.screen.subjects.mapper.toState
import tables.presentation.screen.subjects.model.SubjectState

@OptIn(FlowPreview::class)
class SubjectsViewModel(
    private val observePagedSubjects: ObservePagedSubjects,
    private val saveSubject: SaveSubject,
    private val deleteSubjects: DeleteSubjects,
    private val apiCommonErrorMapper: ApiCommonErrorMapper
) {

    private val loadingState = ObservableLoadingCounter()
    private val savingLoadingState = ObservableLoadingCounter()
    private val deletingLoadingState = ObservableLoadingCounter()
    private val uiEventManager = UiEventManager<SubjectsViewEvent>()
    private val _dialog = MutableStateFlow<SubjectsDialog?>(null)
    private val _searchQuery = MutableStateFlow(TextFieldState.Empty)
    private val _rowsSelection = MutableStateFlow(mapOf<Id, Boolean>())

    val pagedSubjects: Flow<PagingData<SubjectState>> =
        observePagedSubjects.flow.onEach {
            _rowsSelection.value = emptyMap()
        }.map { pagingData ->
            pagingData.map {
                it.toState()
            }
        }.cachedIn(CoroutineScope(Dispatchers.Default))

    val state: StateFlow<SubjectsViewState> = combine(
        _searchQuery,
        _rowsSelection,
        loadingState.observable,
        savingLoadingState.observable,
        deletingLoadingState.observable,
        _dialog,
        uiEventManager.event
    ) { searchQuery, rowsSelection, isLoading, isSaving, isDeleting, dialog, event ->
        SubjectsViewState(
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
        initialValue = SubjectsViewState.Empty,
    )

    init {
        _searchQuery.debounce(100).onEach { searchQuery ->
            observePagedSubjects(searchQuery = searchQuery.text)
        }.launchIn(CoroutineScope(Dispatchers.Default))
    }

    private fun observePagedSubjects(
        searchQuery: String = ""
    ) {
        observePagedSubjects(
            params = ObservePagedSubjects.Params(
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
            event = SubjectsViewEvent.Message(
                message = UiMessage(message = message)
            )
        )
    }

    fun saveSubject(subjectState: SubjectState) = launchWithLoader(savingLoadingState) {
        val subject = subjectState.toDomain().let {
            it.copy(
                name = it.name.trim()
            )
        }

        saveSubject(
            params = SaveSubject.Params(
                subject = subject
            )
        ).onSuccess {
            sendEvent(
                event = SubjectsViewEvent.SubjectSaved
            )
        }.withErrorMapper(
            defaultMessage = AppTheme.stringResources.unexpectedErrorException,
            errorMapper = apiCommonErrorMapper
        ) { message ->
            sendEvent(
                event = SubjectsViewEvent.Message(
                    message = UiMessage(message = message)
                )
            )
        }.collect()
    }

    fun deleteSubjects() = launchWithLoader(deletingLoadingState) {
        val idsToDelete = _rowsSelection.value.filter { it.value }.map { it.key }.toSet()

        deleteSubjects(
            params = DeleteSubjects.Params(ids = idsToDelete)
        ).onSuccess {
            sendEvent(
                event = SubjectsViewEvent.SubjectDeleted
            )
        }.withErrorMapper(
            defaultMessage = AppTheme.stringResources.unexpectedErrorException,
            errorMapper = apiCommonErrorMapper
        ) { message ->
            sendEvent(
                event = SubjectsViewEvent.Message(
                    message = UiMessage(message = message)
                )
            )
        }.collect()
    }

    private fun sendEvent(event: SubjectsViewEvent) {
        uiEventManager.emitEvent(
            event = UiEvent(
                event = event
            )
        )
    }

    fun dialog(dialog: SubjectsDialog?) {
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