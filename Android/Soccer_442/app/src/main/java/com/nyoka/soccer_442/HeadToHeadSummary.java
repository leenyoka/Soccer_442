package com.nyoka.soccer_442;

import java.io.Serializable;
import java.util.List;

public class HeadToHeadSummary implements Serializable {
    public List<GameResult> headToHead;
    public List<GameResult> homeForm;
    public List<GameResult> awayForm;
}
