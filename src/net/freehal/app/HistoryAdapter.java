package net.freehal.app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
			tv_name.setText(Html.fromHtml(history.getName(position)+":"));
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
			list.smoothScrollToPosition(history.size());
	}

	public void setListView(ListView list) {
		this.list = list;
	}
}