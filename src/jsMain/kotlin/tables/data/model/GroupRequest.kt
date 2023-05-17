package tables.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupRequest(
    val number: Long,
    val courseId: Long
)