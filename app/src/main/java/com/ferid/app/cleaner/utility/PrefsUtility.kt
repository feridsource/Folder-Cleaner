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

package com.ferid.app.cleaner.utility

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.ferid.app.cleaner.R
import com.ferid.app.cleaner.enums.SortingType
import java.io.File
import java.text.DecimalFormat

object PrefsUtility {

    private var prefs: SharedPreferences? = null

    /**
     * Initialise shared preferences
     * @param context
     */
    fun initialisePrefs(context: Context) {
        prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0)
    }

    /**
     * Get sorting type
     * @param context
     * @return
     */
    fun getSortingType(context: Context): SortingType? {
        if (prefs == null) {
            initialisePrefs(context)
        }

        val preference = prefs!!.getInt(context.getString(R.string.sorting_type),
                SortingType.ALPHABET.value)
        return SortingType.values()[preference]
    }

    /**
     * Set sorting type
     * @param context
     * @param value
     */
    fun setSortingType(context: Context, value: SortingType) {
        if (prefs == null) {
            initialisePrefs(context)
        }

        val editor = prefs!!.edit()
        editor.putInt(context.getString(R.string.sorting_type), value.value)
        editor.apply()
    }

    /**
     * Get the root searching path to go through
     * @return File
     */
    fun getExplorerRootPath(): File? {
        return Environment.getExternalStorageDirectory()
    }

    /**
     * Get decimal format
     * @return
     */
    fun getDecimalFormat(context: Context, size: Double): String? {
        val formattedSize: String
        val decimalFormat = DecimalFormat("0.00")
        formattedSize = decimalFormat.format(size) + " " + context.getString(R.string.sizeUnitMb)
        return formattedSize
    }

    /**
     * Exceptional files and folders.<br />
     * Check for validity.
     * @param str Folder Path
     * @return valid or not
     */
    fun isValidFolder(str: String): Boolean {
        return !str.startsWith("Android")
    }
}