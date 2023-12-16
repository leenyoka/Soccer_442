package com.nyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class CommentryListAdapter extends BaseAdapter {
    private List<Comment> commentaryList;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    private Utility utility = new Utility();

    public CommentryListAdapter(Context context, ArrayList<Comment> comments){
        contextView = context;
        commentaryList = comments;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View rowView = convertView;
        if(rowView == null){
            rowView = inflater.inflate(R.layout.template_match_comment,null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.time = (TextView) rowView.findViewById(R.id.comment_time);
            viewHolder.comment = (TextView) rowView.findViewById(R.id.comment);
            viewHolder.action = (ImageView) rowView.findViewById(R.id.comment_action);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        Comment commentary = commentaryList.get(position);

        StringBuilder stringBuilder = new StringBuilder(commentary.Time);
        try {
            int num = Integer.parseInt(stringBuilder.toString());
            stringBuilder.append("min");
            holder.time.setText(stringBuilder);
        } catch (NumberFormatException e) {
            holder.time.setText(stringBuilder);
        }
        holder.comment.setText(commentary.Text);

        holder.action.setImageResource(R.drawable.empty);

        if(commentary.Text.contains("Goal!"))
           holder.action.setImageResource(R.drawable.action_goal); //utility.GetMeAnImage(holder.action,"action_goal");
        if(commentary.Text.toLowerCase().contains("yellow card"))
            holder.action.setImageResource(R.drawable.action_yellow_card);// utility.GetMeAnImage(holder.action, "action_yellow_card");
        if(commentary.Text.toLowerCase().contains("red card"))
            holder.action.setImageResource(R.drawable.action_red_card);// utility.GetMeAnImage(holder.action, "action_red_car");

        if(position %2 == 1){
            //rowView.setBackgroundColor(Color.parseColor("#DEDEDE"));
        }else {
            //rowView.setBackgroundColor(Color.parseColor("#E8E8E8"));
        }
        //rowView.setBackgroundResource(R.drawable.panel);
/*
        Animation animation = AnimationUtils.loadAnimation(contextView, R.anim.abc_fade_in);
        animation.setDuration(500);
        rowView.startAnimation(animation);
        animation = null;
 */
        return rowView;
    }

    static class RowDataViewHolder {
        public TextView time;
        public TextView comment;
        public ImageView action;
    }

    @Override
    public int getCount(){
        return commentaryList.size();
    }

    @Override
    public Object getItem(int position){
        return commentaryList.get(position);
    }

    @Override
    public long getItemId(int position){
        Comment commentary = commentaryList.get(position);
        //return commentary;
        return 1;
    }

}
