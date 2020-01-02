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

package com.ferid.app.cleaner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ferid.app.cleaner.adapter.ExplorerAdapter;
import com.ferid.app.cleaner.enums.SortingType;
import com.ferid.app.cleaner.listeners.AdapterListener;
import com.ferid.app.cleaner.listeners.CleaningListener;
import com.ferid.app.cleaner.listeners.SizeListener;
import com.ferid.app.cleaner.model.Explorer;
import com.ferid.app.cleaner.tasks.CleanFoldersTask;
import com.ferid.app.cleaner.tasks.GetFolderSizeTask;
import com.ferid.app.cleaner.utility.ExplorerUtility;
import com.ferid.app.cleaner.utility.PrefsUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Ferid Cafer on 11/16/2015.
 */
public class MainActivity extends AppCompatActivity {

    private Context context;

    //explorer list elements
    private ExplorerAdapter adapterExplorer;
    private ArrayList<Explorer> arrayListExplorer = new ArrayList<>();
    //fill all folders
    private ArrayList<Explorer> allPaths = new ArrayList<>();
    //used to save cleaning explorer list
    private ArrayList<String> cleaningPaths = new ArrayList<>();

    //components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewSize;
    private FloatingActionButton actionButtonDelete;

    //async tasks
    private CleanFoldersTask cleanFoldersTask;
    private GetFolderSizeTask getFolderSizeTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewSize = findViewById(R.id.textViewSize);
        actionButtonDelete = findViewById(R.id.actionButtonDelete);

        //list
        RecyclerView listExplorer = findViewById(R.id.listExplorer);
        adapterExplorer = new ExplorerAdapter(context, arrayListExplorer);
        listExplorer.setAdapter(adapterExplorer);
        listExplorer.setLayoutManager(new LinearLayoutManager(context));
        listExplorer.setHasFixedSize(true);

