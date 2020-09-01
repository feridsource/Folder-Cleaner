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
package com.ferid.app.cleaner.tasks

import android.os.AsyncTask
import com.ferid.app.cleaner.listeners.SizeListener
import com.ferid.app.cleaner.utility.ExplorerUtility.getTotalFileSize
import java.util.*

class GetFolderSizeTask(val handler: () -> Unit): AsyncTask<ArrayList<String>, Void, Double>() {

    private var sizeListener: SizeListener? = null

    override fun doInBackground(vararg params: ArrayList<String>): Double? {
        return getTotalFileSize(params[0])
    }

    override fun onPostExecute(sum: Double?) {
        super.onPostExecute(sum)

        if (sizeListener != null) {
            sizeListener!!.onResult(sum!!)
        }
    }

    fun setListener(operationListener: SizeListener?) {
        sizeListener = operationListener
    }

}