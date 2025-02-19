'use client'

import {useGetScreens} from "@/components/api/api";
import {useEffect, useState} from "react"
import Image from "next/image";
import RequestLoading from "@/components/loading_view";


function OutSidePanel({onNavBarItemClick}){
    const model = useGetScreens();
    let loading = RequestLoading(model.data, model.error, model.isLoading);
    if(loading!=null){
        return loading
    }
    function onScreenMeidaClick(userScreenInfo, type){
        console.log("OutSidePanel,onScreenMeidaClick, type:"+type+"==> "+JSON.stringify(userScreenInfo))
    }
    const screenViews = [];
    let userScreenInfoList = model.data.data;
    userScreenInfoList.forEach((userScreenInfo=>{
       screenViews.push(
           <div key={userScreenInfo.screenId} className="w-full">
               <ScreenItem userScreenInfo={userScreenInfo} onScreenMeidaClick={onScreenMeidaClick}/>
           </div>  
       );
    }));
    
    return(
        <div className="flex flex-row flex-wrap overflow-auto scrollbar-hide scroll-smooth">
            {screenViews}
        </div>
        );
}

function ScreenItem({userScreenInfo, onScreenMeidaClick}){
    const avatarUrl = "/icon_rounded_appsets_42.svg";
    const picturlUrl = "/icon_rounded_appsets_42.svg";
    const pictureViews=[];
    if(userScreenInfo.mediaFileUrls!=null){
        userScreenInfo.mediaFileUrls.forEach((mediaFileUrl, index)=>{
            pictureViews.push(
                <ul key={index} onClick={()=>onScreenMeidaClick(userScreenInfo, "picture")}>
                    <Image
                        className="rounded-3xl bg-gray-100 w-96 h-48"
                        placeholder="empty"
                        width={384}
                        height={192}
                        priority={true}
                        onError={(e) => {
                            //e.target.style.content="";
                        }}
                        alt={""} src={picturlUrl}/>
                </ul>
            );
        });
    }

    let associateUsersView;
    if(userScreenInfo.associateUsers!=null){
        associateUsersView = (
            <div className="flex flex-col">
            <p className="p-1 text-sm">{userScreenInfo.associateUsers?userScreenInfo.associateUsers:""}</p>
        </div>
        )
    }

    let associateTopicsView;
    if(userScreenInfo.associateTopics!=null){
        associateTopicsView = (
            <div className="flex flex-col">
            <p className="p-1 text-sm">{userScreenInfo.associateTopics?userScreenInfo.associateTopics:""}</p>
        </div>
        )
    }

    return (
        <div className="flex flex-col w-full items-center place-items-center text-pretty p-2">
            <div className="flex flex-col space-y-2 p-4 border rounded-3xl">
                <div className="flex flex-row space-x-2">
                    <Image priority={true} className="border rounded-lg w-8 h-8" width={32} height={32} alt={"avatar"} src={avatarUrl}/>
                    <div className="flex flex-col space-y-1">
                        <p className="text-sm">{userScreenInfo.userInfo.name?userScreenInfo.userInfo.name:userScreenInfo.userInfo.uid}</p>
                        <p className="text-xs">{userScreenInfo.postTime?userScreenInfo.postTime:""}</p>
                    </div>
                </div>
                <div className="flex flex-col">
                    <p className="py-2 text-sm">{userScreenInfo.screenContent?userScreenInfo.screenContent:""}</p>
                </div>
                <div className="flex flex-row flex-wrap overflow-auto space-y-2">
                    {pictureViews}
                </div>
                {associateUsersView}
                {associateTopicsView}
            </div>
        </div>
    );
}

export default OutSidePanel