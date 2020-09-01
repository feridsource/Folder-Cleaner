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
package com.ferid.app.cleaner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ferid.app.cleaner.R
import com.ferid.app.cleaner.model.Explorer
import com.ferid.app.cleaner.utility.PrefsUtility.getDecimalFormat
import kotlinx.android.synthetic.main.item_explorer.view.*

class ExplorerAdapter(private val items: ArrayList<Explorer>,
                      private val itemClick: (Explorer) -> Unit)
    : RecyclerView.Adapter<ExplorerAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(item: Explorer, listener: (Explorer) -> Unit) = with(itemView) {
            if (item.isToClean) {
                itemView.checkBox.setImageResource(R.drawable.ic_check_box)
                itemView.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent))
            } else {
                itemView.checkBox.setImageResource(R.drawable.ic_check_box_blank)
                itemView.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.darkGrey))
            }

            if (item.size > 0.0) {
                if (item.isHidden) {
                    itemView.imageViewFolder.setColorFilter(
                            ContextCompat.getColor(context, R.color.hiddenFolder))
                    itemView.fileType.text = context.getString(R.string.hidden)
                } else {
                    if (item.isDirectory) {
                        itemView.imageViewFolder.setColorFilter(
                                ContextCompat.getColor(context, R.color.fullFolder))
                        itemView.fileType.text = context.getString(R.string.fullFolder)
                    } else {
                        itemView.imageViewFolder.setColorFilter(
                                ContextCompat.getColor(context, R.color.itIsFile))
                        itemView.fileType.text = context.getString(R.string.file)
                    }
                }
            } else {
                itemView.imageViewFolder.setColorFilter(
                        ContextCompat.getColor(context, R.color.emptyFolder))
                itemView.fileType.text = context.getString(R.string.emptyFolder)
            }

            itemView.path.text = item.path

            itemView.size.text = getDecimalFormat(context, item.size)

            //click listener
            setOnClickListener { listener(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_explorer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], itemClick)
    }
}