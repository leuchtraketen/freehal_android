package net.freehal.app;

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

		Util.setActivity(this);

		// if (savedInstanceState == null) {
		final String id = SelectContent.validateId(getIntent().getStringExtra(
				DetailFragment.ARG_ITEM_ID));

		this.setTitle(SelectContent.ITEM_MAP.get(id).title);
		DetailFragment fragment = DetailFragment.forTab(id);

		getSupportFragmentManager().beginTransaction()
				.add(R.id.detail_container, fragment).commit();
		// }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpTo(this,
					new Intent(this, OverviewActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		OverviewActivity.doCreateOptionsMenu(menu, this,
				this.getApplicationContext());
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
