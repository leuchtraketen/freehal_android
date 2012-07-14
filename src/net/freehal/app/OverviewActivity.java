package net.freehal.app;

import net.freehal.app.impl.FreehalUser;
import net.freehal.app.select.SelectContent;
import net.freehal.app.util.SpeechHelper;
import net.freehal.app.util.Util;
import net.freehal.app.util.VoiceRecHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class OverviewActivity extends SherlockFragmentActivity implements
		OverviewFragment.Callbacks {

	private boolean mTwoPane;
	private String selectedTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		Util.setActivity(this);
		FreehalUser.init(this.getApplicationContext());
		SpeechHelper.getInstance().start();

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
			DetailFragment fragment = DetailFragment.forTab(id);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		doCreateOptionsMenu(menu, this,
				this.getApplicationContext());
		return true;
	}

	public static void doCreateOptionsMenu(Menu menu,
			final SherlockFragmentActivity activity, final Context appContext) {
		MenuInflater inflater = activity.getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);

		if (VoiceRecHelper.hasVoiceRecognition(appContext)) {
			final MenuItem voiceRec = menu.findItem(R.id.menu_speak);
			voiceRec.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					VoiceRecHelper.start(activity, appContext);
					return true;
				}
			});
		} else {
			menu.removeItem(R.id.menu_speak);
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VoiceRecHelper.REQUEST_CODE) {
        	VoiceRecHelper.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
