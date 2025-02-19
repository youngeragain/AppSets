

import AppCenterPanel from "@/components/panel_app_center";
import AppDetailsPanel from "@/components/panel_app_details";
import OutSidePanel from "@/components/panel_outside";
import ConversationPanel from "@/components/panel_conversation";
import ConversationDetailsPanel from "@/components/panel_conversation_details";
import LoginPanel from "@/components/panel_login";
import SettingsPanel from "@/components/panel_settings";
import UserProfilePanel from "@/components/panel_userprofile";
import DownloadPanel from "@/components/panel_download";
import SearchPanel from "@/components/panel_search";
import { RouteData } from "@/components/scaffold_container_view.js";

function ScaffodContent({routeData, onNavBarItemClick, onPanelBackClick}){
    if(routeData.routeName==="applications"){
        return <AppCenterPanel onNavBarItemClick={onNavBarItemClick}/>
    }
    if(routeData.routeName==="application_details"){
        return <AppDetailsPanel application={routeData.extraData} onNavBarItemClick={onNavBarItemClick} onBackClick={onPanelBackClick}/>
    }

    if(routeData.routeName==="outside"){
        return <OutSidePanel onNavBarItemClick={onNavBarItemClick}/>
    }

    if(routeData.routeName==="conversation"){
        return <ConversationPanel session={routeData.extraData} onSessionClick={onNavBarItemClick}/>
    }

    if(routeData.routeName=="conversation_details"){
        return <ConversationDetailsPanel onNavBarItemClick={onNavBarItemClick} onBackClick={onPanelBackClick}/>
    }

    if(routeData.routeName==="login"){
        return <LoginPanel onNavBarItemClick={onNavBarItemClick} onBackClick={onPanelBackClick}/>
    }

    if(routeData.routeName==="settings"){
        return <SettingsPanel onNavBarItemClick={onNavBarItemClick}/>
    }
    if(routeData.routeName==="userprofile"){
        return <UserProfilePanel onNavBarItemClick={onNavBarItemClick}/>
    }
    if(routeData.routeName==="download"){
        return <DownloadPanel onBackClick={onPanelBackClick}/>
    }
    if(routeData.routeName==="search"){
        return <SearchPanel onBackClick={onPanelBackClick}/>
    }
    return (<p/>);
}

export default ScaffodContent