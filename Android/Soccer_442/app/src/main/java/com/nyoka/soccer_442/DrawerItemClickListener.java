package com.nyoka.soccer_442;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by linda.nyoka on 2015-02-25.
 */
public class DrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }
    private void selectItem(int position) {

        //Fragment fragment = null;

        switch (position) {
            case 0:
                //fragment = new CreateFragment();
                break;
            case 1:
                //fragment = new ReadFragment();
                break;
            case 2:
                //fragment = new HelpFragment();
                break;

            default:
                break;
        }
    }
}