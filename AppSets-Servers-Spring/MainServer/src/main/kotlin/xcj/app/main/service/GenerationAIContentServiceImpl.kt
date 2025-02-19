package xcj.app.main.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.main.model.req.GenerationAIContext
import java.util.*

data class GenerationAIContent(val contextKey: String?, val content: String)

@Service
class GenerationAIContentServiceImpl(
    private val stringRedisTemplate: StringRedisTemplate
) : GenerationAIContentService {

    override fun generateContentWithNoneContext(): DesignResponse<String> {
        return DesignResponse(data = "今天天气很好呢，傍晚有空的话要不要一起到河边散步呀？")
    }

    override fun generateContentWithContext(
        context: GenerationAIContext
    ): DesignResponse<GenerationAIContent> {
        return DesignResponse(
            data = GenerationAIContent(
                contextKey = UUID.randomUUID().toString(),
                content = "今天天气很好呢，傍晚有空的话要不要一起到河边散步呀？"
            )
        )
    }
}