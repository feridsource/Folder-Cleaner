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

package com.ferid.app.cleaner.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ferid.app.cleaner.R;
import com.ferid.app.cleaner.SplashActivity;
import com.ferid.app.cleaner.listeners.CleaningListener;
import com.ferid.app.cleaner.listeners.SizeListener;
import com.ferid.app.cleaner.tasks.CleanFoldersTask;
import com.ferid.app.cleaner.tasks.GetFolderSizeTask;
import com.ferid.app.cleaner.utility.PrefsUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Ferid Cafer on 11/16/2015.
 */
public class CleanerWidget extends AppWidgetProvider {
    private Context context;

    protected RemoteViews remoteViews;
    protected AppWidgetManager appWidgetManager;
    protected ComponentName thisWidget;

    //application triggers the widget
    public static final String APP_TO_WID = "com.ferid.app.cleaner.widget.APP_TO_WID";
    public static final String WID_CLICKED = "com.ferid.app.cleaner.widget.WID_CLICKED";
    public static final String WIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";

    //tasks
    private CleanFoldersTask cleanFoldersTask;
    private GetFolderSizeTask getFolderSizeTask;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_cleaner);
        thisWidget = new ComponentName(context, CleanerWidget.class);

        getFolderSize();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        this.appWidgetManager = appWidgetManager;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_cleaner);
        thisWidget = new ComponentName(context, CleanerWidget.class);

        if (remoteViews != null) {
            if (intent.getAction().equals(APP_TO_WID)
                    || intent.getAction().equals(WIDGET_ENABLED)) {
                getFolderSize();
            } else if (intent.getAction().equals(WID_CLICKED)) {
                cleanSelectedFolders();
            }

            setOnClickListener();
        }
        super.onReceive(context, intent);
    }

    private ArrayList<String> getCleaningPaths() {
        return PrefsUtil.readCleaningList(context);
    }

    /**
     * Delete items
     */
    private void cleanSelectedFolders() {
        ArrayList<String> cleaningPaths = getCleaningPaths();
        if (!cleaningPaths.isEmpty()) {
            cleanFoldersTask = new CleanFoldersTask();
            cleanFoldersTask.setListener(cleaningListener);
            cleanFoldersTask.execute(cleaningPaths);
        } else {
            Intent intent = new Intent(context, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void getFolderSize() {
        getFolderSizeTask = new GetFolderSizeTask();
        getFolderSizeTask.setListener(sizeListener);
        getFolderSizeTask.execute(getCleaningPaths());
    }

    /**
     * Widget on click listener
     */
    private void setOnClickListener() {
        Intent intent = new Intent(WID_CLICKED);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.backgroundImage, pendingIntent);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    /**
     * Listening to cleaning selected folders
     */
    private CleaningListener cleaningListener = new CleaningListener() {
        @Override
        public void OnCompleted() {
            //update size on the widget
            getFolderSize();

            Toast toast = Toast.makeText(context, context.getString(R.string.cleaned),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    };

    /**
     * Listening to getting total size of folders
     */
    private SizeListener sizeListener = new SizeListener() {
        @Override
        public void OnResult(double sum) {
            String formattedSize;
            DecimalFormat decimalFormat = new DecimalFormat("0.0");

            if (sum < 1000) {
                formattedSize = decimalFormat.format(sum);
                remoteViews.setTextViewText(R.id.size, formattedSize);
                remoteViews.setTextViewText(R.id.unit, context.getString(R.string.sizeUnitMb));
            } else {
                sum = sum / 1000;
                formattedSize = decimalFormat.format(sum);
                remoteViews.setTextViewText(R.id.size, formattedSize);
                remoteViews.setTextViewText(R.id.unit, context.getString(R.string.sizeUnitGb));
            }

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
    };
}