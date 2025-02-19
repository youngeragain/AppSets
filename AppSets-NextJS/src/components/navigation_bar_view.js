
import Image from 'next/image';
import {RouteData} from '@/components/scaffold_container_view.js'; 

function NavigationBar({routeData, onNavBarItemClick}){
    let tabItemsData=[
        {
            id:1,
            src:"/shopping_bag_FILL0_wght400_GRAD0_opsz48.svg",
            name:"applications",
            namezh:"应用"
        },
        {
            id:2,
            src:"/explore_FILL0_wght400_GRAD0_opsz48.svg",
            name:"outside",
            namezh:"外面"
        },
        {
            id:3,
            src:"/bubble_chart_FILL0_wght400_GRAD0_opsz48.svg",
            name:"conversation",
            namezh:"对话"
        },
        {
            id:4,
            src:"/search_FILL0_wght400_GRAD0_opsz48.svg",
            name:"search",
            namezh:"搜索"
        },
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
    const tabItems = []
    tabItemsData.forEach((item, index)=>{
        if(item.name=="search"){
            tabItems.push(
                <div key={item.id} className="px-1 pt-1 min-w-fit">
                    <SearchBar item={item} showText={false} routeData={routeData} onItemClick={onNavBarItemClick}/>
                </div>
            );
        }else{
            tabItems.push(
                <div key={item.id} className="px-1 pt-1 min-w-fit">
                    <TabMainItem item={item} showText={false} routeData={routeData} onItemClick={onNavBarItemClick}/>
                </div>
            );
        }
    });
    return(
        <div className="flex flex-col w-full py-2 items-center place-items-center overflow-x-auto scrollbar-hide scroll-smooth bg-white rounded-b-3xl">
            <div className="flex">
                {tabItems}
            </div>
        </div>
        );
}

function SearchBar({item, showText, onItemClick, routeData}){
    return (
        <button key={item.id} onClick={()=>{
            let routeData = new RouteData(item.name, null)
            onItemClick(routeData)
        }}>
            <div className="flex flex-col w-full items-center place-items-center">
                <div className="flex flex-row place-items-center px-3 py-2 border rounded-full bg-gray-50">
                    <Image
                        src={item.src}
                        alt={item.name}
                        width={24}
                        height={24}
                        priority = {true}
                        />
                        <p className="text-sm px-2">{item.namezh}</p>
                        <div className="w-6"/>
                </div>
                <div className="h-1"/>
            </div>

        </button>
    );
}

function TabMainItem({item, showText, onItemClick, routeData}){
    let textView;
    if(showText){
        textView = <p className="pb-2 text-xs">{item.namezh}</p>
    }
    let indicator;
    if(routeData.routeName===item.name){
        indicator =
        <>
        <span className="h-0.5 bg-blue-500 w-4 rounded-lg"/>
        </>
    }
    return (
        <button key={item.id} onClick={()=>{
            let routeData = new RouteData(item.name, null)
            onItemClick(routeData)
        }}>
            <div className="flex flex-col w-full items-center place-items-center">
                <div className="p-2 items-center border rounded-full bg-gray-50">
                    <Image
                        src={item.src}
                        alt={item.name}
                        width={24}
                        height={24}
                        priority={true}
                        />
                </div>
                <div className="h-0.5"/>
                {textView}
                <div className="h-0.5"/>
                {indicator}
            </div>

        </button>
        );
}

export default NavigationBar