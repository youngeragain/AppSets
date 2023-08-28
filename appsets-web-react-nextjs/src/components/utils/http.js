import axios from "axios";
import https from 'https';

const agent = new https.Agent({
    rejectUnauthorized: false
});
export const appToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJrZXkiOiJBUFBTRVRTMjAyMzA3MTU3OTAxOTg4MDMzODUyOSJ9.6mwsBLMAiG02T_U-klbqpHMmNBgJPdlIIsK2kc-LAa0";
export const version = 200;

const baseHeaders = {
    "Content-Type": "application/json;charset=UTF-8",
    "Access-Control-Allow-Origin": "*",
    "b3c6f3a2140f316c": appToken,
    "0c356273d46284f6": version.toString()
};



export const axiosFetcher = (url, requestConfig) => axios(url, requestConfig).then(res => res.data).catch((e)=>{
    console.log("axiosFetcher exception:"+e)
})

export const axiosFetcherAwait = (url, requestConfig) => axios(url, requestConfig);

export function buildAxiosConfig(requestMethod, newHeaders){
    let requestHeaders = baseHeaders;
    if(newHeaders!=null){
        requestHeaders = Object.assign(requestHeaders, newHeaders);
    }
    
    let requestConfig = {
        method: requestMethod,
        headers: requestHeaders,
        httpsAgent: agent
        //mode: "cors",
    };
    return requestConfig;
}