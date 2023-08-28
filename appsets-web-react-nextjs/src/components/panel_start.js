'use client'

import Image from "next/image";
import useSWRImmutable from "swr/immutable";
import {axiosFetcher, buildAxiosConfig} from "@/components/utils/http";
import RequestLoading from "./request_loading";
import {api_base_url, api_get_index_spotlight, useGetSpotlight} from "@/components/api/api";

function StarterPanel({ spotlight }){
    return(
        <div className="flex flex-row h-210">
            <div className="w-full h-210">
                <LeftConent/>
            </div>
            <div className="w-full h-210">
                <RightConent spotlight={ spotlight }/>
            </div>
        </div>

        );
}

function LeftConent(){
    return (
        <div className="flex flex-col w-full overflow-auto scrollbar-hide px-4 py-6 space-y-2">
            <div className="flex relative items-center">
                <p className="text-sm w-24">已固定</p>
                <button className="absolute right-0 py-1 px-2 w-20 bg-gray-100 border rounded text-xs text-black">所有应用</button>
            </div>
            <div className="h-60">
                
            </div>
            <div className="flex relative items-center">
                <p className="text-sm w-24">推荐的项目</p>
                <button className="absolute right-0 py-1 px-2 w-20 bg-gray-100 border rounded text-xs text-black">更多</button>
            </div>
            <div className="h-120">

            </div>
        </div>
    );
}

function RightConent(){
    let model = useGetSpotlight()
    let check = RequestLoading(model.data, model.error, model.isLoading);
    if(check!=null)
        return check
    let spotlight = model.data.data;
    const hotsearchViews = [];
    if(spotlight.popularSearches!=null&&spotlight.popularSearches.keywords!=null){
        spotlight.popularSearches.keywords.forEach((keyword, index) => {
            hotsearchViews.push(
                <ul key={index}>
                    <p className="p-3 text-xs">{keyword}</p>
                </ul>
                ); 
        });
    }
    const baiduHotDataViews = [];
    let baiduHotData;
    if(spotlight.baiduHotData!=null){
        baiduHotData = spotlight.baiduHotData.hotsearch
        if(baiduHotData!=null){
            baiduHotData.forEach((hotsearch, index) => {
                baiduHotDataViews.push(
                    <ul key={index}>
                        <div className="flex flex-row">
                            <p className="p-2 w-full text-xs">{hotsearch.cardTitle}</p>
                            <p className="p-2 text-xs">{hotsearch.heatScore}</p>
                        </div>

                    </ul>
                    );
            });
        }
    }
    
    var bingWallpaperView;
    if(spotlight.bingWallpaperJson!=null){
        let bingWallpaperImage = spotlight.bingWallpaperJson.images[0];
        bingWallpaperView = (
            <div className="relative bg-gray-100 h-78 border rounded">
                <div className="relative h-60">
                    <Image className="rounded bg-gray-100 h-60" placeholder="empty" layout="fill" alt={"picture"} src={"https://www.bing.com"+bingWallpaperImage.url}/>
                </div>
                <div className="relative flex flex-col mt-2 space-y-2 p-2">
                    <p className="text-sm p-1  bg-white w-fit rounded flex-wrap">{bingWallpaperImage.copyright}</p>
                    <p className="text-xs p-1 bg-white w-fit rounded">{bingWallpaperImage.title}</p>    
                </div>

            </div>
        );
    }
    
    return (
        <div className="flex flex-col overflow-auto scrollbar-hide px-4 py-6 space-y-2 h-210">
            <div className="flex flex-row items-center">
                <p className="text-sm">今天 • 8月26日</p>
            </div>
            {bingWallpaperView}
            <div className="flex flex-row space-x-2">
               <div className="flex flex-col h-48 w-full bg-gray-100 border rounded space-y-2 p-1">
                   <p className="text-sm p-1 w-fit rounded">{spotlight.wordOfTheDay.word}</p>
                    <span className="h-full"/>
                   <span className="text-xs p-1 w-fit rounded space-y-1"><p className="font-bold">每日一言：</p><p>{spotlight.wordOfTheDay.author}</p></span>
               </div>
                <div className="relative h-48 w-full bg-gray-100 border rounded">
                    <div className="h-48">
                        <Image className="rounded bg-gray-100 h-48" placeholder="empty" layout="fill" alt={"picture"} src={spotlight.todayInHistory.picUrl}/>
                    </div>
                    <span className="absolute bottom-1 left-1 text-sm p-1 bg-white w-fit rounded space-y-1"><p className="font-bold">{spotlight.todayInHistory.title}</p><p>{spotlight.todayInHistory.event}</p></span>
                </div>
            </div>
            <div className="flex flex-col w-full bg-gray-100 border rounded space-y-2 p-1">
                <p className="text-sm p-1">热门搜索</p>
                <div className="flex flex-row flex-wrap w-full space-x-2 p-1">
                    {hotsearchViews}
                </div>
            </div>
            <div className="flex flex-col w-full bg-gray-100 border rounded space-y-2 p-1">
                <p className="text-sm p-1">百度热搜</p>
                <div className="flex flex-col w-full space-y-2 p-1">
                    {baiduHotDataViews}
                </div>
            </div>
        </div>
    );
}

export default StarterPanel