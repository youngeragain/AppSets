package xcj.app.starter.foundation

interface InitializingBean {
    @Throws fun afterPropertiesSet()
}