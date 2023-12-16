package com.nyoka.soccer_442.football_data;

import android.content.DialogInterface;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nyoka.soccer_442.Comment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class FootballMatch {
    public Area area;
    public Competition competition;
    public Season season;
    public int id;
    public String utcDate;

    public LocalDateTime GetDate(){
        String dateString = "2023-08-12T12:00:00Z";

        // Define the date format
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
        }

        try {
            // Parse the date string into a LocalDateTime object
            LocalDateTime parsedDateTime = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                parsedDateTime = LocalDateTime.parse(dateString, formatter);
            }
            return parsedDateTime;

        } catch (Exception e) {
            // Handle parsing errors
            System.out.println("Error parsing date: " + e.getMessage());
        }
        return null;

    }
    public String status;
    public int minute;
    public Integer injuryTime;
    public int attendance;
    public String venue;
    public int matchday;
    public String stage;
    public String group;
    public String lastUpdated;
    public Team homeTeam;
    public Team awayTeam;
    public Score score;
    public List<Goal> goals;
    public List<Object> penalties;  // You can define a Penalty class if needed
    public List<Booking> bookings;
    public List<Substitution> substitutions;
    public Odds odds;
    public List<Referee> referees;

    public ArrayList<Comment> GetComments(){
        ArrayList<Comment> matchCommentry = new ArrayList<>();

        if(goals != null) {
            for (Goal goal : goals) {
                Comment comment = new Comment(String.valueOf(goal.minute), "Goal by " + goal.scorer);
                matchCommentry.add(comment);
            }
        }

        if(bookings != null) {
            for (Booking booking : bookings) {
                Comment comment = new Comment(String.valueOf(booking.minute), booking.card + "for " + booking.player);
                matchCommentry.add(comment);
            }
        }

        if(substitutions != null) {
            for (Substitution substitution : substitutions) {
                Comment comment = new Comment(String.valueOf(substitution.minute), "sub for " + substitution.team + " " + substitution.playerIn + " coming on to replace " + substitution.playerOut);
                matchCommentry.add(comment);
            }
        }

        return matchCommentry;
    }
    public ArrayList<String> GetScorers(HomeOrAway side){
        ArrayList<String> scorers = new ArrayList<>();

        if(goals != null) {
            for (Goal goal : goals) {
                if (goal != null) {
                    if ((goal.team.name == homeTeam.name && side == HomeOrAway.Home) || (goal.team.name == awayTeam.name && side == HomeOrAway.Away)) {
                        scorers.add(goal.scorer.name);
                    }
                }
            }
        }
        return  scorers;
    }
}
