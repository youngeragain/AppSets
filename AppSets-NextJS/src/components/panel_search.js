'use client'
import Image from "next/image";
import axios from "axios";
import {buildAxiosConfig} from "@/components/utils/http";
import BackActionBar from "@/components/back_action_bar";
import { RouteData } from "@/components/scaffold_container_view";

function SearchPanel({onBackClick}){
    return(
        <div className="flex flex-col p-2 w-full">
            <SearchBar onBackClick = {onBackClick}/>
        </div>
        );
}


function SearchBar({onBackClick}){
    let item = {
        id:4,
        src:"/search_FILL0_wght400_GRAD0_opsz48.svg",
        name:"search",
        namezh:"搜索"
    }
    let backIconUrl = "/arrow_back_FILL0_wght400_GRAD0_opsz40.svg"
     let backIconView = 
        (<button onClick={()=>{
                onBackClick()
            }}>
            <div>
                <Image priority={true} className="object-cover rounded-3xl w-6 h-6" width={24} height={24} alt={"back icon"} src={backIconUrl}/>
            </div>
        </button>
        )
    return (
        <div className="relative w-full">
             <div className="flex flex-row  absolute inset-y-0 left-0 px-3 py-2 place-item-center">
                {backIconView}
             </div>
            <input className="border rounded-full px-12 text-sm w-full h-12 w-full" placeholder={item.namezh} onInput={(ele)=>{
                    
            }}/>
            <div className="flex flex-row absolute inset-y-0 right-0 px-3 py-2 place-item-center">
                <Image
                    src={item.src}
                    alt={item.name}
                    width={24}
                    height={24}
                    priority = {true}
                    />
             </div>

            
        </div>
    );
}

export default SearchPanel