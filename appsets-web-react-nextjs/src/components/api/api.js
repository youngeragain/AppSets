import {axiosFetcher, buildAxiosConfig} from "@/components/utils/http";
import useSWRImmutable from "swr/immutable";

export const api_base_url = "https://localhost:8084/";

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

export function useGetApplicationList(){
    let requestConfig = buildAxiosConfig(api_get_index_applicaitons.method, null);
    let uri = api_base_url+api_get_index_applicaitons.path;
    const {data, error, isLoading}  = useSWRImmutable([uri, requestConfig], ([uri, config])=>axiosFetcher(uri, config));
    console.log({
        name: "getApplicationList",
        response: data
    });
    return {data, error, isLoading}
}

export function useGetScreens(){
    let uri = api_base_url+api_get_index_screens.path;
    let requestConfig = buildAxiosConfig(api_get_index_screens.method);
    const {data, error, isLoading} = useSWRImmutable([uri, requestConfig], ([uri, config])=>axiosFetcher(uri, config));

    console.log({
        name: "getScreens",
        response: data
    });

    return {data, error, isLoading}
}

export function useGetSpotlight(){
    let requestConfig = buildAxiosConfig(api_get_index_spotlight.method, null);
    let uri = api_base_url+api_get_index_spotlight.path;
    const {data, error, isLoading} = useSWRImmutable([uri, requestConfig], ([uri, config])=>axiosFetcher(uri, config));

    console.log({
        name: "getSpotlight",
        response: data
    });

    return {data, error, isLoading}
}