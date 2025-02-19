package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.req.GenerationAIContext

interface GenerationAIContentService {

    fun generateContentWithNoneContext(): DesignResponse<String>

    fun generateContentWithContext(context: GenerationAIContext): DesignResponse<GenerationAIContent>

}