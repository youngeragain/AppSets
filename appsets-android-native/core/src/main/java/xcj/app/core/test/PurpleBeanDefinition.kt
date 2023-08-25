package xcj.app.core.test

import xcj.app.core.test.annotations.BeanMetadata

class PurpleBeanDefinition{
    lateinit var customKClassName: String
    lateinit var customKClass: Class<*>
    lateinit var metadata: BeanMetadata
}