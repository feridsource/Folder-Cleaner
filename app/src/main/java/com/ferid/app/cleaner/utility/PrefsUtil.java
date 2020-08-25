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
import java.text.DecimalFormat;

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
        String formattedSize;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        formattedSize = decimalFormat.format(size) + " " + context.getString(R.string.sizeUnitMb);

        return formattedSize;
    }

    /**
     * Exceptional files and folders.<br />
     * Check for validity.
     * @param str Folder Path
     * @return valid or not
     */
    public static boolean isValidFolder(String str) {
        //cache folders
        return !str.startsWith("Android");
    }
}