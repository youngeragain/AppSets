package xcj.app.a.config

import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "server")
@Configuration
class HttpConfig {
    var port:Int=0
    var httpPort:Int=0
    @Bean
    fun servletContainer(httpConnector: Connector): TomcatServletWebServerFactory? {
        val tomcat: TomcatServletWebServerFactory = object : TomcatServletWebServerFactory() {
            override fun postProcessContext(context: Context) {
                val constraint = SecurityConstraint()
                constraint.userConstraint = "CONFIDENTIAL"
                val collection = SecurityCollection()
                collection.addPattern("/*")
                constraint.addCollection(collection)
                context.addConstraint(constraint)
            }
        }
        tomcat.addAdditionalTomcatConnectors(httpConnector)
        return tomcat
    }

    @Bean
    fun httpConnector(): Connector {
        val connector = Connector(org.apache.coyote.http11.Http11NioProtocol::class.java.canonicalName)
        connector.scheme = "http"
        //Connector监听的http的端口号
        connector.port = httpPort
        connector.secure = false
        //监听到http的端口号后转向到的https的端口号
        connector.redirectPort = port
        return connector
    }
}