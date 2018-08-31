package com.example.jasonfagerberg.nightsout

import android.graphics.Color
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.Toast


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

        // main
        val main = (activity as AppCompatActivity)

        //toolbar setup
        val toolbar:android.support.v7.widget.Toolbar = view!!.findViewById(R.id.toolbar_home)
        toolbar.inflateMenu(R.menu.home_menu)
        main.setSupportActionBar(toolbar)
        main.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)

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
        // Add touch listener for left swipe
        val itemTouchHelperCallback = HomeFragmentRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this@HomeFragment)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(drinksListView)

        // add a drink button setup
        val btnAdd: MaterialButton = view.findViewById(R.id.btn_home_add_drink)
        btnAdd.setOnClickListener{ _ ->
            val mainActivity: MainActivity = context as MainActivity
            mainActivity.setFragment(mainActivity.addDrinkFragment)
        }

        // setup bottom nav bar
        val mainActivity: MainActivity = context as MainActivity
        val botNavBar: BottomNavigationView = mainActivity.findViewById(R.id.bottom_navigation_view)
        botNavBar.visibility = View.VISIBLE
        botNavBar.selectedItemId = R.id.bottom_nav_home

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ABOVE, R.id.bottom_navigation_view)
        (mainActivity.findViewById(R.id.main_frame) as FrameLayout).layoutParams = params

        // return
        return view
    }

    // create new fragment
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        if(resId == R.id.btn_toolbar_home_done){
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        //Yes is pressed
                        val toast = Toast.makeText(activity!!.applicationContext,
                                "Session Logged ", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {/* no action for clicking no */ }
                }
            }
            //Build Actual box
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage("Log Session?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
        }
        return true
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
            val undo = { _:View ->
                mDrinkListAdapter.restoreItem(deletedItem, deletedIndex)
            }
            snackbar.setAction("UNDO", undo)

            snackbar.setActionTextColor(Color.YELLOW)
            snackbar.show()
        }
    }
}
