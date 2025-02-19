package xcj.app.server.im.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long?,
    var to_:String?=null,
    var content:String?=null,
    val payload:String?=null,
    val type_: Int?=null,
    @Transient
    val routingKey:String?=null,) {
    constructor() : this(null, null, null, null, null, null)
}