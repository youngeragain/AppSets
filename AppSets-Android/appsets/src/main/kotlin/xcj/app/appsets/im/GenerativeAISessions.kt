package xcj.app.appsets.im

import kotlinx.coroutines.delay
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.TextMessage
import java.util.Date
import java.util.UUID

object GenerativeAISessions {

    data class AIModel(val name: String)

    interface AIBio : Bio {
        val description: String?
        val models: List<AIModel>
        val type: Int
    }

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

    fun createResponseTemplateMessage(session: Session, userPrompt: TextMessage): ImMessage {
        val toUserInfo = LocalAccountManager.userInfo
        val toImObj = ImObj.fromBio(toUserInfo)
        val messageFromInfo = MessageFromInfo(
            session.imObj.id,
            session.imObj.name,
            null
        ).apply {
            bioUrl = session.imObj.bio.bioUrl
        }
        val messageToInfo = MessageToInfo.fromImObj(toImObj).apply {
            bioUrl = session.imObj.bio.bioUrl
        }
        return TextMessage(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            fromInfo = messageFromInfo,
            toInfo = messageToInfo,
            messageGroupTag = null,
            metadata = ImMessage.Companion.textImMessageMetadata("\uD83D\uDC31, Sorry, temporarily unavailable!"),
        )
    }


    fun createStartMessage(bio: Bio): ImMessage {
        val toUserInfo = LocalAccountManager.userInfo
        val toImObj = ImObj.fromBio(toUserInfo)
        val messageToInfo = MessageToInfo.fromImObj(toImObj).apply {
            bioUrl = bio.bioUrl
        }
        val messageFromInfo = MessageFromInfo(bio.id, bio.name, null).apply {
            bioUrl = bio.bioUrl
        }
        return TextMessage(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            fromInfo = messageFromInfo,
            toInfo = messageToInfo,
            messageGroupTag = null,
            metadata = ImMessage.Companion.textImMessageMetadata("Ask me anything!")
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
                xunfeiSparkSession,
                wenxinyiyanSession,
                doubaoSession,
                qwenSession,
                yuanbaoSession,
            )
        }

    val geminiSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Gemini"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Gemini is Google's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val claudeSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Claude"
                override val bioUrl: Any? = R.drawable.ai_model_logo_claude
                override val description: String? = "Claude is Anthropic's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val openAiChatGPTSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "OpenAI ChatGPT"
                override val bioUrl: Any? = R.drawable.ai_model_logo_openai
                override val description: String? = "OpenAI ChatGPT is OpenAI's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val microsoftCopilotSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Microsoft Copilot"
                override val bioUrl: Any? =
                    R.drawable.ai_model_logo_microsoft_copilot
                override val description: String? = "Microsoft Copilot is Microsoft's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val deepSeekSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "DeepSeek"
                override val bioUrl: Any? = R.drawable.ai_model_logo_deepseek
                override val description: String? = "DeepSeek is 深度求索's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val xunfeiSparkSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "星火"
                override val bioUrl: Any? = R.drawable.ai_model_logo_xunfei_spark
                override val description: String? = "星火 is 讯飞's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val wenxinyiyanSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "文心一言"
                override val bioUrl: Any? = R.drawable.ai_model_logo_baidu_wenxin
                override val description: String? = "文心一言 is Baidu's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val doubaoSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "豆包"
                override val bioUrl: Any? = R.drawable.ai_model_logo_doubao
                override val description: String? = "豆包 is ByteDance's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val qwenSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "通义千问"
                override val bioUrl: Any? = R.drawable.ai_model_logo_alibaba_qwen
                override val description: String? = "通义千问 is Alibaba's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val yuanbaoSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "元宝"
                override val bioUrl: Any? = R.drawable.ai_model_logo_tencent_yuanbao
                override val description: String? = "元宝 is Tencent's AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ONLINE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
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
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Gemini Nano"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Gemini Nano is On-device AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val gemmaSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Gemma"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Gemma is On-device AI Model"
                override val models: List<AIModel> =
                    listOf(AIModel("Gemma 2B"), AIModel("Gemma 7B"))
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val mistralSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Mistral"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Mistral is On-device AI Model"
                override val models: List<AIModel> = listOf(AIModel("Mistral-Lite 7B"))
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
    val phi_2Session: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Phi-2"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Phi-2 is On-device AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val tinyLlamaSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "TinyLlama"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "TinyLlama is On-device AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val zephyrSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Zephyr"
                override val bioUrl: Any? = R.drawable.ai_model_logo_google_gemini
                override val description: String? = "Zephyr is On-device AI Model"
                override val models: List<AIModel> = emptyList()
                override val type: Int = TYPE_ON_DEVICE
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val mixedSessions: List<Session>
        get() {
            return listOf()
        }
}