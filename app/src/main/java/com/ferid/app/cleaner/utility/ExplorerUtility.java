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

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ferid Cafer on 11/16/2015.
 */
public class ExplorerUtility {

    /**
     * Get total file size of cleaning paths
     * @param cleaningPaths
     * @return
     */
    public static double getTotalFileSize(ArrayList<String> cleaningPaths) {
        File file;
        double sum = 0.0;

        for (String path : cleaningPaths) {
            file = new File(PrefsUtility.INSTANCE.getExplorerRootPath() + "/" + path);
            if (file.exists()) {
                sum += getFileSize(file);
            }
        }

        return sum;
    }

    /**
     * Get the file or folder size in mb unit
     * @param file File
     * @return double
     */
    public static double getFileSize(File file) {
        double bytes;
        if (file.isDirectory()) {
            bytes = getFolderSizeRecursively(file); //get folder size
        } else {
            bytes = file.length(); //get file size
        }

        return (bytes / (1024 * 1024)); //megabytes
    }

    /**
     * Get folder size recursively
     * @param directory File
     * @return long
     */
    private static long getFolderSizeRecursively(File directory) {
        long length = 0;
        if (directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += getFolderSizeRecursively(file);
                }
            }
        }

        return length;
    }

    /**
     * Delete items of the cleaning paths
     * @param cleaningPaths ArrayList<String>
     */
    public static void deleteExplorer(ArrayList<String> cleaningPaths) {
        File directory;

        for (String path : cleaningPaths) {
            directory = new File(PrefsUtility.INSTANCE.getExplorerRootPath() + "/" + path);
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    deleteFiles(directory.listFiles());
                }

                //delete the file or the empty directory too
                directory.delete();
            }
        }
    }

    /**
     * Search through directories and delete recursively
     * @param files array of files and directories to be deleted
     */
    private static synchronized void deleteFiles(File[] files) {
        if (files != null) {
            for (File file : files) {
                recursiveDelete(file);
            }
        }
    }

    /**
     * Search through directory and delete its files recursively
     * @param file directory to iterate recursively or file
     */
    private static synchronized void recursiveDelete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    //continue to delete files recursively
                    recursiveDelete(f);
                }
            }
            //delete file
            file.delete();
        }
    }

}