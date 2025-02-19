import {axiosFetcher, buildAxiosConfig} from "@/components/utils/http";
import useSWR from "swr";
import {CONFIG_HTTPS} from "@/components/utils/config";

export const api_base_url_https = "https://localhost:8084/";

export const api_base_url = "http://162.14.70.230:3401/";

//export const api_base_url = "http://localhost:8085/";

export const api_get_index_applicaitons = {
    method:"GET",
    path:"appsets/apps/index/recommend"
};
export const api_get_index_spotlight = {
    method:"GET",
    path:"appsets/spotlight"
};

export const api_get_index_screens = {
    method:"GET",
    path:"user/screens/index/recommend"
};

function makeUrl(suffix){
    if(CONFIG_HTTPS){
        return api_base_url_https+suffix
    }else{
        return api_base_url+suffix
    }
}

export function useGetApplicationList(){
    let uri = makeUrl(api_get_index_applicaitons.path);
    let config = buildAxiosConfig(uri, api_get_index_applicaitons.method, null);
    const {data, error, isLoading}  = useSWR(config, axiosFetcher);
    console.log({
        name: "getApplicationList",
        uri: uri,
        response: data,
        error:error,
        isLoading:isLoading
    });
    return {data, error, isLoading}
}

export function useGetScreens(){
    let uri = makeUrl(api_get_index_screens.path);
    let config = buildAxiosConfig(uri, api_get_index_screens.method, null);
    const {data, error, isLoading} = useSWR(config, axiosFetcher);

    console.log({
        name: "getScreens",
        uri: uri,
        response: data,
        error:error,
        isLoading:isLoading
    });

    return {data, error, isLoading}
}

export function useGetSpotlight(){
    let uri = makeUrl(api_get_index_spotlight.path);
    let config = buildAxiosConfig(api_get_index_spotlight.method, null);
    const {data, error, isLoading} = useSWR(config, axiosFetcher);

    console.log({
        name: "getSpotlight",
        uri: uri,
        response: data,
        error:error,
        isLoading:isLoading
    });

    return {data, error, isLoading}
}




//MONGO_INITDB_ROOT_USERNAME=dev-ubuntu-3100 -e MONGO_INITDB_ROOT_PASSWORD=dev-ubuntu-3100pwd