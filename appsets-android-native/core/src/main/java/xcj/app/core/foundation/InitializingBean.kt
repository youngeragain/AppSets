package xcj.app.core.foundation

interface InitializingBean {
    @Throws fun afterPropertiesSet()
}