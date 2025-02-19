'use client'
import axios from "axios";
import {buildAxiosConfig} from "@/components/utils/http";
import BackActionBar from "@/components/back_action_bar";

function DownloadPanel({onBackClick}){
    const platformsDownloadInfo = [
        {
            platform:"Android",
            canDownload:true,
            downloadButtonDescription:"下载",
            downloadUrl:"http://162.14.70.230:3600/appsets/file/get/app/release/android",
            downloadButtonTooltips:"直接下载安装文件"
        },
        {
            platform:"iOS",
            canDownload:false,
            downloadButtonDescription:"AppStore",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Windows",
            canDownload:false,
            downloadButtonDescription:"Microsoft Store",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Linux",
            canDownload:false,
            downloadButtonDescription:"Snap Store",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Mac OS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"ChromeOS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Harmony OS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Kai OS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Sailfish OS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Ubuntu Touch",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        },
        {
            platform:"Tizen OS",
            canDownload:false,
            downloadButtonDescription:"下载",
            downloadUrl:"",
            downloadButtonTooltips:"未就绪"
        }
    ];
    function onButtonClick(platformsDownloadInfo){
        if(platformsDownloadInfo.platform==="Android"){
            if(platformsDownloadInfo.canDownload){
                console.log("DownloadPanel:下载AppSets Android版");
                window.open(platformsDownloadInfo.downloadUrl, '_self');
                //axios.request(platformsDownloadInfo.downloadUrl, buildAxiosConfig("GET", null))
            }
        }

    }
    const downloadViews = [];
    platformsDownloadInfo.forEach((platfromDownloadInfo, index) => {
        var button = (
            <button
                title={platfromDownloadInfo.downloadButtonTooltips}
                className="mt-3 ml-2 py-2 px-4 bg-grey-100 border rounded-lg text-xs text-black w-36"
                onClick={()=>onButtonClick(platfromDownloadInfo)}>
                    {platfromDownloadInfo.downloadButtonDescription}
            </button>
            );
        if(platfromDownloadInfo.canDownload){
            button = (
                <button
                    title={platfromDownloadInfo.downloadButtonTooltips}
                    className="mt-3 ml-2 py-2 px-4 bg-blue-600 border rounded-lg text-xs text-white w-36"
                    onClick={()=>onButtonClick(platfromDownloadInfo)}>
                        {platfromDownloadInfo.downloadButtonDescription}
                </button>
                );
        }
        downloadViews.push(
            <ul key={index}>
                <div className="flex flex-col">
                    <p className="mt-5 ml-2">{platfromDownloadInfo.platform}</p>
                    {button}
                </div>
            </ul>
        );
    });
    return(
        <div className="flex flex-col p-2">
             <BackActionBar backText={""} onBackClick={onBackClick}/>
            <p className="text-2xl font-bold mt-3 ml-2">下载AppSets</p>
            <div className="flex flex-col overflow-auto scrollbar-hide">
                {downloadViews}
            </div>
        </div>
        );
}

export default DownloadPanel