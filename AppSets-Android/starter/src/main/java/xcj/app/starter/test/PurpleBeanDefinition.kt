package xcj.app.starter.test

import xcj.app.starter.test.annotations.BeanMetadata

class PurpleBeanDefinition {
    lateinit var customKClassName: String
    lateinit var customKClass: Class<*>
    lateinit var metadata: BeanMetadata
}