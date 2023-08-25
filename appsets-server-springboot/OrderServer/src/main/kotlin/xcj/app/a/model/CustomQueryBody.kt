package xcj.app.a.model

data class CustomQueryBody(
    val queryString: String?)
{
    constructor() : this(null)
}