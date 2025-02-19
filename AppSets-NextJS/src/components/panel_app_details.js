'use client'

import Image from "next/image";
import {useEffect, useState} from "react"
import RequestLoading from "@/components/loading_view";
import BackActionBar from "@/components/back_action_bar";
import {useGetApplicationList} from "@/components/api/api";


function isHttpSchema(str){
    var result = false
    result = String(str).startsWith("http")
    return result
}

function AppDetailsPanel({application, onNavBarItemClick, onBackClick}){
    return (
            <div className="flex flex-col overflow-auto flex-wrap scrollbar-hide scroll-smooth p-2">
                <BackActionBar backText={application.name} onBackClick={onBackClick}/>
                <ApplicationHeader application={application}/>
            </div>
        );
}

export function ApplicationHeader({application}){
    var iconUrl = "/icon_rounded_appsets_42.svg"
    if(isHttpSchema(application.iconUrl)){
        iconUrl = application.iconUrl
    }
    var applicationName = application.name
    if(applicationName==null){
        applicationName = "AppSets"
    }
    return (
        <button onClick={()=>{
           
        }}>
             <div className="mt-5">
                <div className="flex flex-col items-center place-item-center">
                    <div className="border rounded-3xl">
                        <Image priority={true} className="object-cover rounded-3xl w-32 h-32" width={128} height={38} alt={"application icon"} src={iconUrl}/>
                    </div>
                    <span className="h-2"/>
                </div>
            </div>
        </button>
       
    );
}

export default AppDetailsPanel