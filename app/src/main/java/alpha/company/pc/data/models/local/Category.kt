package alpha.company.pc.data.models.local

data class Category(
    val title: String,
    var isClicked: Boolean = false
){
    fun reverseClicked(){
        isClicked = !isClicked
    }
}