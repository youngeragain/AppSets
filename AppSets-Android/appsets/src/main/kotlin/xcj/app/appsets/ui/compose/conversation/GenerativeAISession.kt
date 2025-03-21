package xcj.app.appsets.ui.compose.conversation;

import kotlinx.coroutines.delay
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio;
import xcj.app.appsets.im.ConversationState
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.TextMessage
import java.util.Date
import java.util.UUID

object GenerativeAISession {

    interface AIBio : Bio

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
            metadata = ImMessage.textImMessageMetadata("\uD83D\uDC31, Sorry, temporarily unavailable!"),
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
            metadata = ImMessage.textImMessageMetadata("Ask me anything!")
        )
    }

    val geminiSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Gemini"
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_google_gemini
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }

    val cursorSession: Session
        get() {
            val bio = object : AIBio {
                override val id: String = UUID.randomUUID().toString()
                override val name: String = "Cursor"
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_cursor
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
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_openai_logomark
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
                    xcj.app.appsets.R.drawable.ai_model_logo_microsoft_copilot
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
                override val name: String = "讯飞星火"
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_xunfei_spark
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
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_baidu_yiyan
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
                override val bioUrl: Any? = xcj.app.appsets.R.drawable.ai_model_logo_doubao_white_bg
            }

            return Session(
                imObj = ImObj.fromBio(bio),
                conversationState = ConversationState()
            )
        }
}