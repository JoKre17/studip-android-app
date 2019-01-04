package de.kriegel.studip.login

import android.content.Context
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI

class ServerService(context: Context) {

    val STUDIP_SERVER_ADDRESSES_FILE = "studipServerAddresses.conf"

    lateinit var allServers: List<CustomServerPairWrapper>

    init {
        loadServersFromAssetsFile(context)
    }

    private fun loadServersFromAssetsFile(context: Context) {

        Timber.d("Loading server addresses from assets file " + STUDIP_SERVER_ADDRESSES_FILE)
        /*
        context.resources.assets.list("").forEach {
            Timber.i(it)
        }
        */

        var studipServerAddressesFileContent = readAssetsFileContent(STUDIP_SERVER_ADDRESSES_FILE, context)
        var lines = studipServerAddressesFileContent.split("\n")

        var allServersMutableList = emptyList<CustomServerPairWrapper>().toMutableList()

        lines.forEach {
            if (it.startsWith("#")) {
                Timber.d("Skipping " + it)
                return@forEach
            }

            var serverSplit = it.split("=")

            if (serverSplit.size != 2) {
                Timber.e("Could not load server from config: " + it)
                return@forEach
            }

            var serverName = serverSplit.get(0).trim()
            var serverAddress = URI(serverSplit.get(1).trim())

            Timber.d("Name: $serverName Address: $serverAddress")

            allServersMutableList.add(CustomServerPairWrapper(serverName, serverAddress))
        }

        allServers = allServersMutableList.toList()

        allServers.forEach {
            Timber.d("${it.serverPair.first} => ${it.serverPair.second}")
        }

        Timber.i("Loaded ${allServers.size} server addresses")
    }

    private fun readAssetsFileContent(filename: String, context: Context): String {
        Timber.d("Loading assets file " + filename)
        val iS = context.resources.assets.open(STUDIP_SERVER_ADDRESSES_FILE)

        //create a buffer that has the same size as the InputStream
        val buffer = ByteArray(iS.available())
        //read the text file as a stream, into the buffer
        iS.read(buffer)
        //create a output stream to write the buffer into
        val oS = ByteArrayOutputStream()
        //write this buffer to the output stream
        oS.write(buffer)
        //Close the Input and Output streams
        oS.close()
        iS.close()

        return oS.toString()
    }

    private fun printFilesRecursive(file: File) {
        Timber.i(file.absolutePath)

        file.listFiles().forEach {
            if (it.isDirectory) {
                printFilesRecursive(it)
            } else {
                Timber.i(it.absolutePath)
            }
        }
    }

}