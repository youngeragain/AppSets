'use client'

import Image from "next/image";
import {useEffect, useState} from "react"
import RequestLoading from "@/components/loading_view";
import {useGetApplicationList} from "@/components/api/api";
import { RouteData } from "@/components/scaffold_container_view";

function isHttpSchema(str){
    var result = false
    result = String(str).startsWith("http")
    return result
}

function AppCenterPanel({onNavBarItemClick}){
    const model = useGetApplicationList();
    
    let loading = RequestLoading(model.data, model.error, model.isLoading);
    if(loading!=null){
        return loading
    }
    let applicationCategoryList = model.data.data;
    return (
        <ApplicationList applicationCategoryList={applicationCategoryList} onNavBarItemClick={onNavBarItemClick}/>
        );
}

function ApplicationList({ applicationCategoryList, onNavBarItemClick }){
    const applicationViews = []
    if(applicationCategoryList!=null){
        var i = 0
        applicationCategoryList.forEach((applicationCategory)=>{
            applicationCategory.applications.forEach((application)=>{
                i++
                applicationViews.push(
                    <div key={(i+application.id.timestamp)}>
                        <Application application={application} onNavBarItemClick={onNavBarItemClick}/>
                    </div>
                )
            })
        })
    }
    return (
        <div className="flex flex-row overflow-auto flex-wrap scrollbar-hide scroll-smooth">
            {applicationViews}
        </div>
        
    );
}

export function Application({application, onNavBarItemClick}){
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
            let routeData = new RouteData("application_details", application)
            onNavBarItemClick(routeData)
        }}>
             <div className="w-20 h-24 ms-3 mt-5">
                <div className="flex flex-col items-center place-item-center">
                    <div className="border rounded-3xl">
                        <Image priority={true} className="object-cover rounded-3xl w-16 h-16" width={64} height={64} alt={"application icon"} src={iconUrl}/>
                    </div>
                    <span className="h-2"/>
                    <p className="text-center text-xs">{applicationName}</p>
                </div>
            </div>
        </button>
       
    );
}

export default AppCenterPanel