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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ferid.app.cleaner.adapter.ExplorerAdapter;
import com.ferid.app.cleaner.enums.SortingType;
import com.ferid.app.cleaner.listeners.CleaningListener;
import com.ferid.app.cleaner.listeners.SizeListener;
import com.ferid.app.cleaner.model.Explorer;
import com.ferid.app.cleaner.tasks.CleanFoldersTask;
import com.ferid.app.cleaner.tasks.GetFolderSizeTask;
import com.ferid.app.cleaner.utility.ExplorerUtility;
import com.ferid.app.cleaner.utility.PrefsUtil;
import com.ferid.app.cleaner.widget.CleanerWidget;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

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

    //listViewExplorer elements
    private ListView listViewExplorer;
    private ExplorerAdapter adapterExplorer;
    private ArrayList<Explorer> arrayListExplorer = new ArrayList<>();
    //fill all folders
    private ArrayList<Explorer> allPaths = new ArrayList<>();
    //used to save cleaning listViewExplorer
    private ArrayList<String> cleaningPaths = new ArrayList<>();

    //components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewSize;
    private FloatingActionButton actionButtonDelete;

    //async tasks
    private CleanFoldersTask cleanSelectedFoldersTask;
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

        listViewExplorer = findViewById(R.id.list);
        adapterExplorer = new ExplorerAdapter(context,
                R.layout.explorer_list_item, arrayListExplorer);

        //listViewExplorer item animations
        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter
                = new SwingBottomInAnimationAdapter(adapterExplorer);
        swingBottomInAnimationAdapter.setAbsListView(listViewExplorer);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);

        listViewExplorer.setAdapter(swingBottomInAnimationAdapter);

        setOnClickListeners();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }

    /**
     * Get all explorers.<br />
     * Update total folder size.<br />
     * Update widget.
     */
    private void refresh() {
        //get all explorers
        new GetExplorerList().execute();

        //update widget
        updateCleanerWidget();
    }

    /**
     * Set on click listeners
     */
    private void setOnClickListeners() {
        listViewExplorer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                    //update widget
                    updateCleanerWidget();
                }
            }
        });

        actionButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanSelectedFoldersTask = new CleanFoldersTask();
                cleanSelectedFoldersTask.setListener(new CleaningListener() {
                    @Override
                    public void OnCompleted() {
                        refresh();

                        Snackbar.make(listViewExplorer, getString(R.string.cleaned),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
                cleanSelectedFoldersTask.execute(cleaningPaths);
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
                if (file.isDirectory()) {
                    String[] partPath = file.getPath().split("0");
                    //exclude the root content
                    if (partPath.length > 1) {
                        //exclude hidden folders and files
                        if (PrefsUtil.isValidFolder(partPath[1])) {
                            String folderName = partPath[1].substring(1);
                            explorer = new Explorer();
                            explorer.setPath(folderName);
                            explorer.setSize(ExplorerUtility.getFileSize(file));

                            allPaths.add(explorer);
                        }
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

        updateCleanerWidget();
    }

    private class ChangeSorting extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //get current sorting type and convert it into the next one
            SortingType sortingType = PrefsUtil.getSortingType(context).next();

            //sort elements
            sortElements(sortingType);

            //save the next sorting type
            PrefsUtil.setSortingType(context, sortingType);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            arrayListExplorer.clear();
            arrayListExplorer.addAll(allPaths);

            adapterExplorer.notifyDataSetChanged();
        }
    }

    /**
     * Search through folders
     */
    private class GetExplorerList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            swipeRefreshLayout.setRefreshing(true);

            allPaths.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File root = PrefsUtil.getExplorerRootPath();
            if (root.exists()) {
                searchParentFolders(root.listFiles());

                //get sorting type and sort elements accordingly
                sortElements(PrefsUtil.getSortingType(context));

                //update allPaths according to cleaning paths
                getCleaningPaths();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            arrayListExplorer.clear();
            arrayListExplorer.addAll(allPaths);

            adapterExplorer.notifyDataSetChanged();

            //update total cleaning folder size
            getFolderSizeTask = new GetFolderSizeTask();
            getFolderSizeTask.setListener(sizeListener);
            getFolderSizeTask.execute(cleaningPaths);

            swipeRefreshLayout.setRefreshing(false);
        }
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
    public class ExplorerComparator implements Comparator<Explorer> {
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
    public class SizeComparator implements Comparator<Explorer> {

        @Override
        public int compare(Explorer e1, Explorer e2) {
            return Double.compare(e2.getSize(), e1.getSize());
        }
    }

    @Override
    protected void onDestroy() {
        //release listeners
        if (cleanSelectedFoldersTask != null) {
            cleanSelectedFoldersTask.setListener(null);
        }

        if (getFolderSizeTask != null) {
            getFolderSizeTask.setListener(null);
        }

        super.onDestroy();
    }

    /**
     * Update cleaner widget
     */
    private void updateCleanerWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), this.getClass().getName());
        Intent updateWidget = new Intent(context, CleanerWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        updateWidget.setAction(CleanerWidget.APP_TO_WID);
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateWidget);
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
                new ChangeSorting().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}