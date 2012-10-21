/*******************************************************************************
 * Copyright (c) 2006 - 2012 Tobias Schulz and Contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl.html>.
 ******************************************************************************/
package net.freehal.app.gui;

import net.freehal.app.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

@TargetApi(8)
public class HistoryAdapter extends ArrayAdapter<String> implements HistoryHook {

	private Context context;
	private History history;
	private ListView list;

	public HistoryAdapter(Context context, int resource, History history) {
		super(context, resource, history.getText());

		history.setHook(this);

		this.context = context;
		this.history = history;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.row, null);
		}

		TextView tv_name = (TextView) v.findViewById(R.id.name);
		TextView tv_text = (TextView) v.findViewById(R.id.text);

		if (tv_name != null) {
			tv_name.setText(Html.fromHtml(history.getName(position) + ":"));
		}
		if (tv_text != null) {
			tv_text.setText(Html.fromHtml(history.getText(position)));
		}

		return v;
	}

	@Override
	public int getCount() {
		return history.size();
	}

	@Override
	public void onHistoryChanged() {
		notifyDataSetChanged();
		if (list != null)
			list.setSelection(getCount() - 1);
	}

	public void setListView(ListView list) {
		this.list = list;
	}
}
