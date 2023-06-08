/*
 * Copyright (c) 2023. Vitalii Kozyr
 */

package tables.presentation.screen.schedule.mapper

import coreui.model.TextFieldState
import tables.domain.model.*
import tables.presentation.compose.PagingDropDownMenuState
import tables.presentation.screen.schedule.model.ScheduleItemState

fun ScheduleItemState.toDomain() = ScheduleItem(
    id = id,
    group = group.selectedItem ?: Group.Empty,
    classroom = classroom.selectedItem ?: Classroom.Empty,
    teacher = teacher.selectedItem ?: Teacher.Empty,
    subject = subject.selectedItem ?: Subject.Empty,
    eventName = eventName.text,
    isSubject = isSubject,
    lessonNumber = lessonNumber,
    dayNumber = dayNumber,
    weekAlternation = weekAlternation
)

fun ScheduleItem.toState() = ScheduleItemState(
    id = id,
    group = PagingDropDownMenuState.Empty<Group>().copy(selectedItem = group),
    classroom = PagingDropDownMenuState.Empty<Classroom>().copy(selectedItem = classroom),
    teacher = PagingDropDownMenuState.Empty<Teacher>().copy(selectedItem = teacher),
    subject = PagingDropDownMenuState.Empty<Subject>().copy(selectedItem = subject),
    eventName = TextFieldState.Empty.copy(text = eventName ?: ""),
    isSubject = isSubject,
    lessonNumber = lessonNumber,
    dayNumber = dayNumber,
    weekAlternation = weekAlternation
)