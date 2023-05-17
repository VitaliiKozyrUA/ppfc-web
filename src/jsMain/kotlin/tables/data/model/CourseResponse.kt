package tables.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CourseResponse(
    val id: Long,
    val number: Long
)