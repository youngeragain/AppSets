import StarterPanel from "@/components/panel_start";
import ApplicationsPanel from "@/components/panel_application";
import OutSidePanel from "@/components/panel_outside";
import ConversationPanel from "@/components/panel_conversation";
import LoginPanel from "@/components/panel_login";
import SettingsPanel from "@/components/panel_settings";
import UserProfilePanel from "@/components/panel_userprofile";
import DownloadPanel from "@/components/panel_download";

function SplitContent({ routeName }){
    if(routeName==="start"){
        return <StarterPanel/>
    }
    if(routeName==="applications"){
        return <ApplicationsPanel/>
    }

    if(routeName==="outside"){
        return <OutSidePanel/>
    }

    if(routeName==="conversation"){
        return <ConversationPanel/>
    }

    if(routeName==="login"){
        return <LoginPanel/>
    }

    if(routeName==="settings"){
        return <SettingsPanel/>
    }
    if(routeName==="userprofile"){
        return <UserProfilePanel/>
    }
    if(routeName==="download"){
        return <DownloadPanel/>
    }
    return (<p/>);
}

export default SplitContent