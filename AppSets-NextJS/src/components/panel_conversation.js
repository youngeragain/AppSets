import Image from "next/image";
import {useEffect, useState} from "react"
import { RouteData } from "./scaffold_container_view";

class Session{
    
}

function ConversationPanel({onSessionClick}){
    const [aiSessions, setAiSessions] = useState(Array());
    const [userSessions, setUserSessions] = useState(Array());
    const [groupSessions, setGroupSessions] = useState(Array());
    const [systemSessions, setSystemSessions] = useState(Array());
    const [currentTabTag, setCurrentTabTag] = useState("generative_ai");//generative_ai, personal, group, system
    const [currentSession, setCurrentSession] = useState(null);
    
    var sesssions=[];
    if(currentTabTag==="generative_ai"){
        sesssions = aiSessions;
    }else if(currentTabTag==="personal"){
        sesssions = userSessions;
    }else if(currentTabTag==="group"){
        sesssions = groupSessions;
    }else if(currentTabTag==="system"){
        sesssions = systemSessions;
    }
    if(sesssions.length<=0){
        for(var i=0;i<5;i++){
            let session = new Session()
            sesssions.push(session)
        }
    }

    function onTabClick(tabTag){
        setCurrentTabTag(tabTag);
    }
    
    return(
        <ConversationOverview
            onSessionClick={onSessionClick}
            sessions={sesssions}
            currentTabTag={currentTabTag}
            onTabClick={onTabClick}
        />
        );
}

function ConversationOverview({onSessionClick, sessions, currentTabTag, onTabClick}){
    const sessionViews = [];
    if(sessions!=null){
        sessions.forEach((session, index)=>{
            sessionViews.push(
                <SessionView key={index} onClick={onSessionClick} session={session}/>
            );  
        });
    }
   
    return (
        <div className="flex flex-col">
            <div className="flex-row space-x-3 px-2 py-3">
                <button className="py-1 px-2 border rounded-full text-sm text-black" onClick={()=>onTabClick("generative_ai")}>Generative AI</button>
                <button className="py-1 px-2 border rounded-full text-sm text-black" onClick={()=>onTabClick("personal")}>个人</button>
                <button className="py-1 px-2 border rounded-full text-sm text-black" onClick={()=>onTabClick("group")}>群组</button>
                <button className="py-1 px-2 border rounded-full text-sm text-black" onClick={()=>onTabClick("system")}>系统</button>
            </div>
            <div className="bg-gray-100 h-px"/>
            <div className="flex flex-row flex-wrap overflow-auto scrollbar-hide scroll-smooth">
                {sessionViews}
            </div>
            
        </div>
    );
}

function SessionView({onClick, session}){
    let avatarUrl = "/icon_rounded_appsets_42.svg";
    return (
        <div className="flex flex-row w-full space-x-2 p-2" onClick={()=>{
            let routeData = new RouteData("conversation_details", session)
            onClick(routeData)
        }}>
            <Image priority={true} className="border rounded-2xl bg-gray-200 w-12 h-12" width={48} height={48} alt={"avatar"} src={avatarUrl}/>
                <div className="flex flex-col space-y-1">
                    <p className="text-sm">{"AppSets"}</p>
                    <p className="text-sm">{"无消息"}</p>
                </div>
        </div>
    )
}

export default ConversationPanel