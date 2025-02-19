import { RouteData } from "./scaffold_container_view";

function SettingsPanel({onNavBarItemClick}){
    return(
        <div className="flex flex-col h-full p-2">
            <p className="text-2xl font-bold mt-3 ml-2">设置</p>
           
            <p className="text-xl font-bold mt-6 ml-2">下载</p>
            <button onClick={()=>{
                let routeData = new RouteData("download", null)
                onNavBarItemClick(routeData)
            }} className="mt-3 ml-2 py-3 px-4 bg-gray-100 border rounded-full text-xs text-black w-fit">下载页面</button>

            <p className="text-xl font-bold mt-6 ml-2">关于</p>
            <button className="mt-3 ml-2 py-3 px-4 bg-gray-100 border rounded-full text-xs text-black w-fit">查看</button>
        </div>
        );
}

export default SettingsPanel