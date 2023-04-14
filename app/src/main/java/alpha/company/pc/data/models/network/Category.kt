package alpha.company.pc.data.models.network

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("category")
    val category: String,

    @SerializedName("subCategories")
    val subcategories: List<String>
)
