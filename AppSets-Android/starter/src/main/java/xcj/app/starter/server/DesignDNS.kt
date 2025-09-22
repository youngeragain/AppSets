package xcj.app.starter.server

import xcj.app.starter.android.util.PurpleLogger
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private class DesignDNS : okhttp3.Dns, Runnable {

    companion object {
        private const val TAG = "DesignDNS"
    }

    private var hostname: String? = null
    private var results: String? = null
    private val mPingExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun run() {
        PurpleLogger.current.d(TAG, "run ping host for host:$hostname")
        if (hostname.isNullOrEmpty()) {
            results = "Unreachable"
            return
        }
        val process = Runtime.getRuntime().exec("ping $hostname")
        val ins = process.inputStream.bufferedReader()
        ins.readLine()//first line is cmd
        results = ins.readLine()
    }

    override fun lookup(hostname: String): List<InetAddress> {

        this.hostname = hostname
        mPingExecutor.execute(this)
        val startTimeMills = System.currentTimeMillis()
        while (true) {
            if (results?.contains("Unreachable") == true) {
                PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} unreachable!")
                return emptyList()
            }
            if (results.isNullOrEmpty()) {
                val interval = System.currentTimeMillis() - startTimeMills
                if (interval > 1000) {
                    PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} unreachable!")
                    return emptyList()
                } else {
                    continue
                }
            }
            break
        }
        PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} get by default!")
        return InetAddress.getAllByName(hostname).toList()
    }
}