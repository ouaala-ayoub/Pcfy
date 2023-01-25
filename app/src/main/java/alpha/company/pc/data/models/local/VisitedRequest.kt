package alpha.company.pc.data.models.local

import com.google.gson.annotations.SerializedName

data class VisitedRequest(
    @SerializedName("visited")
    val visited: Int
)
