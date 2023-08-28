import Image from 'next/image'; 
function PanelItem({item, showText, onItemClick, routeName}){
    let textView;
    if(showText){
        textView = <p className="pb-2 text-xs">{item.namezh}</p>
    }
    let indicator;
    if(routeName===item.name){
        indicator =
        <>
        <span className="h-6 bg-blue-500 w-px-3 rounded-lg"/>
        <span className="w-4"/>
        </>
    }
    return (
        <button key={item.id} onClick={()=>onItemClick(item.name)} className="w-full my-1 rounded-lg">
            <div className="flex flex-row w-full items-center place-items-center">
                <div className="w-5 flex flex-row">
                    {indicator}
                </div>
                <div className="flex flex-col items-cente">
                    <Image
                        className="py-2"
                        src={item.src}
                        alt={item.name}
                        width={24}
                        height={24}
                        priority/>
                    {textView}
                </div>
            </div>

        </button>
        );
}

export default PanelItem