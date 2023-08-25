package xcj.app.a.config

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class DynamicDataSourceAspect {
    private val log: Logger = LoggerFactory.getLogger(DynamicDataSourceAspect::class.java)
    @Pointcut("@annotation(ds)")
    fun aroundPoint(){}

    @Around("aroundPoint()")
    fun arount(){
        log.debug("around")
    }

    @Before("@annotation(ds)")
    @Throws(Throwable::class)
    fun changeDataSource(point: JoinPoint, ds: DataSourceUsed) {
        val dsId: Int = ds.value
        if (DynamicDataSourceRegister.instance.containsDataSource(dsId)) {
            DynamicDataSourceRegister.instance.setDataSourceRouterKey(dsId)
        } else {
            DynamicDataSourceRegister.instance.setDefaultDataSourceKey()
        }

    }

    @After("@annotation(ds)")
    fun restoreDataSource(point: JoinPoint, ds: DataSourceUsed) {
        DynamicDataSourceRegister.instance.removeDataSourceRouterKey()
    }
}