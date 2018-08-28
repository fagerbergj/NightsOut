package com.example.jasonfagerberg.nightsout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.support.v7.widget.LinearLayoutManager



class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private val drinks: ArrayList<Drink> = ArrayList()

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbarHome)
        toolbar.inflateMenu(R.menu.home_menu)

        // drinks recycler view setup
        val drinksListView:RecyclerView = view.findViewById(R.id.recyclerDrinkList)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        drinksListView.layoutManager = linearLayoutManager

        // todo remove test data
        for (i in 0..9){
            val drink = Drink(ByteArray(0), "This is an Example Drink #" + i.toString(),
                    i*10 + i + i.toDouble()/10, (i*10 + i + i.toDouble()/10), "oz")
            drinks.add(drink)
        }

        val adapter = HomeFragmentDrinkListAdapter(context!!, drinks) //object to update fragment
        //update list
        drinksListView.adapter = adapter //Update display with new list
        //drinksListView.layoutManager!!.scrollToPosition(drinks.size - 1) //Nav to end of list


        // return
        return view
    }

    // create new fragment
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
