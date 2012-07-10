package net.freehal.app;

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
        setContentView(R.layout.activity_conversation_list);

        if (findViewById(R.id.conversation_detail_container) != null) {
            mTwoPane = true;
            ((OverviewFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.conversation_list))
                    .setActivateOnItemClick(true);
            onItemSelected("1");
        }
        
        SpeechHelper.getInstance().start(this);
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            DetailFragment fragment = DetailFragment.forTab(id, this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.conversation_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
