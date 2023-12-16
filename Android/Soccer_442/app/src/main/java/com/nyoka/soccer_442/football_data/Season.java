package com.nyoka.soccer_442.football_data;

import java.util.List;

public class Season {
    public int id;
    public String startDate;
    public String endDate;
    public int currentMatchday;
    public Object winner;  // You might want to create a class for Winner if it has a specific structure
    private List<String> stages;
}
