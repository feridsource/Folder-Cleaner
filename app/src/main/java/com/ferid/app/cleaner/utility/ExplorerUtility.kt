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

import com.ferid.app.cleaner.utility.PrefsUtility.getExplorerRootPath
import java.io.File
import java.util.*

/**
 * Files explorer utility
 */
object ExplorerUtility {
    /**
     * Get total file size of cleaning paths
     * @param cleaningPaths
     * @return
     */
    @JvmStatic
    fun getTotalFileSize(cleaningPaths: ArrayList<String>): Double {
        var file: File
        var sum = 0.0
        for (path in cleaningPaths) {
            file = File(getExplorerRootPath().toString() + "/" + path)
            if (file.exists()) {
                sum += getFileSize(file)
            }
        }
        return sum
    }

    /**
     * Get the file or folder size in mb unit
     * @param file File
     * @return double
     */
    @JvmStatic
    fun getFileSize(file: File): Double {
        val bytes: Double
        bytes = if (file.isDirectory) {
            getFolderSizeRecursively(file).toDouble() //get folder size
        } else {
            file.length().toDouble() //get file size
        }
        return bytes / (1024 * 1024) //megabytes
    }

    /**
     * Get folder size recursively
     * @param directory File
     * @return long
     */
    private fun getFolderSizeRecursively(directory: File): Long {
        var length: Long = 0
        if (directory.isDirectory) {
            for (file in directory.listFiles()) {
                length += if (file.isFile) {
                    file.length()
                } else {
                    getFolderSizeRecursively(file)
                }
            }
        }
        return length
    }

    /**
     * Delete items of the cleaning paths
     * @param cleaningPaths ArrayList<String>
    </String> */
    @JvmStatic
    fun deleteExplorer(cleaningPaths: ArrayList<String>) {
        var directory: File
        for (path in cleaningPaths) {
            directory = File(getExplorerRootPath().toString() + "/" + path)
            if (directory.exists()) {
                if (directory.isDirectory) {
                    deleteFiles(directory.listFiles())
                }

                //delete the file or the empty directory too
                directory.delete()
            }
        }
    }

    /**
     * Search through directories and delete recursively
     * @param files array of files and directories to be deleted
     */
    @Synchronized
    private fun deleteFiles(files: Array<File>?) {
        if (files != null) {
            for (file in files) {
                recursiveDelete(file)
            }
        }
    }

    /**
     * Search through directory and delete its files recursively
     * @param file directory to iterate recursively or file
     */
    @Synchronized
    private fun recursiveDelete(file: File) {
        if (file.exists()) {
            if (file.isDirectory) {
                for (f in file.listFiles()) {
                    //continue to delete files recursively
                    recursiveDelete(f)
                }
            }
            //delete file
            file.delete()
        }
    }
}