package grig.yeganyan.trackit;

import java.util.Date;

/** A single logged entry (delta + running total + timestamp). */
public class ProgressEntry {
    public final double delta;
    public final double runningTotal;
    public final Date timestamp;

    public ProgressEntry(double delta, double runningTotal, Date timestamp) {
        this.delta        = delta;
        this.runningTotal = runningTotal;
        this.timestamp    = timestamp;
    }
}