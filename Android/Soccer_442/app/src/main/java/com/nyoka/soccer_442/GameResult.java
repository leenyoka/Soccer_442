package com.nyoka.soccer_442;

import java.io.Serializable;

public class GameResult implements Serializable {
    public String date;
    public String opponentName;
    public String scoreLine;
    public String result; // "W" | "L" | "D"
    public String competitionName;
}
