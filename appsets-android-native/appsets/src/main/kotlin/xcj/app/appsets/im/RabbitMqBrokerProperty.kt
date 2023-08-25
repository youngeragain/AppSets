package xcj.app.appsets.im

data class RabbitMqBrokerProperty(
    val `rabbit-host`: String,
    val `rabbit-port`: Int,
    val `rabbit-admin-username`: String,
    val `rabbit-admin-password`: String,
    val `rabbit-virtual-host`: String,
    val `queue-prefix`: String,
    val `routing-key-prefix`: String,
    val `user-exchange-main`: String,
    val `user-exchange-main-parent`: String,
    val `user-exchange-group-prefix`: String,
) {
    lateinit var uid: String
    var `user-exchange-groups`: String? = null
}