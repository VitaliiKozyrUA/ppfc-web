/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.changes.mapper

import coreui.model.TextFieldState
import tables.domain.model.*
import tables.presentation.compose.PagingDropDownMenuState
import tables.presentation.screen.changes.model.ChangeLessonState
import tables.presentation.screen.changes.model.ChangeState
import kotlin.js.Date

fun ChangeLessonState.toDomain(
    date: Date,
    dayNumber: DayNumber,
    weekAlternation: WeekAlternation,
    group: Group
) = Change(
    group = group,
    classroom = classroom.selectedItem ?: Classroom.Empty,
    teacher = teacher.selectedItem ?: Teacher.Empty,
    subject = subject.selectedItem ?: Subject.Empty,
    eventName = eventName.text,
    lessonNumber = lessonNumber.toDomain(),
    dayNumber = dayNumber,
    date = date,
    weekAlternation = weekAlternation
)

fun ChangeState.toDomain() = Change(
    id = id,
    group = group.selectedItem ?: Group.Empty,
    classroom = classroom.selectedItem ?: Classroom.Empty,
    teacher = teacher.selectedItem ?: Teacher.Empty,
    subject = subject.selectedItem ?: Subject.Empty,
    eventName = eventName.text,
    lessonNumber = lessonNumber.toDomain(),
    date = date,
    weekAlternation = weekAlternation
)

fun Change.toState() = ChangeState(
    id = id,
    group = PagingDropDownMenuState.Empty<Group>()
        .copy(selectedItem = group.takeIf { it != Group.Empty }),
    classroom = PagingDropDownMenuState.Empty<Classroom>()
        .copy(selectedItem = classroom.takeIf { it != Classroom.Empty }),
    teacher = PagingDropDownMenuState.Empty<Teacher>()
        .copy(selectedItem = teacher.takeIf { it != Teacher.Empty }),
    subject = PagingDropDownMenuState.Empty<Subject>()
        .copy(selectedItem = subject.takeIf { it != Subject.Empty }),
    eventName = TextFieldState.Empty.copy(text = eventName ?: ""),
    lessonNumber = lessonNumber.toChangeState(),
    date = date,
    weekAlternation = weekAlternation
)