/*
 * Copyright (C) 2015 Ferid Cafer
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

package com.ferid.app.cleaner.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.ferid.app.cleaner.R;
import com.ferid.app.cleaner.enums.SortingType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Ferid Cafer on 11/16/2015.
 */
public class PrefsUtil {

    private static SharedPreferences sPrefs;

    /**
     * Initialise shared preferences
     * @param context
     */
    private static void initialisePrefs(Context context) {
        sPrefs = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
    }

    /**
     * Get sorting type
     * @param context
     * @return
     */
    public static SortingType getSortingType(Context context) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        if (sPrefs != null) {
            int preference = sPrefs.getInt(context.getString(R.string.sorting_type),
                    SortingType.ALPHABET.getValue());

            return SortingType.values()[preference];
        } else {
            return SortingType.ALPHABET;
        }
    }

    /**
     * Set sorting type
     * @param context
     * @param value
     */
    public static void setSortingType(Context context, SortingType value) {
        if (sPrefs == null) {
            initialisePrefs(context);
        }

        if (sPrefs != null) {
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putInt(context.getString(R.string.sorting_type), value.getValue());
            editor.commit();
        }
    }

    /**
     * Get the root searching path to go through
     * @return File
     */
    public static File getExplorerRootPath() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * Get decimal format
     * @return
     */
    public static String getDecimalFormat(Context context, double size) {
        String formattedSize = "";
        DecimalFormat decimalFormat = new DecimalFormat("0.0");

        if (size < 1000) {
            formattedSize = decimalFormat.format(size) + " " + context.getString(R.string.sizeUnitMb);
        } else {
            size = size / 1000;
            formattedSize = decimalFormat.format(size) + " " + context.getString(R.string.sizeUnitGb);
        }

        return formattedSize;
    }

    /**
     * Get path to write.<br />
     * Initially creates the necessary folder
     * @return
     */
    private static String getPathPrefix() {
        String path = Environment.getExternalStorageDirectory() + "/cleaner_widget/";
        // create a File object for the parent directory
        File directory = new File(path);
        // have the object build the directory structure, if needed.
        directory.mkdirs();

        return path;
    }

    /**
     * Write cleaning list
     * @param list ArrayList<String>
     */
    public synchronized static void writeCleaningList(Context context, ArrayList<String> list) {
        try {
            String tempPath = getPathPrefix() + context.getString(R.string.pref_cleaner);
            File file = new File(tempPath);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(list);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read cleaning list
     * @return ArrayList<String>
     */
    public synchronized static ArrayList<String> readCleaningList(Context context) {
        ArrayList<String> tmpList = new ArrayList<String>();
        try {
            String tmpPath = getPathPrefix() + context.getString(R.string.pref_cleaner);
            File file = new File(tmpPath);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                tmpList = (ArrayList<String>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        makeSelfHealing(context, tmpList);


        return tmpList;
    }

    /**
     * If folder has been removed or renamed,
     * remove the item from the cleaning list
     * this is a kind of self healing
     * @param context
     * @param initialCleanList
     */
    private static void makeSelfHealing(Context context, ArrayList<String> initialCleanList) {
        ArrayList<String> tmp = new ArrayList<String>();
        for (String path : initialCleanList) {
            File file = new File(PrefsUtil.getExplorerRootPath() + path);
            if (file.exists()) {
                tmp.add(path);
            }
        }

        initialCleanList.clear();
        initialCleanList.addAll(tmp);

        PrefsUtil.writeCleaningList(context, tmp);
    }

    /**
     * Exceptional folders.<br />
     * Check for validity.
     * @param str Folder Path
     * @return valid or not
     */
    public static boolean isValidFolder(String str) {
        if (!str.contains("/.")
                && !str.startsWith("/Android")
                && !str.startsWith("/cleaner_widget")
                && !str.startsWith("/frequent_contacts_widget")
                && !str.startsWith("/attendance_taker")) {

            return true;
        } else {
            return false;
        }
    }
}