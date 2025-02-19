import Image from "next/image";
import {useEffect, useState} from "react"
import BackActionBar from "@/components/back_action_bar";

function ConversationDetailsPanel({session, onNavBarItemClick, onBackClick}){
    const [inputText, setInputText] = useState("");
   

    const [messages, setMessages] = useState(Array());
    const [test, setTest] = useState(true);
    const messageViews = [];
   
    if(test){
        setTest(false);
        messages.push(
            {
                messageId:0,
                fromUid:"U1234",
                messageContent:("hi! how are you")
            }
            );
            messages.push(
            {
                messageId:1,
                fromUid:"U0000",
                messageContent:("hello!")
            }
            );
    }

    function onMessageSendClick(content, type){
        let tempMessage = [];
        messages.forEach(oldMessage => {
            tempMessage.push(oldMessage);
        });
        tempMessage.push(
            {
                messageId:(messages.length+1),
                fromUid:"U0000",
                messageContent:(content)
            });
        setMessages(tempMessage);
    }

    messages.forEach((message)=>{
        var messageView = (<></>)
        if(message.fromUid == "U0000"){
            messageView = (<div className="flex flex-row-reverse flew-full">
                <p className="p-3 bg-blue-600 max-w-xs end_message_bubble text-sm text-white">{message.messageContent}</p>
            </div>);
        }else{
            messageView = (<div className="flex flex-row flew-full">
                <p className="p-3 bg-blue-500 max-w-xs start_message_bubble text-sm text-white">{message.messageContent}</p>
            </div>);
        }
        messageViews.push(
            <ul key={message.messageId} className="items-end">
                {messageView}
            </ul>
        );
    });
   
    return (
        <div className="flex flex-col h-full min-w-96 space-y-2">
            <div className="flex flex-col">
                <div className="relative w-full py-4">
                    <div className="flex flex-row absolute inset-y-0 left-0 px-3 place-item-center">
                        <Image src="/arrow_back_FILL0_wght400_GRAD0_opsz40.svg" width={24} height={24} alt="back" onClick={()=>{
                                onBackClick()
                            }} priority={true}/>
                    </div>
                    <div className="flex flex-row place-self-center place-item-center">
                        <Image src="/icon_rounded_appsets_42.svg" width={24} height={24} alt="avatar" priority={true}/>
                        <div className="p-1"></div>
                        <p>AppSets</p>
                    </div>
                    
                </div>
                <div className="bg-gray-100 h-px"/>
            </div>
            
            <div className="flex flex-col h-full overflow-auto scrollbar-hide space-y-2 p-2">
                {messageViews}
            </div>
            <div className="flex flex-col space-y-2 p-2">
                <input className="border rounded-full mt-3 p-4 text-sm w-full h-12 w-full" placeholder="Text something" value={inputText} onInput={(ele)=>{
                    setInputText(ele.target.value)
                }}/>
                <div className="flex flex-row space-x-4 items-center">
                    <Image src="/add_circle_FILL0_wght300_GRAD0_opsz40.svg" alt={"add action"} width={24} height={24} priority={true}/>
                    <Image src="/mic_FILL0_wght300_GRAD0_opsz40.svg" alt={"record action"} width={24} height={24} priority={true}/>
                    <span className="w-full"></span>
                    <button className="py-3 px-4 bg-white border rounded-full text-xs text-black" onClick={()=>{
                        if(inputText.length==0||inputText=="")
                            return;
                        onMessageSendClick(inputText, "text");
                        setInputText("");
                    }}>Send</button>
                </div>
            </div>
        </div>
    );
}

export default ConversationDetailsPanel