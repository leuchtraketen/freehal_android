package net.freehal.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class OverviewActivity extends FragmentActivity
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
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(DetailFragment.ARG_ITEM_ID, id);
            DetailFragment fragment = new DetailFragment();
            fragment.setActivity(this);
            fragment.setArguments(arguments);
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
