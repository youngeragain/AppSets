package xcj.app.appsets.im

import android.os.Parcelable
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.StringMessageMetadata
import xcj.app.appsets.im.message.TextMessage
import java.util.Date
import java.util.UUID

object GenerativeAISessions {

    @Parcelize
    data class AIModel(val name: String) : Parcelable

    @Parcelize
    data class AIModelInfo(
        val description: String?,
        val models: List<AIModel>,
        val type: Int,
        override val bioId: String,
        override val bioName: String,
        override val bioUrl: Int?
    ) : Bio

    const val TYPE_ONLINE = 0
    const val TYPE_ON_DEVICE = 1

    const val TYPE_MIX = 2

    suspend fun handleSessionNewMessage(session: Session, userPrompt: Any) {
        if (userPrompt is TextMessage) {
            session.conversationState.addMessage(userPrompt)
            delay(200)
            val imMessage = createResponseTemplateMessage(session, userPrompt)
            session.conversationState.addMessage(imMessage)
        }
    }

    fun createResponseTemplateMessage(session: Session, userPrompt: TextMessage): IMMessage<*> {
        val toUserInfo = LocalAccountManager.userInfo
        val toIMObj = IMObj.fromBio(toUserInfo)
        val messageFromInfo = MessageFromInfo(
            session.imObj.id,
            session.imObj.name,
            null
        ).apply {
            bioUrl = session.imObj.bio.bioUrl
        }
        val messageToInfo = MessageToInfo.fromImObj(toIMObj).apply {
            bioUrl = session.imObj.bio.bioUrl
        }
        return TextMessage(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            fromInfo = messageFromInfo,
            toInfo = messageToInfo,
            messageGroupTag = null,
            metadata = StringMessageMetadata.fromString("\uD83D\uDC31, Sorry, temporarily unavailable!"),
        )
    }


    fun createStartMessage(bio: Bio): IMMessage<*> {
        val toUserInfo = LocalAccountManager.userInfo
        val toIMObj = IMObj.fromBio(toUserInfo)
        val messageToInfo = MessageToInfo.fromImObj(toIMObj).apply {
            bioUrl = bio.bioUrl
        }
        val messageFromInfo = MessageFromInfo(bio.bioId, bio.bioName, null).apply {
            bioUrl = bio.bioUrl
        }
        return TextMessage(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            fromInfo = messageFromInfo,
            toInfo = messageToInfo,
            messageGroupTag = null,
            metadata = StringMessageMetadata.fromString("Ask me anything!")
        )
    }

    val onlineSessions: List<Session>
        get() {
            return listOf(
                geminiSession,
                claudeSession,
                openAiChatGPTSession,
                microsoftCopilotSession,
                deepSeekSession,
                wenxinyiyanSession,
                doubaoSession,
                qwenSession,
                yuanbaoSession,
            )
        }

    val geminiSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Gemini",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Gemini is Google's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val claudeSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Claude",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_claude,
                description = "Claude is Anthropic's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val openAiChatGPTSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "OpenAI ChatGPT",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_openai,
                description = "OpenAI ChatGPT is OpenAI's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val microsoftCopilotSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Microsoft Copilot",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_microsoft_copilot,
                description = "Microsoft Copilot is Microsoft's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val deepSeekSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "DeepSeek",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_deepseek,
                description = "DeepSeek is 深度求索's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )
            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val wenxinyiyanSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "文心一言",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_baidu_wenxin,
                description = "文心一言 is Baidu's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val doubaoSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "豆包",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_doubao,
                description = "豆包 is ByteDance's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE,
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val qwenSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "千问",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_alibaba_qwen,
                description = "千问 is Alibaba's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val yuanbaoSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "元宝",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_tencent_yuanbao,
                description = "元宝 is Tencent's AI Model",
                models = emptyList(),
                type = TYPE_ONLINE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val onDeviceSessions: List<Session>
        get() {
            return listOf(
                geminiNanoSession,
                gemmaSession,
                mistralSession,
                phi_2Session,
                tinyLlamaSession,
                zephyrSession
            )
        }

    val geminiNanoSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Gemini Nano",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Gemini Nano is On-device AI Model",
                models = emptyList(),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val gemmaSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Gemma",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Gemma is On-device AI Model",
                models = listOf(AIModel("Gemma 2B"), AIModel("Gemma 7B")),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val mistralSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Mistral",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Mistral is On-device AI Model",
                models = listOf(AIModel("Mistral-Lite 7B")),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val phi_2Session: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Phi-2",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Phi-2 is On-device AI Model",
                models = emptyList(),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val tinyLlamaSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "TinyLlama",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "TinyLlama is On-device AI Model",
                models = emptyList(),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val zephyrSession: Session
        get() {
            val bio = AIModelInfo(
                bioId = UUID.randomUUID().toString(),
                bioName = "Zephyr",
                bioUrl = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini,
                description = "Zephyr is On-device AI Model",
                models = emptyList(),
                type = TYPE_ON_DEVICE
            )

            return Session(
                imObj = IMObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val mixedSessions: List<Session>
        get() {
            return listOf()
        }
}