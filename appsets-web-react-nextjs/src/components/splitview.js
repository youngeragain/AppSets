import { useState } from 'react';
import SplitPanel from "./splitview_panel";
import SplitContent from "./splitview_content";

function SplitView({ routeNameDefault }){
    const [routeName, setRouteName] = useState(routeNameDefault?routeNameDefault:"start");
    
    function onPanelItemClick(name){
        if(name==="appsets")
            return;
        setRouteName(name);
    }
    return (
        <div className="flex flex-row border rounded-lg bg-white h-210 w-full min-w-fit">
            <div className="w-20 min-w-fit">
                <SplitPanel onItemClick={onPanelItemClick} routeName={ routeName }/>
            </div>
            <div className="bg-gray-200 w-px"/>
            <div className="flex flex-col h-210 w-full">
                <SplitContent routeName={ routeName } />
            </div>
        </div>
        );
}

export default SplitView