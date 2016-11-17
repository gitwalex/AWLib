/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.aw.awlib.filechooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.aw.awlib.R;

public class FileChooserAdapter extends ArrayAdapter<FileChooserOptions> {
    private Context c;
    private int id;
    private List<FileChooserOptions> items;

    public FileChooserAdapter(Context context, int textViewResourceId,
                              List<FileChooserOptions> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    @Override
    public FileChooserOptions getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
            v.setTag(R.layout.awlib_filechooser, new ViewHolder(v));
        }
        final FileChooserOptions o = items.get(position);
        if (o != null) {
            ViewHolder holder = (ViewHolder) v.getTag(R.layout.awlib_filechooser);
            String name = null;
            if (holder.fileName != null) {
                name = o.getName();
                holder.fileName.setText(name);
            }
            if (holder.fileData != null) {
                holder.fileData.setText(o.getData());
            }
            if (holder.ivFile != null) {
                if (o.parent == null) {
                    holder.ivFile.setImageResource(R.drawable.ic_file_generic);
                } else {
                    if (o.parent) {
                        holder.ivFile.setImageResource(R.drawable.ic_open_folder);
                    } else {
                        holder.ivFile.setImageResource(R.drawable.ic_closed_folder);
                    }
                }
            }
        }
        return v;
    }
    // couldn't find taskId=12001 Callers=com.android.server.wm.WindowState.getDisplayContent:1059 com.android.server.wm.WindowState.isDefaultDisplay:1778 com.android.server.wm.Windo

    private class ViewHolder {
        public TextView fileName, fileData;
        public ImageView ivFile;

        public ViewHolder(View view) {
            ivFile = (ImageView) view.findViewById(R.id.folderImage);
            fileName = (TextView) view.findViewById(R.id.awlib_fileName);
            fileData = (TextView) view.findViewById(R.id.awlib_fileData);
        }
    }
}
