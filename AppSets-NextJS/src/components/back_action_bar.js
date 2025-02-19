
import Image from 'next/image';
import {RouteData} from '@/components/scaffold_container_view.js'; 

function BackActionBar({backText, onBackClick}){
    let backIconUrl = "/arrow_back_FILL0_wght400_GRAD0_opsz40.svg"
    let backIconView = 
    (<button onClick={()=>{
            onBackClick()
        }}>
        <div className="p-2">
            <Image priority={true} className="object-cover rounded-3xl w-6 h-6" width={24} height={24} alt={"back icon"} src={backIconUrl}/>
        </div>
    </button>
    )

    var backTextView = (<></>)
    if(backText!=null){
        backTextView = 
        (
           <p>{backText}</p>
        )
    }

    return(
        <div className="flex flex-col w-full">
             <div className="flex flex-row w-full place-items-center">
                {backIconView}
                {backTextView}
            </div>
        </div>
        );
}

export default BackActionBar