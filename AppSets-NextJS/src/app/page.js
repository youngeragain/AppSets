'use client'
import ScaffoldContainer, { RouteData } from "@/components/scaffold_container_view";
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'

function IndexPageWrapper(){
    const searchParams = useSearchParams()
    const routeName = searchParams.get('route')
    let routeData = routeName?new RouteData(routeName, null):new RouteData("applications", null)
    return (
        <main className="flex flex-col min-h-screen items-center justify-between w-full py-4">
            <ScaffoldContainer routeDataDefault={ routeData }/>
        </main>
    );
}

export default function IndexPage(){
    
    return (
        <Suspense>
            <IndexPageWrapper />
        </Suspense>
    );
}
