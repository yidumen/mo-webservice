package com.yidumen.service.framework;

/**
 * Created by cdm on 2015/10/9.
 */
public class RangeHeader {

    public static enum Unit {
        BYTES;
    }

    private Unit unit;

    private long from;

    private long to;

    public RangeHeader(Unit unit, long from, long to) {
        this.unit = unit;
        this.from = from;
        this.to = to;
    }

    public Unit getUnit() {
        return unit;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public static RangeHeader valueOf(String range) {
        if (range == null || range.isEmpty()) {
            return null;
        }
        long start = 0;
        long end = -1;
        final StringBuilder rangeStr = new StringBuilder(range);
        rangeStr.delete(0, 6);
        final int split = rangeStr.indexOf("-");
        final String startStr = rangeStr.substring(0, split);
        start = startStr.isEmpty() ? 0 : Long.parseLong(startStr);
        final String endStr = range.substring(split + 1);
        end = endStr.isEmpty() ? -1 : Long.parseLong(endStr);
        return new RangeHeader(Unit.BYTES, start, end);
    }

    public String toString() {
        return String.format("Range: %s=%d-%d", unit.name().toLowerCase(), from, to);
    }

}
