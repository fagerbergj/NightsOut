package com.example.jasonfagerberg.nightsout

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.widget.RelativeLayout


class HomeFragment : Fragment(), HomeFragmentRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private val TAG = "HomeFragment"
    private val mDrinkList: ArrayList<Drink> = ArrayList()
    private lateinit var mDrinkListAdapter: HomeFragmentDrinkListAdapter
    private lateinit var mRelativeLayout: RelativeLayout

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // inflate layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // set layout
        mRelativeLayout = view.findViewById(R.id.layout_home)

        // toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_home)
        toolbar.inflateMenu(R.menu.home_menu)

        // mDrinkList recycler view setup
        val drinksListView:RecyclerView = view.findViewById(R.id.recycler_drink_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        drinksListView.layoutManager = linearLayoutManager

        // todo remove test data
        for (i in 0..9){
            val drink = Drink(ByteArray(0), "This is an Example Drink #" + i.toString(),
                    i*10 + i + i.toDouble()/10, (i*10 + i + i.toDouble()/10), "oz")
            mDrinkList.add(drink)
        }

        // set adapter
        mDrinkListAdapter = HomeFragmentDrinkListAdapter(context!!, mDrinkList)
        //update list
        drinksListView.adapter = mDrinkListAdapter //Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mDrinkList.size - 1) //Nav to end of list
        
        // set up touch listener for recycler
        //Add touch listener for left swipe
        val itemTouchHelperCallback = HomeFragmentRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this@HomeFragment)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(drinksListView)
        
        
        // return
        return view
    }

    // create new fragment
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
    
    //swipe method 
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is HomeFragmentDrinkListAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            val name = mDrinkList[viewHolder.adapterPosition].name

            // backup of removed item for undo purpose
            val deletedItem = mDrinkList[viewHolder.adapterPosition]
            val deletedIndex = viewHolder.adapterPosition

            // remove the item from recycler view
            mDrinkListAdapter.removeItem(viewHolder.adapterPosition)

            // showing snack bar with Undo option
            val snackbar = Snackbar.make(mRelativeLayout, "$name removed from meal list!",
                    Snackbar.LENGTH_LONG)

            // undo is selected, restore the deleted item
            snackbar.setAction("UNDO", { _ ->
                mDrinkListAdapter.restoreItem(deletedItem, deletedIndex)
            })

            snackbar.setActionTextColor(Color.YELLOW)
            snackbar.show()
        }
    }
}
