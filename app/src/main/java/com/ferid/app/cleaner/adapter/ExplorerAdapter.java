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

package com.ferid.app.cleaner.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ferid.app.cleaner.R;
import com.ferid.app.cleaner.listeners.AdapterListener;
import com.ferid.app.cleaner.model.Explorer;
import com.ferid.app.cleaner.utility.PrefsUtil;

import java.util.ArrayList;

/**
 * Created by ferid.cafer on 3/29/2018.
 */

public class ExplorerAdapter extends RecyclerView.Adapter<ExplorerAdapter.ViewHolder> {
    private Context context;

    private ArrayList<Explorer> items;

    private AdapterListener adapterClickListener;

    public ExplorerAdapter(Context context, ArrayList<Explorer> items) {
        this.context = context;
        this.items = items;
    }

    /**
     * Set on item click listener
     * @param adapterClickListener AdapterClickListener
     */
    public void setAdapterClickListener(AdapterListener adapterClickListener) {
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explorer_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Explorer item = items.get(position);

        viewHolder.path.setText(item.getPath());

        viewHolder.size.setText(PrefsUtil.getDecimalFormat(context, item.getSize()));

        if (item.isToClean()) {
            viewHolder.checkBox.setImageResource(R.drawable.ic_check_box);
            viewHolder.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            viewHolder.checkBox.setImageResource(R.drawable.ic_check_box_blank);
            viewHolder.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.darkGrey));
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView path;
        TextView size;
        ImageView checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            path = itemView.findViewById(R.id.path);
            size = itemView.findViewById(R.id.size);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();

            if (adapterClickListener != null && position != RecyclerView.NO_POSITION) {
                adapterClickListener.OnItemClick(position);
            }
        }
    }
}