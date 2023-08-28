function SettingsPanel(){
    return(
        <div className="flex flex-col h-full p-2">
            <p className="text-2xl font-bold mt-3 ml-2">设置</p>
            <p className="text-xl font-bold mt-6 ml-2">关于</p>
            <button className="mt-3 ml-2 py-2 px-2 bg-gray-100 border rounded text-xs text-black w-fit">关于AppSets</button>
        </div>
        );
}

export default SettingsPanel