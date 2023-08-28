interface ImObj{
    
}
class ConversationUiState{
    
}
class Session{
    imObj:ImObj;
    conversationUiState:ConversationUiState;
    constructor(imObj:ImObj, conversationUiState:ConversationUiState) {
        this.imObj = imObj;
        this.conversationUiState = conversationUiState;
    }
}