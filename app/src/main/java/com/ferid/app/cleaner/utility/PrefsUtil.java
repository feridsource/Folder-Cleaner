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

package com.ferid.app.cleaner.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.ferid.app.cleaner.R;
import com.ferid.app.cleaner.enums.SortingType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

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
            editor.apply();
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
     * Write cleaning list
     * @param list ArrayList<String>
     */
    public synchronized static void writeCleaningList(Context context, ArrayList<String> list) {
        FileOutputStream outputStream = null;

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }

        try {
            outputStream = context.openFileOutput(context.getString(R.string.list_file_name),
                    Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Read cleaning list
     * @return ArrayList<String>
     */
    public synchronized static ArrayList<String> readCleaningList(Context context) {
        ArrayList<String> tmpList = new ArrayList<>();
        FileInputStream fin = null;

        try {
            fin = context.openFileInput(context.getString(R.string.list_file_name));
            int c;
            StringBuilder sb = new StringBuilder();
            while ((c = fin.read()) != -1) {
                sb.append(Character.toString((char)c));
            }
            if (!sb.toString().equals("")) {
                tmpList.addAll(Arrays.asList(sb.toString().split(",")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        makeSelfHealing(context, tmpList);


        return tmpList;
    }

    /**
     * If folder has been removed manually or renamed,
     * remove the item from the cleaning list
     * this is a kind of self healing
     * @param context
     * @param initialCleanList
     */
    private static void makeSelfHealing(Context context, ArrayList<String> initialCleanList) {
        ArrayList<String> tmp = new ArrayList<>();
        for (String path : initialCleanList) {
            File file = new File(PrefsUtil.getExplorerRootPath() + "/" + path);
            if (file.exists()) {
                tmp.add(path);
            }
        }

        //if any changes have happened, write the newer one
        if (!(initialCleanList.containsAll(tmp) && tmp.containsAll(initialCleanList))) {
            PrefsUtil.writeCleaningList(context, tmp);

            initialCleanList.clear();
            initialCleanList.addAll(tmp);
        }
    }

    /**
     * Exceptional folders.<br />
     * Check for validity.
     * @param str Folder Path
     * @return valid or not
     */
    public static boolean isValidFolder(String str) {
        if (!str.contains("/.")
                && !str.startsWith("/Android")) {

            return true;
        } else {
            return false;
        }
    }
}