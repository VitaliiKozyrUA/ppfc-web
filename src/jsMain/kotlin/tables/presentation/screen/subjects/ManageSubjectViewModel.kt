/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.subjects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import tables.presentation.screen.subjects.model.SubjectState

class ManageSubjectViewModel {

    private val _subjectState = MutableStateFlow(SubjectState.Empty)
    private val _isFormBlank = _subjectState.map { subjectState ->
        subjectState.name.text.isBlank()
    }

    val state: StateFlow<ManageSubjectViewState> = combine(
        _subjectState,
        _isFormBlank
    ) { subjectState, isFormBlank ->
        ManageSubjectViewState(
            subjectState = subjectState,
            isFormBlank = isFormBlank
        )
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = ManageSubjectViewState.Empty,
    )

    fun loadSubjectState(subjectState: SubjectState) {
        _subjectState.value = subjectState
    }

    fun setName(name: String) {
        _subjectState.update {
            it.copy(
                name = it.name.copy(
                    text = name,
                    error = null
                ),
            )
        }
    }
}