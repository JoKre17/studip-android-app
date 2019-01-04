package de.kriegel.studip.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import timber.log.Timber
import java.net.URL

private val item = android.R.layout.simple_spinner_item

class ServerPairSpinnerAdapter(context: Context, objects: List<CustomServerPairWrapper>) :
    ArrayAdapter<CustomServerPairWrapper>(context, item, objects) {

    private var values: List<CustomServerPairWrapper>
    private var textViewResourceId: Int

    init {
        values = objects
        this.textViewResourceId = item

        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sort { customServerPairWrapper1, customServerPairWrapper2 ->
            customServerPairWrapper1.serverPair.first.compareTo(
                customServerPairWrapper2.serverPair.first
            )
        }
    }

    override fun getItem(position: Int): CustomServerPairWrapper? {
        return values.get(position)
    }

}