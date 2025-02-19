'use client'

import {useState} from "react";
import Image from "next/image";

function LoginPanel({onNavBarItemClick, onBackClick}){
    const [account, setAccount] = useState("")
    const [password, setPassword] = useState("")
    function onLoginClick(){
        let md5 = require("md5")
        let accountMd5 = md5(account)
        let passwordMd5 = md5(password)
        let temp = {
            "account":accountMd5,
            "password":passwordMd5
        }
        console.log(temp)
    }
    let backIconUrl = "/arrow_back_ios_new_24dp_E8EAED_FILL0_wght100_GRAD0_opsz24.svg"
    let backIconView = 
    (  
         <button onClick={()=>{
            onBackClick()
            }}> 
            <div className="border rounded-3xl p-2">
                <Image priority={true} className="object-cover rounded-3xl w-6 h-6" width={24} height={24} alt={"back icon"} src={backIconUrl}/>
            </div>
        </button>
    
    )
    return(
        <div className="flex flex-col h-full items-start mx-6 py-4">
            <div className="flex flex-row">
                <p className="mt-3 ml-2 py-2 px-2">注册</p>
                <p className="mt-3 ml-2 py-2 px-2">二维码登录</p>
                <p className="mt-3 ml-2 py-2 px-2">扫描二维码</p>
            </div>
            <p className="text-9xl font-bold mt-5 ml-2">登录</p>
            <div className="h-full"></div>
            <div className="flex flex-col items-center place-items-center w-full">
                <input id="account" className="border rounded-full mt-3 ml-2 p-3 text-sm w-full" placeholder="账号" value={account} onInput={(ele)=>{
                    setAccount(ele.target.value);
                }}/>
                <input type="password" className="border rounded-full mt-3 ml-2 p-3 text-sm w-full" placeholder="密码" value={password} onInput={(ele)=>{
                    setPassword(ele.target.value)
                }}/>
                <button className="mt-3 ml-2 py-3 px-4 bg-blue-100 border rounded-full text-xs w-full" onClick={onLoginClick}>确定</button>
                <div className="p-2"></div>
                {backIconView}
            </div>
            
        </div>
        
        );
}

export default LoginPanel