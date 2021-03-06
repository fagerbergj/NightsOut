package com.wit.jasonfagerberg.nightsout.manageDB

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.databaseHelper.AddDrinkDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.constants.Constants
import com.wit.jasonfagerberg.nightsout.models.Drink
import com.wit.jasonfagerberg.nightsout.main.NightsOutActivity
import androidx.appcompat.widget.SearchView

class ManageDBActivity : NightsOutActivity() {
    private lateinit var mDrinkListAdapter: ManageDBDrinkListAdapter
    lateinit var mDrinksList: ArrayList<Drink>
    lateinit var dbh: AddDrinkDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_db)
        dbh = AddDrinkDatabaseHelper(this, Constants.DB_NAME, null, Constants.DB_VERSION)
        dbh.openDatabase()
        mDrinksList = dbh.getSuggestedDrinks("", true)
    }

    override fun onStart() {
        setupToolbar()
        setupRecycler()
        super.onStart()
    }

    override fun onPause() {
        dbh.closeDatabase()
        super.onPause()
    }

    private fun setupToolbar() {
        supportActionBar?.title = "Manage Database"
        // adds back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back_white_24dp)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manage_db_menu, menu)
        val searchItem = menu!!.findItem(R.id.btn_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                // your text view here
                mDrinksList.clear()
                mDrinksList.addAll(dbh.getSuggestedDrinks(newText, true))
                mDrinkListAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_reset_db -> {
                val dialog = LightSimpleDialog(this)
                val posAction = {
                    dbh.copyDatabase()
                    dbh.pullCurrentSessionDrinks()
                    dbh.pullLogHeaders()

                    mDrinksList.clear()
                    mDrinksList.addAll(dbh.getSuggestedDrinks("", true))
                    mDrinkListAdapter.notifyDataSetChanged()
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

                }
                dialog.setActions(posAction, {})
                dialog.show("Are you sure? You will lose everything.")
            }
            R.id.btn_clean_db -> {
                val dialog = LightSimpleDialog(this)
                dialog.setActions({ deleteDrinksWithNoReference() }, {})
                dialog.show("Are you sure you want to clean your database? This will permanently delete all drinks:" +
                        "\n    Not Currently in Use\n    Not in Favorited\n    Not Recently Used\n    Not Logged")
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteDrinksWithNoReference() {
        var size = mDrinksList.size
        var offset = 0
        for (position in 0 until size) {
            val drink = mDrinksList[position - offset]
            val loss = mDrinkListAdapter.getLostReferenceString(drink)
            if (loss.isEmpty()) {
                dbh.deleteRowsInTable("drinks", "id = \"${drink.id}\"")
                mDrinksList.removeAt(position - offset)
                mDrinkListAdapter.notifyItemRemoved(position - offset)
                mDrinkListAdapter.notifyItemRangeChanged(position - offset, mDrinksList.size)
                size --
                offset ++
            }
        }
        showToast("$offset drinks deleted from database")
    }

    private fun setupRecycler() {
        // mDrinkList recycler view setup
        val drinksListView: RecyclerView = findViewById(R.id.recycler_manage_db_list)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        drinksListView.layoutManager = linearLayoutManager

        val itemDecor = DividerItemDecoration(drinksListView.context, DividerItemDecoration.VERTICAL)
        drinksListView.addItemDecoration(itemDecor)

        // set adapter
        mDrinkListAdapter = ManageDBDrinkListAdapter(this, mDrinksList)
        // update list
        drinksListView.adapter = mDrinkListAdapter // Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mDrinksList.size - 1) // Nav to end of list
    }
}
