package xcj.app.a

import ch.qos.logback.core.util.ExecutorServiceUtil
import org.jboss.logging.Logger
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Component
import xcj.app.a.config.DynamicDataSourceRegister
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService


@Import(DynamicDataSourceRegister::class)
@EnableJpaRepositories
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class OrderApplication

fun main() {
    runApplication<OrderApplication>()
}

class MyApplicationLoadedListener: SpringApplicationRunListener{

}

@Component
class MyApplicationPreparedEventListener: ApplicationListener<ApplicationPreparedEvent>{

    override fun onApplicationEvent(event: ApplicationPreparedEvent) {
        val executorService:ExecutorService = ExecutorServiceUtil.newExecutorService()
        MyTextServerSocketManager.startAccept(executorService)
    }

}

class MyTextServerSocketManager(private val executorService: ExecutorService):Runnable{
    private val log:Logger = Logger.getLogger(MyTextServerSocketManager::class.java)
    var port:Int = 12000
    private val clientSockets = mutableMapOf<String, MyTextSocketCommunicator>()
    lateinit var serverSocket: ServerSocket
    override fun run() {
        if(!::serverSocket.isInitialized)
            serverSocket = ServerSocket(port)
        do{
            log.debug("accept waiting!")
            val clientSocket = serverSocket.accept()
            val myTextSocketCommunicator = MyTextSocketCommunicator(clientSocket).also {
                it.start = true
            }
            clientSockets["${clientSocket.inetAddress.hostAddress}:${clientSocket.port}"] = myTextSocketCommunicator
            log.debug("accept one connection, current all connection is:${clientSockets.map { it.key }}")
            executorService.execute(myTextSocketCommunicator)
        }while (true)
    }
    companion object{
        private var Instance:MyTextServerSocketManager? = null
        fun startAccept(executorService: ExecutorService):MyTextServerSocketManager {
            val myTextServerSocketManager = Instance?: synchronized(this) {
                MyTextServerSocketManager(executorService).also {
                    Instance = it
                }
            }
            executorService.execute(myTextServerSocketManager)
            return myTextServerSocketManager
        }
    }
}
class MyTextSocketCommunicator(private val socket: Socket):Runnable{
    private val log:Logger = Logger.getLogger(MyTextSocketCommunicator::class.java)
    var start = false
    private val inPutStream:BufferedInputStream = socket.getInputStream().buffered()
    private val outPutStream:BufferedOutputStream = socket.getOutputStream().buffered()
    override fun run() {
        log.debug("start read from $socket")
        val sb = StringBuilder()
        val bytes = ByteArray(1024)
        var len: Int
        do {
            sb.clear()
            while (inPutStream.read(bytes).also { len = it } != -1) {
                //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                sb.append(String(bytes, 0, len, Charset.defaultCharset()))
            }
            log.debug("${socket.inetAddress.hostAddress}:${socket.port}:$sb")
        }while (start)
    }
    fun send(any:String){
        if(start){
            outPutStream.write(any.toByteArray())
            outPutStream.flush()
        }
    }
}
