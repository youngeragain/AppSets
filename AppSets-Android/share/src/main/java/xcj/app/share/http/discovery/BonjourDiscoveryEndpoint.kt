package xcj.app.share.http.discovery

import javax.jmdns.JmDNS

class BonjourDiscoveryEndpoint(jmDNS: JmDNS) : DiscoveryEndpoint {
    private val mEndpointHash = jmDNS.hashCode()
    override fun endpointHash(): Int {
        return mEndpointHash
    }

}