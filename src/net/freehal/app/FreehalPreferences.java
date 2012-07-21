package net.freehal.app;

import java.util.ArrayList;

import net.freehal.app.impl.FreehalUser;
import net.freehal.app.util.Util;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class FreehalPreferences extends SherlockPreferenceActivity implements
		OnPreferenceClickListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private ArrayList<Preference> mPreferences = new ArrayList<Preference>();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// default values
		reset(false);

		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences prefs = Util.getPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
		for (String prefKey : prefs.getAll().keySet()) {
			Preference pref = (Preference) findPreference(prefKey);
			mPreferences.add(pref);
			this.onSharedPreferenceChanged(prefs, prefKey);
		}

		// get the custom preference
		Preference clearPrefs = (Preference) findPreference("clearPrefs");
		clearPrefs.setOnPreferenceClickListener(this);
	}

	private void reset(boolean doReset) {
		SharedPreferences prefs = Util.getPreferences();
		SharedPreferences.Editor editor = prefs.edit();

		if (doReset || prefs.getString("userName", "").length() == 0)
			editor.putString("userName", FreehalUser.get().findOutUserName(""));
		if (doReset || prefs.getString("userEmail", "").length() == 0)
			editor.putString("userEmail", FreehalUser.get()
					.findOutEmailAddr(""));
		if (doReset || prefs.getString("freehalName", "").length() == 0)
			editor.putString("freehalName", FreehalUser.get()
					.findOutFreehalName());

		editor.commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		for (Preference pref : mPreferences) {
			if (pref.getKey().equals(key)) {
				try {
					pref.setSummary(sharedPreferences.getString(key, "<None>"));
				} catch (ClassCastException e) {
					pref.setSummary(sharedPreferences.getBoolean(key, false) ? R.string.string_true
							: R.string.string_false);
				}
				break;
			}
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("clearPrefs")) {
			Toast.makeText(getBaseContext(), R.string.pref_clear_toast,
					Toast.LENGTH_LONG).show();
			reset(true);
		}
		return true;
	}

	public static class Listener implements View.OnClickListener,
			OnMenuItemClickListener {

		final Context context;

		public Listener(Context context) {
			this.context = context;
		}

		private void startActivity() {
			Intent detailIntent = new Intent(context, FreehalPreferences.class);
			context.startActivity(detailIntent);
		}

		@Override
		public void onClick(View v) {
			startActivity();
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			startActivity();
			return true;
		}
	}
}
