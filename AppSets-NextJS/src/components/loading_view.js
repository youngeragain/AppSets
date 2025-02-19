import Image from 'next/image'; 

function RequestLoading(data, error, isLoading){
    let iconUrl="/icon_rounded_appsets_42.svg"
    if(isLoading){
        return (<div className="flex flex-col w-full h-full p-4 items-center">
            <span className="h-full"/>
            <div className="border rounded-3xl">
                <Image priority={true} className="object-cover rounded-3xl" width={68} height={68} alt={"appsets logo"} src={iconUrl}/>
            </div>
            <span className="h-full"/>
        </div>);
    }
    if(error){
        return (<div className="flex flex-col w-full h-full p-4 items-center">
            <span className="h-full"/>
            <div className="border rounded-3xl">
                <Image priority={true} className="object-cover rounded-3xl" width={68} height={68} alt={"appsets logo"} src={iconUrl}/>
            </div>
            {/* Request error */}
            <p className='p-4'>Error01</p>
            <span className="h-full"/>
        </div>);
    }
    let responseData = data

    if(responseData==null||responseData.data==null){
        return (<div className="flex flex-col w-full h-full p-4 items-center">
            <span className="h-full"/>
            <div className="border rounded-3xl">
                <Image priority={true} className="object-cover rounded-3xl" width={68} height={68} alt={"appsets logo"} src={iconUrl}/>
            </div>
            {/* Response error or parse error */}
            <p className='p-4'>Error02</p>
            <span className="h-full"/>
        </div>);
    }
    return null;
}

export default RequestLoading