        setOnClickListeners();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getExplorerList();
            }
        });

        getExplorerList();
    }

    /**
     * Set on click listeners
     */
    private void setOnClickListeners() {
        adapterExplorer.setAdapterClickListener(new AdapterListener() {
            @Override
            public void OnItemClick(int position) {
                if (arrayListExplorer.size() > position) {
                    Explorer explorer = arrayListExplorer.get(position);

                    boolean isToClean = !explorer.isToClean();
                    arrayListExplorer.get(position).setToClean(isToClean);

                    setCleaningPaths();

                    adapterExplorer.notifyDataSetChanged();

                    //update total folder size
                    getFolderSizeTask = new GetFolderSizeTask();
                    getFolderSizeTask.setListener(sizeListener);
                    getFolderSizeTask.execute(cleaningPaths);
                }
            }
        });

        actionButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cleaningPaths.isEmpty()) {
                    cleanFoldersTask = new CleanFoldersTask();
                    cleanFoldersTask.setListener(new CleaningListener() {
                        @Override
                        public void OnCompleted() {
                            getExplorerList();

                            Snackbar.make(swipeRefreshLayout, getString(R.string.cleaned),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                    cleanFoldersTask.execute(cleaningPaths);
                } else {
                    Snackbar.make(swipeRefreshLayout, getString(R.string.nothing_to_clean),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Search only after root directory
     */
    private void searchParentFolders(final File[] files) {
        Explorer explorer;

        for (File file : files) {
            if (file.exists()) {
                    String[] partPath = file.getPath().split("/0/");
                    //exclude the root content
                    if (partPath.length > 1) {
                        String fileName = partPath[1];

                        //exclude exceptional files and folders
                        if (PrefsUtil.isValidFolder(fileName)) {
                            explorer = new Explorer();
                            explorer.setPath(fileName);
                            explorer.setHidden(fileName.startsWith(".")); //hidden files and folders
                            explorer.setDirectory(file.isDirectory()); //file or folder
                            explorer.setSize(ExplorerUtility.getFileSize(file));

                            allPaths.add(explorer);
                        }
                    }
            }
        }
    }

    /**
     * Pick cleaning items from the allPaths and save it
     */
    private void setCleaningPaths() {
        cleaningPaths.clear();

        for (Explorer explorer : arrayListExplorer) {
            if (explorer.isToClean()) {
                cleaningPaths.add(explorer.getPath());
            }
        }

        PrefsUtil.writeCleaningList(context, cleaningPaths);
    }

    /**
     * Get (read) cleaning paths and set into allPaths
     */
    private void getCleaningPaths() {
        cleaningPaths.clear();
        cleaningPaths.addAll(PrefsUtil.readCleaningList(context));

        for (int i = 0; i < allPaths.size(); i++) {
            Explorer explorer = allPaths.get(i);
            for (String cleanigPath : cleaningPaths) {
                if (explorer.getPath().equals(cleanigPath)) {
                    allPaths.get(i).setToClean(true);
                }
            }
        }
    }

    /**
     * Clear all selections/Deselect all
     */
    private void deselectAll() {
        cleaningPaths.clear();
        PrefsUtil.writeCleaningList(context, cleaningPaths);

        for (Explorer explorer : arrayListExplorer) {
            explorer.setToClean(false);
        }

        adapterExplorer.notifyDataSetChanged();

        getFolderSizeTask = new GetFolderSizeTask();
        getFolderSizeTask.setListener(sizeListener);
        getFolderSizeTask.execute(cleaningPaths);
    }

    /**
     * Change sorting and refresh list
     */
    private void changeSorting() {
        //get current sorting type and convert it into the next one
        SortingType sortingType = PrefsUtil.getSortingType(context).next();

        //sort elements
        sortElements(sortingType);

        //save the next sorting type
        PrefsUtil.setSortingType(context, sortingType);

        arrayListExplorer.clear();
        arrayListExplorer.addAll(allPaths);

        adapterExplorer.notifyDataSetChanged();
    }

    /**
     * Search through folders
     */
    private void getExplorerList() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        actionButtonDelete.hide();
                    }
                });

                swipeRefreshLayout.setRefreshing(true);

                allPaths.clear();

                File root = PrefsUtil.getExplorerRootPath();
                if (root.exists()) {
                    searchParentFolders(root.listFiles());

                    //get sorting type and sort elements accordingly
                    sortElements(PrefsUtil.getSortingType(context));

                    //update allPaths according to cleaning paths
                    getCleaningPaths();
                }

                arrayListExplorer.clear();
                arrayListExplorer.addAll(allPaths);

                adapterExplorer.notifyDataSetChanged();

                //update total cleaning folder size
                getFolderSizeTask = new GetFolderSizeTask();
                getFolderSizeTask.setListener(sizeListener);
                getFolderSizeTask.execute(cleaningPaths);

                swipeRefreshLayout.setRefreshing(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        actionButtonDelete.show();
                    }
                }, 800);
            }
        });
    }

    /**
     * Sort elements with given sorting type
     * @param sortingType SortingType
     */
    private void sortElements(SortingType sortingType) {
        if (allPaths != null) {
            if (sortingType == SortingType.ALPHABET) {
                Collections.sort(allPaths, new ExplorerComparator());
            } else if (sortingType == SortingType.SIZE) {
                Collections.sort(allPaths, new SizeComparator());
            }
        }
    }

    /**
     * Folder size retrieving listener
     */
    private SizeListener sizeListener = new SizeListener() {
        @Override
        public void OnResult(double sum) {
            textViewSize.setText(PrefsUtil.getDecimalFormat(context, sum));
        }
    };

    /**
     * Alphabetical order
     */
    class ExplorerComparator implements Comparator<Explorer> {
        //prepare utf-8
        Collator collator = Collator.getInstance(new Locale("UTF-8"));

        @Override
        public int compare(Explorer e1, Explorer e2) {
            return collator.compare(e1.getPath(), e2.getPath());
        }
    }

    /**
     * Size order
     */
    class SizeComparator implements Comparator<Explorer> {

        @Override
        public int compare(Explorer e1, Explorer e2) {
            return Double.compare(e2.getSize(), e1.getSize());
        }
    }

    @Override
    protected void onDestroy() {
        //release listeners
        if (cleanFoldersTask != null) {
            cleanFoldersTask.setListener(null);
        }

        if (getFolderSizeTask != null) {
            getFolderSizeTask.setListener(null);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.menu_deselect_all:
                deselectAll();
                return true;
            case R.id.menu_change_sorting:
                changeSorting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}