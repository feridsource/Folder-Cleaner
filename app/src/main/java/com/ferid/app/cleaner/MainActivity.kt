/*
 * Copyright (C) 2016 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ferid.app.cleaner

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferid.app.cleaner.adapter.ExplorerAdapter
import com.ferid.app.cleaner.enums.SortingType
import com.ferid.app.cleaner.listeners.CleaningListener
import com.ferid.app.cleaner.listeners.SizeListener
import com.ferid.app.cleaner.model.Explorer
import com.ferid.app.cleaner.tasks.CleanFoldersTask
import com.ferid.app.cleaner.tasks.GetFolderSizeTask
import com.ferid.app.cleaner.utility.ExplorerUtility.getFileSize
import com.ferid.app.cleaner.utility.PrefsUtility.getDecimalFormat
import com.ferid.app.cleaner.utility.PrefsUtility.getExplorerRootPath
import com.ferid.app.cleaner.utility.PrefsUtility.getSortingType
import com.ferid.app.cleaner.utility.PrefsUtility.isValidFolder
import com.ferid.app.cleaner.utility.PrefsUtility.setSortingType
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.text.Collator
import java.util.*


class MainActivity : AppCompatActivity() {
    private var context: Context? = null

    //explorer list elements
    private var adapterExplorer: ExplorerAdapter? = null
    private val arrayListExplorer = ArrayList<Explorer>()

    //fill all folders
    private val allPaths: ArrayList<Explorer>? = ArrayList()

    //used to save cleaning explorer list
    private val cleaningPaths = ArrayList<String>()

    //async tasks
    private var cleanFoldersTask: CleanFoldersTask? = null
    private var getFolderSizeTask: GetFolderSizeTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        setSupportActionBar(toolbar)

        //list
        adapterExplorer = ExplorerAdapter(arrayListExplorer) {
            it.isToClean = !it.isToClean
            setCleaningPaths()
            adapterExplorer!!.notifyDataSetChanged()

            //update total folder size
            getFolderSizeTask = GetFolderSizeTask{}
            getFolderSizeTask!!.setListener(sizeListener)
            getFolderSizeTask!!.execute(cleaningPaths)
        }
        listExplorer.adapter = adapterExplorer
        listExplorer.layoutManager = LinearLayoutManager(context)
        listExplorer.setHasFixedSize(true)

        setOnClickListener()

        explorerList()
    }

    /**
     * Set delete button on click listener
     */
    private fun setOnClickListener() {
        actionButtonDelete!!.setOnClickListener {
            if (cleaningPaths.isNotEmpty()) {
                cleanFoldersTask = CleanFoldersTask{}
                cleanFoldersTask!!.setListener(object : CleaningListener {
                    override fun onCompleted() {
                        explorerList()

                        Snackbar.make(listExplorer!!, getString(R.string.cleaned),
                                Snackbar.LENGTH_SHORT).show()
                    }
                })
                cleanFoldersTask!!.execute(cleaningPaths)
            } else {
                Snackbar.make(listExplorer!!, getString(R.string.nothing_to_clean),
                        Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Search only after root directory
     */
    private fun searchParentFolders(files: Array<File>) {
        var explorer: Explorer
        for (file in files) {
            if (file.exists()) {
                val partPath = file.path.split("/0/".toRegex()).toTypedArray()
                //exclude the root content
                if (partPath.size > 1) {
                    val fileName = partPath[1]

                    //exclude exceptional files and folders
                    if (isValidFolder(fileName)) {
                        explorer = Explorer()
                        explorer.path = fileName
                        explorer.isHidden = fileName.startsWith(".") //hidden files and folders
                        explorer.isDirectory = file.isDirectory //file or folder
                        explorer.size = getFileSize(file)
                        allPaths!!.add(explorer)
                    }
                }
            }
        }
    }

    /**
     * Pick cleaning items from the allPaths and save it
     */
    private fun setCleaningPaths() {
        cleaningPaths.clear()
        for (explorer in arrayListExplorer) {
            if (explorer.isToClean) {
                cleaningPaths.add(explorer.path)
            }
        }
    }

    /**
     * Clear all selections/Deselect all
     */
    private fun deselectAll() {
        cleaningPaths.clear()
        for (explorer in arrayListExplorer) {
            explorer.isToClean = false
        }
        adapterExplorer!!.notifyDataSetChanged()
        getFolderSizeTask = GetFolderSizeTask{}
        getFolderSizeTask!!.setListener(sizeListener)
        getFolderSizeTask!!.execute(cleaningPaths)
    }

    /**
     * Change sorting and refresh list
     */
    private fun changeSorting() {
        //get current sorting type and convert it into the next one
        val sortingType = getSortingType(context!!)!!.next()

        //sort elements
        sortElements(sortingType)

        //save the next sorting type
        setSortingType(context!!, sortingType)
        arrayListExplorer.clear()
        arrayListExplorer.addAll(allPaths!!)
        adapterExplorer!!.notifyDataSetChanged()
    }//get sorting type and sort elements accordingly

    /**
     * Search through folders
     */
    private fun explorerList() {
        Handler().post {
            Handler().post { actionButtonDelete.hide() }
            allPaths!!.clear()
            val root = getExplorerRootPath()
            if (root!!.exists()) {
                searchParentFolders(root.listFiles())

                //get sorting type and sort elements accordingly
                sortElements(getSortingType(context!!))
            }
            arrayListExplorer.clear()
            arrayListExplorer.addAll(allPaths)
            adapterExplorer!!.notifyDataSetChanged()

            //update total cleaning folder size
            getFolderSizeTask = GetFolderSizeTask{}
            getFolderSizeTask!!.setListener(sizeListener)
            getFolderSizeTask!!.execute(cleaningPaths)
            Handler().postDelayed({ actionButtonDelete.show() }, 800)
        }
    }

    /**
     * Sort elements with given sorting type
     * @param sortingType SortingType
     */
    private fun sortElements(sortingType: SortingType?) {
        if (allPaths != null) {
            if (sortingType === SortingType.ALPHABET) {
                Collections.sort(allPaths, ExplorerComparator())
            } else if (sortingType === SortingType.SIZE) {
                Collections.sort(allPaths, SizeComparator())
            }
        }
    }

    /**
     * Folder size retrieving listener
     */
    private val sizeListener: SizeListener = object : SizeListener {
        override fun onResult(sum: Double) {
            supportActionBar?.apply {
                // Set toolbar title/app title
                title = getDecimalFormat(context!!, sum)
            }
        }
    }

    /**
     * Alphabetical order
     */
    internal class ExplorerComparator : Comparator<Explorer> {
        //prepare utf-8
        var collator = Collator.getInstance(Locale("UTF-8"))
        override fun compare(e1: Explorer, e2: Explorer): Int {
            return collator.compare(e1.path, e2.path)
        }
    }

    /**
     * Size order
     */
    internal class SizeComparator : Comparator<Explorer> {
        override fun compare(e1: Explorer, e2: Explorer): Int {
            return e2.size.compareTo(e1.size)
        }
    }

    override fun onDestroy() {
        //release listeners
        if (cleanFoldersTask != null) {
            cleanFoldersTask!!.setListener(null)
        }
        if (getFolderSizeTask != null) {
            getFolderSizeTask!!.setListener(null)
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar actions click
        return when (item.itemId) {
            R.id.menu_deselect_all -> {
                deselectAll()
                true
            }
            R.id.menu_change_sorting -> {
                changeSorting()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}