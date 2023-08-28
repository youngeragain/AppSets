import {useState} from "react";
import Image from "next/image";

function ConversationPanel(){
    const [userSessions, setUserSessions] = useState(Array());
    const [groupSessions, setGroupSessions] = useState(Array());
    const [systemSessions, setSystemSessions] = useState(Array());
    const [currentTabTag, setCurrentTabTag] = useState("default");//person, group, system
    const [currentSession, setCurrentSession] = useState(null);
    
    const [testMessages, setTestMessages] = useState(Array());
    const [usetTest, setUseTest] = useState(true);
    if(usetTest){
        setUseTest(false);
        testMessages.push(
            {
                messageId:0,
                fromUid:"U1234",
                messageContent:("hi! how are you")
            }
            );
        testMessages.push(
            {
                messageId:1,
                fromUid:"U0000",
                messageContent:("hello!")
            }
            );
    }
   
    var sesssions=[];
    if(currentTabTag==="person"){
        sesssions = userSessions;
    }else if(currentTabTag==="group"){
        sesssions = groupSessions;
    }else if(currentTabTag==="system"){
        sesssions = systemSessions;
    }
    function onTabClick(tabTag){
        setCurrentTabTag(tabTag);
    }
    function onMessageSendClick(content, type){
        let tempMessage = [];
        testMessages.forEach(oldMessage => {
            tempMessage.push(oldMessage);
        });
        tempMessage.push(
            {
                messageId:(testMessages.length+1),
                fromUid:"U0000",
                messageContent:(content)
            });
        setTestMessages(tempMessage);
    }
    return(
        <div className="flex flex-row h-210 w-full">
            <div className="w-96">
                <ConversationOverview
                    sessions={sesssions}
                    currentTabTag={currentTabTag}
                    onTabClick={onTabClick}
                />
            </div>
            <div className="w-px bg-gray-200 h-full"/>
            <div className="w-full">
                <ConversationDetails testMessages={testMessages} session={currentSession} onMessageSendClick={onMessageSendClick}/>
            </div>
        </div>

        );
}

function ConversationOverview({sessions, currentTabTag, onTabClick}){
    const sessionViews = [];
    if(sessions!=null){
        sessions.forEach((session, index)=>{
            let avatarUrl = "https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png";
            sessionViews.push(
                <ul key={index}>
                    <div className="flex flex-row flex-wrap space-x-2">
                        <Image className="border rounded-lg bg-gray-200 w-9 h-9" width={36} height={36} alt={"avatar"} src={avatarUrl}/>
                        <div className="flex flex-col space-y-1">
                            <p className="text-sm font-bold">{"User name"}</p>
                            <p className="text-sm">{"无消息"}</p>
                        </div>
                    </div>
                </ul>
            );  
        });
    }
   
    return (
        <div className="flex flex-col">
            <div className="flex flex-row space-x-3 px-4 py-6">
                <button className="py-1 px-2 border rounded text-sm text-black" onClick={()=>onTabClick("person")}>个人</button>
                <button className="py-1 px-2 border rounded text-sm text-black" onClick={()=>onTabClick("group")}>群组</button>
                <button className="py-1 px-2 border rounded text-sm text-black" onClick={()=>onTabClick("system")}>系统</button>
            </div>
            <div className="flex flex-col">
                {sessionViews}
            </div>
        </div>
    );
}

function ConversationDetails({testMessages, session, onMessageSendClick}){
    const [inputText, setInputText] = useState("");
    const messageViews = [];
    testMessages.forEach((message)=>{
        var messageView = (<div className="flex flex-row flew-full">
            <p className="min-h-16 p-3 bg-blue-600 max-w-xs start_message_bubble text-sm text-white">{message.messageContent}</p>
        </div>);
        if(message.fromUid=="U0000"){
            messageView = (<div className="flex flex-row-reverse flew-full">
                <p className="min-h-16 p-3 bg-blue-600 max-w-xs end_message_bubble text-sm text-white">{message.messageContent}</p>
            </div>);
        }
        messageViews.push(
            <ul key={message.messageId} className="items-end">
                {messageView}
            </ul>
            );
    });
   
    return (
        <div className="flex flex-col h-210 p-2">
            <div className="flex flex-row h-12 py-2">
                <p>AppSets</p>
            </div>
            <div className="flex flex-col h-full overflow-auto scrollbar-hide space-y-2">
                {messageViews}
            </div>
            <div className="flex flex-col space-y-2">
                <input className="border rounded mt-3 p-2 text-sm w-full h-8" placeholder="Text something" width="100%" height={32} value={inputText} onInput={(ele)=>{
                    setInputText(ele.target.value)
                }}/>
                <div className="flex flex-row space-x-4 items-center">
                    <Image src="/add_circle_FILL0_wght300_GRAD0_opsz40.svg" alt={"add action"} width={24} height={24}/>
                    <Image src="/mic_FILL0_wght300_GRAD0_opsz40.svg" alt={"record action"} width={24} height={24}/>
                    <span className="w-full"></span>
                    <button className="py-2 px-4 bg-white border rounded-full text-xs text-black" onClick={()=>{
                        if(inputText.lenght==0||inputText=="")
                            return;
                        onMessageSendClick(inputText, "text");
                        setInputText("");
                    }}>Send</button>
                </div>
            </div>
        </div>
        );
}

export default ConversationPanel