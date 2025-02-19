package xcj.app.file_server.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import xcj.app.DesignResponse
import xcj.app.util.PurpleLogger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID


@RequestMapping("/appsets/share")
@Controller
class FileController() {
    companion object {
        private const val TAG = "FileController"
    }

    private fun getFileDir(): String {
        return ""
    }


    @ResponseBody
    @RequestMapping("/file")
    fun postFile(@RequestParam("file") file: MultipartFile): DesignResponse<Boolean> {
        PurpleLogger.current.d(TAG, "file upload..., name:${file.originalFilename}")
        if (file.isEmpty) {
            return DesignResponse(data = false)
        }
        try {
            GlobalScope.launch(Dispatchers.IO) {
                // 创建存储目录
                val uploadPath: Path = Paths.get(getFileDir())
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath)
                }
                // 获取文件名
                val fileName: String = file.originalFilename ?: UUID.randomUUID().toString()
                // 构建文件存储路径
                val filePath = uploadPath.resolve(fileName)
                // 将文件保存到指定路径
                Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            return DesignResponse(data = true)
        } catch (e: IOException) {
            e.printStackTrace()
            return DesignResponse(data = false)
        }
    }

    @ResponseBody
    @RequestMapping("/ping")
    fun ping(): DesignResponse<String> {
        PurpleLogger.current.d(TAG, "ping")
        return DesignResponse(data = "pong")
    }

    @ResponseBody
    @RequestMapping("/pin/isneed")
    fun isNeedPin(): DesignResponse<Boolean> {
        PurpleLogger.current.d(TAG, "isNeedPin")
        return DesignResponse(data = false)
    }

    @ResponseBody
    @RequestMapping("/send/prepare")
    fun prepareSend(): DesignResponse<Boolean> {
        PurpleLogger.current.d(TAG, "prepareSend")
        return DesignResponse(data = false)
    }
}