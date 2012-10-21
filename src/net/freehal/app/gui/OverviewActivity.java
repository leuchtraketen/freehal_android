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
import net.freehal.app.notification.NotificationService;
import net.freehal.app.util.FreehalAdapters;
import net.freehal.app.util.FreehalUser;
import net.freehal.app.util.SpeechHelper;
import net.freehal.app.util.AndroidUtils;
import net.freehal.app.util.VoiceRecHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class OverviewActivity extends SherlockFragmentActivity implements OverviewFragment.Callbacks {

	private static boolean mTwoPane;
	private String selectedTab;
	private OverviewFragment fragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		AndroidUtils.setActivity(this, OverviewActivity.class);
		FreehalUser.init(this.getApplicationContext());
		FreehalAdapters.initialize();
		SpeechHelper.getInstance().start();

		if (findViewById(R.id.detail_container) != null) {
			mTwoPane = true;
			fragment = (OverviewFragment) getSupportFragmentManager().findFragmentById(R.id.list);
			fragment.setTwoPane(true);
			fragment.setActivateOnItemClick(true);

		}
		SelectContent.init(this.getResources());

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		this.startService(new Intent(this, NotificationService.class));

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				recieveText(intent);
			}
		} else if (intent.getBooleanExtra("BY_SERVICE", false) == true) {
			startedByService(intent);
		} else {
			launch(savedInstanceState);
		}
	}

	public void launch(Bundle savedInstanceState) {
		if (mTwoPane) {
			DetailFragment.setRecievedIntent(null);

			final String tab;
			if (savedInstanceState != null && savedInstanceState.containsKey("tab"))
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

	private void startedByService(Intent intent) {
		onItemSelected("online");
	}

	@Override
	public void onItemSelected(String id) {

		if (selectedTab == "settings") {
			onItemSelected(selectedTab);
			Intent detailIntent = new Intent(this, FreehalPreferences.class);
			startActivity(detailIntent);

		} else if (mTwoPane) {
			selectedTab = id;
			DetailFragment fragment = DetailFragment.forTab(id);
			getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, fragment).commit();

		} else {
			selectedTab = id;
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
		doCreateOptionsMenu(menu, this, this.getApplicationContext());
		return true;
	}

	public static void doCreateOptionsMenu(Menu menu, final SherlockFragmentActivity activity,
			final Context appContext) {
		MenuInflater inflater = activity.getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);

		// voice recognition
		if (VoiceRecHelper.hasVoiceRecognition(appContext)) {
			final MenuItem voiceRec = menu.findItem(R.id.menu_speak);
			if (mTwoPane)
				voiceRec.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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

		// preferences
		{
			final MenuItem prefs = menu.findItem(R.id.menu_settings);
			if (mTwoPane)
				prefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			prefs.setOnMenuItemClickListener(new FreehalPreferences.Listener(activity));
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
