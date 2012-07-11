package net.freehal.app;

import net.freehal.app.impl.FreehalUser;
import net.freehal.app.select.SelectContent;
import net.freehal.app.util.SpeechHelper;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class OverviewActivity extends SherlockFragmentActivity
        implements OverviewFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        FreehalUser.init(this.getApplicationContext());

        if (findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
            ((OverviewFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.list))
                    .setActivateOnItemClick(true);
            onItemSelected("about");
        }
        SelectContent.init(this.getResources());
        
        SpeechHelper.getInstance().start(this);
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            DetailFragment fragment = DetailFragment.forTab(id, this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
