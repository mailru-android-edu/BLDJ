package data

data class User(
    val email: String = "",
    var name: String = "Даниил Можайский",
    var group: String = "Бакалавриат группа 205-2019",
    var id: String = "",
    var myAdvert: Advert = Advert()
) {
    var isTraveller: Boolean = false

    override fun equals(other: Any?): Boolean {
        return id == (other as User).id
    }
}