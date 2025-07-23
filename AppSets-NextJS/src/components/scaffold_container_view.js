import { useState } from 'react';
import NavigationBar from "@/components/navigation_bar_view";
import ScaffodContent from "@/components/scaffold_content_view";

export class RouteData {
    constructor(routeName, extraData){
        this.routeName = routeName
        this.extraData = extraData
    }
}

function ScaffoldContainer({ routeDataDefault }){
   
    const [lastRouteData, setLastRouteData] = useState(null);
    const [routeData, setRouteData] = useState(routeDataDefault);
    
    const [isNavBarShow, setIsNavBarShow] = useState(true)

    function onNavBarItemClick(newRouteData){
        let oldRouteData = routeData
        setRouteData(newRouteData);

        if(
            newRouteData!=null&&
            (newRouteData.routeName==="login"||
                newRouteData.routeName==="conversation_details"||
                newRouteData.routeName==="download"||
                newRouteData.routeName==="application_details"||
                newRouteData.routeName==="search"
            )
        ){
            setIsNavBarShow(false)
        }else{
            setIsNavBarShow(true)
        }

        if(lastRouteData!=null&&newRouteData.routeName==lastRouteData.routeName){
            setLastRouteData(null)
        }else{
            setLastRouteData(oldRouteData)
        }
        console.log("=======\n"+"route:"+routeData.routeName+"\nlastRoute:"+(lastRouteData?lastRouteData:oldRouteData).routeName+"\n=========")
       
    }

    function onPanelBackClick(){
        let lastRouteDataOverride = lastRouteData?lastRouteData:new RouteData("applications", null)
        onNavBarItemClick(lastRouteDataOverride)
    }

    var navibarView = (<></>)

    if(isNavBarShow){
        navibarView = (
            <div className="flex flex-col opacity-100">
                <div className="bg-gray-100 h-px"/>
                <div className="flex w-full min-w-fit">
                    <NavigationBar routeData={ routeData } onNavBarItemClick={onNavBarItemClick}/>
                </div>
            </div>
        )
    }
    

    return (
        <div className="relative w-96 min-h-screen border rounded-3xl bg-white">
            <div className="absolute inset-0 overflow-auto scrollbar-hide">
                <ScaffodContent routeData={ routeData } onNavBarItemClick={onNavBarItemClick} onPanelBackClick={onPanelBackClick}/>
            </div>
            <div className="absolute inset-x-0 bottom-0">
                {navibarView}
            </div>
        </div>
        );
}

export default ScaffoldContainer