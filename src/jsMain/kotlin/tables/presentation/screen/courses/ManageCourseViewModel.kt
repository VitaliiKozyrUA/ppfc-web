/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.courses

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import tables.presentation.screen.courses.model.CourseState

class ManageCourseViewModel {

    private val _courseState = MutableStateFlow(CourseState.Empty)
    private val _isFormBlank = _courseState.map { courseState ->
        courseState.number.number == null
    }

    val state: StateFlow<ManageCourseViewState> = combine(
        _courseState,
        _isFormBlank
    ) { courseState, isFormBlank ->
        ManageCourseViewState(
            courseState = courseState,
            isFormBlank = isFormBlank
        )
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = ManageCourseViewState.Empty,
    )

    fun loadCourseState(courseState: CourseState) {
        _courseState.value = courseState
    }

    fun setNumber(number: Long?) {
        _courseState.update {
            it.copy(
                number = it.number.copy(
                    number = number,
                    error = null
                ),
            )
        }
    }
}