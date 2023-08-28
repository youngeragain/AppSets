'use client'
import useSWRImmutable from "swr/immutable";
import {axiosFetcher, buildAxiosConfig} from "@/components/utils/http";
import {api_base_url, api_get_index_applicaitons, useGetApplicationList} from "@/components/api/api";
import Image from "next/image";
import RequestLoading from "./request_loading";

function ApplicationsPanel(){
    var model = useGetApplicationList();
    let check = RequestLoading(model.data, model.error, model.isLoading);
    if(check!=null)
        return check
    let  applicationWithCategoryList = model.data.data;
    let headerApplication = applicationWithCategoryList[0].applications[0]
    
    console.log(headerApplication)
    return(
        <div className="overflow-auto scrollbar-hide h-210 p-4">
            <Header application={headerApplication}/>
            <span className="h-6"/>
            <ApplicationCategoryList applicationWithCategoryList={applicationWithCategoryList}/>
        </div>
        );
}

function Header({ application }){
    var iconUrl="https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png"
    const bannerUrl = "https://img1.baidu.com/it/u=1157252718,2208155279&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800";
    return (
        <div className="relative border rounded-2xl h-72 w-150">
            <Image className="h-full w-full bg-red-100 rounded-2xl" alt={"cover"} width={32} height={32} src={bannerUrl}/>
            <div className="absolute top-4 left-4 flex flex-row space-x-4 items-center">
                <Image className="object-cover" alt={"icon"} width={32} height={32} src={iconUrl}/>
                <p className="text-white">{application.name}</p>
            </div>
        </div>
    );
}

function ApplicationCategoryList({ applicationWithCategoryList }){
    const categoryViews = []
    if(applicationWithCategoryList!=null&&applicationWithCategoryList.length!==0){
        applicationWithCategoryList.forEach((applicationWithCategory)=>{
            categoryViews.push(
                <ul key={applicationWithCategory.categoryName}>
                    <div className="flex flex-col py-4">
                        <p className="font-semibold py-4">{applicationWithCategory.categoryNameZh}</p>
                        <ApplicationList applicationList={applicationWithCategory.applications}/>
                    </div>
                </ul>
            )
        })
    }
    return (
        <div className="flex flex-col">
            {categoryViews}
        </div>
        
    );
}

function ApplicationList({ applicationList }){
    const applicationViews = []
    if(applicationList!=null&&applicationList.length!==0){
        applicationList.forEach((application)=>{
            var iconUrl="https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png"
            var applicationName = application.name
            if(applicationName==null)
                applicationName = "AppSets"
            applicationViews.push(
                <ul key={application.id.timestamp}>
                    <div className="flex flex-col p-2 w-32 items-center">
                        <div className="border rounded-2xl">
                            <Image className="object-cover" width={96} height={96} alt={"application icon"} src={iconUrl}/>
                        </div>
                        <span className="h-6"/>
                        <p className="text-center">{applicationName}</p>
                    </div>
                </ul>
                );
        })
    }
    return (
        <div className="flex flex-row space-x-8">
            {applicationViews}
        </div>

        );
}

export default ApplicationsPanel