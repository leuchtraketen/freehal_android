package net.freehal.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class AutoScrollListView extends ListView {

	public AutoScrollListView(Context context) {
		super(context);
	}

	public AutoScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		ListView list = (ListView) this.findViewById(R.id.listView);
		if (list != null)
			list.setSelection(list.getAdapter().getCount() - 1);
	}

}
