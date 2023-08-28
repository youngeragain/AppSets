'use client'

import useSWRImmutable from "swr/immutable";
import {api_base_url, api_get_index_screens, useGetScreens} from "@/components/api/api";
import {axiosFetcher, buildAxiosConfig} from "@/components/utils/http";
import Image from "next/image";
import RequestLoading from "./request_loading";




function OutSidePanel(){
    let model = useGetScreens()
    let check = RequestLoading(model.data, model.error, model.isLoading);
    if(check!=null)
        return check
    function onScreenMeidaClick(userScreenInfo, type){
        console.log("OutSidePanel,onScreenMeidaClick, type:"+type+"==> "+JSON.stringify(userScreenInfo))
    }
    const screenViews = [];
    let userScreenInfoList = model.data.data;
    userScreenInfoList.forEach((userScreenInfo=>{
       screenViews.push(
           <ul key={userScreenInfo.screenId}>
               <ScreenItem userScreenInfo={userScreenInfo} onScreenMeidaClick={onScreenMeidaClick}/>
           </ul>  
       );
    }));
    
    return(
        <div className="flex flex-col overflow-auto scrollbar-hide h-210 p-4 scroll-smooth">
            {screenViews}
        </div>

        );
}

function ScreenItem({userScreenInfo, onScreenMeidaClick}){
    const avatarUrl = "https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png";
    const picturlUrl = "https://img1.baidu.com/it/u=1157252718,2208155279&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800";
    const pictureViews=[];
    if(userScreenInfo.mediaFileUrls!=null){
        userScreenInfo.mediaFileUrls.forEach((mediaFileUrl, index)=>{
            pictureViews.push(
                <ul key={index} className="p-2" onClick={()=>onScreenMeidaClick(userScreenInfo, "picture")}>
                    <Image
                        className="rounded bg-gray-100 w-32 h-20"
                        placeholder="empty"
                        width={128}
                        height={80}
                        onError={(e) => {
                            //e.target.style.content="";
                        }}
                        alt={""} src={picturlUrl}/>
                </ul>
            );
        });
    }
    return (
        <div className="flex flex-col w-120 space-y-1 p-2">
            <div className="flex flex-row flex-wrap space-x-2">
                <Image className="border rounded-lg bg-gray-200 w-8 h-8" width={32} height={32} alt={"avatar"} src={avatarUrl}/>
                <div className="flex flex-col space-y-1">
                    <p className="text-xs">{userScreenInfo.userInfo.name?userScreenInfo.userInfo.name:userScreenInfo.userInfo.uid}</p>
                    <p className="text-xs">{userScreenInfo.postTime?userScreenInfo.postTime:""}</p>
                </div>
            </div>
            <div className="flex flex-row flex-wrap">
                <p className="p-1 text-sm">{userScreenInfo.screenContent?userScreenInfo.screenContent:""}</p>
            </div>
            <div className="flex flex-row flex-wrap">
                {pictureViews}
            </div>
            <div className="flex flex-row flex-wrap">
                <p className="p-1 text-sm">{userScreenInfo.associateUsers?userScreenInfo.associateUsers:""}</p>
            </div>
            <div className="flex flex-row flex-wrap">
                <p className="p-1 text-sm">{userScreenInfo.associateTopics?userScreenInfo.associateTopics:""}</p>
            </div>
        </div>
    );
}

export default OutSidePanel