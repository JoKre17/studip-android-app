package de.kriegel.studip.login

import java.net.URI

class CustomServerPairWrapper(name: String, address: URI) {

    val serverPair: Pair<String, URI>

    init {
        serverPair = Pair(name, address)
    }

    override fun toString(): String {
        return serverPair.first
    }

}