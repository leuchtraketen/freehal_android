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
package net.freehal.app;

import net.freehal.app.R;
import net.freehal.app.select.SelectContent;
import net.freehal.app.util.Util;
import net.freehal.app.util.VoiceRecHelper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DetailActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.activity_detail);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Util.setActivity(this, DetailActivity.class);

		// if (savedInstanceState == null) {
		final String id = SelectContent.validateId(getIntent().getStringExtra(DetailFragment.ARG_ITEM_ID));

		this.setTitle(SelectContent.ITEM_MAP.get(id).title);
		DetailFragment fragment = DetailFragment.forTab(id);

		getSupportFragmentManager().beginTransaction().add(R.id.detail_container, fragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpTo(this, new Intent(this, OverviewActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		OverviewActivity.doCreateOptionsMenu(menu, this, this.getApplicationContext());
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VoiceRecHelper.REQUEST_CODE) {
			VoiceRecHelper.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
