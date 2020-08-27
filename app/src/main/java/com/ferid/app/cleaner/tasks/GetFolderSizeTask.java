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

package com.ferid.app.cleaner.tasks;

import android.os.AsyncTask;

import com.ferid.app.cleaner.listeners.SizeListener;
import com.ferid.app.cleaner.utility.ExplorerUtility;

import java.util.ArrayList;

/**
 * Update the total size of items to be deleted
 */
public class GetFolderSizeTask extends AsyncTask<ArrayList<String>, Void, Double> {

    private SizeListener sizeListener;

    @Override
    protected Double doInBackground(ArrayList<String>... params) {
        return ExplorerUtility.getTotalFileSize(params[0]);
    }

    @Override
    protected void onPostExecute(Double sum) {
        super.onPostExecute(sum);

        if (sizeListener != null) {
            sizeListener.onResult(sum);
        }
    }

    public void setListener(SizeListener operationListener) {
        this.sizeListener = operationListener;
    }
}