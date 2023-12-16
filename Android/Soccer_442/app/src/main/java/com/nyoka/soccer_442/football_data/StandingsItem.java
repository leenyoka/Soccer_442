package com.nyoka.soccer_442.football_data;

import java.util.List;

public class StandingsItem {
    public String stage;
    public String type;
    public Object group;  // You might want to create a class for Group if it has a specific structure
    public List<TableItem> table;
}
