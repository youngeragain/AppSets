package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.GenerationAIContext
import xcj.app.main.service.GenerationAIContent
import xcj.app.main.service.GenerationAIContentService

@RequestMapping("/appsets/genai")
@RestController
class GenerationAIController(
    private val generationAIContentService: GenerationAIContentService
) {
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("/content")
    fun generateContentWithNoneContext(): DesignResponse<String> {
        return generationAIContentService.generateContentWithNoneContext()
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @PostMapping("/content")
    fun generateContentWithContext(
        @RequestBody generationAIContext: GenerationAIContext
    ): DesignResponse<GenerationAIContent> {
        return generationAIContentService.generateContentWithContext(generationAIContext)
    }
}