
import PanelItem from "./panel_item";

function SplitPanel({onItemClick, routeName}){
    let panelTopItemsData=[
        {
            id:0,
            src:"/icon_rounded_appsets_42.svg",
            name:"appsets",
            namezh:"appsets"
        },
        {
            id:1,
            src:"/play_circle_FILL0_wght400_GRAD0_opsz48.svg",
            name:"start",
            namezh:"开始"
        },
        {
            id:2,
            src:"/shopping_bag_FILL0_wght400_GRAD0_opsz48.svg",
            name:"applications",
            namezh:"应用"
        },
        {
            id:3,
            src:"/explore_FILL0_wght400_GRAD0_opsz48.svg",
            name:"outside",
            namezh:"外面"
        },
        {
            id:4,
            src:"/bubble_chart_FILL0_wght400_GRAD0_opsz48.svg",
            name:"conversation",
            namezh:"对话"
        }
    ]
    let panelBottomItemsData=[
        {
            id:5,
            src:"/face_FILL0_wght400_GRAD0_opsz48.svg",
            name:"login",
            namezh:"登录"
        },
        {
            id:6,
            src:"/settings_FILL0_wght400_GRAD0_opsz48.svg",
            name:"settings",
            namezh:"设置"
        }
    ]
    const panelTopItems = []
    panelTopItemsData.forEach((item, index)=>{
        panelTopItems.push(
            <ul key={item.id} className="w-full">
                <PanelItem item={item} showText={index>0} onItemClick={onItemClick} routeName={routeName}/>
            </ul>
            );
    });
    const panelBottomItems = []
    panelBottomItemsData.forEach((item, index)=>{
        panelBottomItems.push(
            <ul key={item.id} className="w-full">
                <PanelItem item={item} showText={true} onItemClick={onItemClick} routeName={routeName}/>
            </ul>
            );
    });
    return(
        <div className="flex flex-col h-210 w-20">
            <div className="flex flex-col items-center py-4 px-2">
                {panelTopItems}
            </div>
            <div className="h-96"></div>
            <div className="flex flex-col items-center py-4 px-2">
                {panelBottomItems}
            </div>
        </div>

        );
}

export default SplitPanel