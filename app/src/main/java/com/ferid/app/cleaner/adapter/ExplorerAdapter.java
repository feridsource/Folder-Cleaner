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

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ferid.app.cleaner.R;
import com.ferid.app.cleaner.model.Explorer;
import com.ferid.app.cleaner.utility.PrefsUtil;

import java.util.ArrayList;

/**
 * Created by Ferid Cafer on 11/16/2015.
 */
public class ExplorerAdapter extends ArrayAdapter<Explorer> {
    private Context context;
    private int layoutResId;
    private ArrayList<Explorer> items;

    public ExplorerAdapter(Context context, int layoutResId, ArrayList<Explorer> objects) {
        super(context, layoutResId, objects);
        this.items = objects;
        this.context = context;
        this.layoutResId = layoutResId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.path = (TextView) convertView.findViewById(R.id.path);
            viewHolder.size = (TextView) convertView.findViewById(R.id.size);
            viewHolder.checkBox = (ImageView) convertView.findViewById(R.id.checkBox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Explorer item = items.get(position);

        viewHolder.path.setText(item.getPath());

        viewHolder.size.setText(PrefsUtil.getDecimalFormat(context, item.getSize()));

        if (item.isToClean()) {
            viewHolder.checkBox.setImageResource(R.drawable.abc_btn_check_to_on_mtrl_015);
            viewHolder.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            viewHolder.checkBox.setImageResource(R.drawable.abc_btn_check_to_on_mtrl_000);
            viewHolder.checkBox.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView path;
        TextView size;
        ImageView checkBox;
    }
}