'use client'
import Splitview from "@/components/splitview";
import { useSearchParams } from 'next/navigation'

export default function IndexPage(){
    const searchParams = useSearchParams()
    const routeName = searchParams.get('route')
    return (
        <main className="flex flex-col min-h-screen items-center justify-between w-full px-12 py-12">
            <Splitview routeNameDefault={ routeName }/>
        </main>
    );
}
