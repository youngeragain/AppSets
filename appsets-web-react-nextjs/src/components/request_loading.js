function RequestLoading(data, error, isLoading){
    if(isLoading){
        return (<div className="flex flex-col h-210 p-4 items-center">
            <span className="h-full"/>
            <p>Loading...</p>
            <span className="h-full"/>
        </div>);
    }
    if(error){
        return (<div className="flex flex-col h-210 p-4 items-center">
            <span className="h-full"/>
            <p>Request error</p>
            <span className="h-full"/>
        </div>);
    }
    let responseData = data

    if(responseData==null||responseData.data==null){
        return (<div className="flex flex-col h-210 p-4 items-center">
            <span className="h-full"/>
            <p>Response error or parse error</p>
            <span className="h-full"/>
        </div>);
    }
    return null;
}

export default RequestLoading