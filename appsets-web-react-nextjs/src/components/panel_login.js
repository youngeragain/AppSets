'use client'

import {useState} from "react";

function LoginPanel(){
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
    return(
        <div className="flex flex-col h-full items-start ml-6">
            <button className="mt-3 ml-2 py-2 px-4 bg-blue-600 border rounded-lg text-xs text-white">注册</button>
            <button className="mt-3 ml-2 py-2 px-4 bg-blue-600 border rounded-lg text-xs text-white">二维码登录</button>
            <button className="mt-3 ml-2 py-2 px-4 bg-blue-600 border rounded-lg text-xs text-white">扫描二维码</button>
            <button className="mt-3 ml-2 py-2 px-4 bg-blue-600 border rounded-lg text-xs text-white">验证码登录</button>
            <span className="w-60 border rounded-lg mt-5 ml-2 p-2">
                <p className="font-bold">提示</p>
                <p className="mt-1 text-xs">AppSets为你提供类似应用商店，社交，聊天等功能，开发版无法保证你账号的数据和隐私安全<br/>* 注册时使用消息摘要算法对账号密码处理的情况，需要以同等方式处理后再填入输入框</p>
            </span>
            <p className="text-2xl font-bold mt-5 ml-2">登录</p>
            <input id="input_account" className="border rounded mt-3 ml-2 p-2 text-sm w-60 h-8" placeholder="账号" width={260} height={32} value={account} onInput={(ele)=>{
                setAccount(ele.target.value);
            }}/>
            <input type="password" className="border rounded mt-3 ml-2 p-2 text-sm w-60 h-8" placeholder="密码" width={260} height={32} value={password} onInput={(ele)=>{
                setPassword(ele.target.value)
            }}/>
            <button className="mt-3 ml-2 py-2 px-4 bg-gray-100 border rounded text-xs text-black" onClick={onLoginClick}>确定</button>
        </div>
        
        );
}

export default LoginPanel