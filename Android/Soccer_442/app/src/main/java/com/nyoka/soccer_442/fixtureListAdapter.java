package com.nyoka.soccer_442;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.caverock.androidsvg.SVG;
import com.nyoka.soccer_442.football_data.FootballMatch;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class fixtureListAdapter  extends BaseAdapter {
    private List<FootballMatch> fixtures;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();
    String _txt;

    public fixtureListAdapter(Context context, List<FootballMatch> fixtures, String txt) {
        contextView = context;
        this.fixtures = fixtures;
        _txt = txt;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        FootballMatch result = fixtures.get(position);

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(contextView);
            rowView = inflater.inflate(R.layout.template_game, parent,false);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.awayTeamImage = (ImageView) rowView.findViewById(R.id.awayTeamImage);
            viewHolder.score = (TextView) rowView.findViewById(R.id.score);
            viewHolder.homeTeamImage = (ImageView) rowView.findViewById(R.id.homeTeamImage);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.homeTeamName = (TextView) rowView.findViewById(R.id.homeTeamName);
            viewHolder.awayTeamName = (TextView) rowView.findViewById(R.id.awayTeamName);
            rowView.setTag(viewHolder);
            rowView.setMinimumHeight(160);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();

        // (Re)loaded on every bind, not just when the row view is first created - ListView
        // recycles row views, and a recycled view otherwise keeps showing whatever crest/badge
        // an earlier, unrelated row happened to load into it.
        utility.ShowTeamBadge(contextView, result.homeTeam, holder.homeTeamImage, holder.homeTeamName);
        holder.score.setText("VS");
        utility.ShowTeamBadge(contextView, result.awayTeam, holder.awayTeamImage, holder.awayTeamName);
        holder.title.setText(_txt);
        holder.date.setText(utility.FormatMatchDate(result.utcDate));

        //ViewGroup.LayoutParams params = rowView.getLayoutParams();
//        rowView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,175));
        return rowView;
    }

    static class RowDataViewHolder {
        public ImageView awayTeamImage;
        public TextView score;
        public ImageView homeTeamImage;
        public TextView title;
        public TextView date;
        public TextView homeTeamName;
        public TextView awayTeamName;
    }

    @Override
    public int getCount() {
        return fixtures.size();
    }

    @Override
    public Object getItem(int position) {
        return fixtures.get(position);
    }

    @Override
    public long getItemId(int position) {
        FootballMatch commentary = fixtures.get(position);
        //return commentary;
        return 1;
    }
}
