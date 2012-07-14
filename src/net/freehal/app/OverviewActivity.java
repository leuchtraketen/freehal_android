package net.freehal.app;

import net.freehal.app.impl.FreehalUser;
import net.freehal.app.select.SelectContent;
import net.freehal.app.util.SpeechHelper;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class OverviewActivity extends SherlockFragmentActivity implements
		OverviewFragment.Callbacks {

	private boolean mTwoPane;
	private String selectedTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		FreehalUser.init(this.getApplicationContext());
		SpeechHelper.getInstance().start(this);

		if (findViewById(R.id.detail_container) != null) {
			mTwoPane = true;
			OverviewFragment fragment = (OverviewFragment) getSupportFragmentManager()
					.findFragmentById(R.id.list);
			fragment.setTwoPane(true);
			fragment.setActivateOnItemClick(true);

		}
		SelectContent.init(this.getResources());

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				recieveText(intent);
			}
		} else {
			launch(savedInstanceState);
		}
	}

	public void launch(Bundle savedInstanceState) {
		if (mTwoPane) {
			DetailFragment.setRecievedIntent(null);

			final String tab;
			if (savedInstanceState != null
					&& savedInstanceState.containsKey("tab"))
				tab = savedInstanceState.getString("tab");
			else
				tab = "about";
			onItemSelected(tab);
		}
	}

	private void recieveText(Intent intent) {
		DetailFragment.setRecievedIntent(intent);
		onItemSelected("online");
	}

	@Override
	public void onItemSelected(String id) {
		selectedTab = id;
		if (mTwoPane) {
			DetailFragment fragment = DetailFragment.forTab(id, this);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.detail_container, fragment).commit();

		} else {
			Intent detailIntent = new Intent(this, DetailActivity.class);
			detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (selectedTab != null && selectedTab.length() > 0)
			savedInstanceState.putString("tab", selectedTab);
	}
}
