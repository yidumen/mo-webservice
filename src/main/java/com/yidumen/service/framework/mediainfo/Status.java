package com.yidumen.service.framework.mediainfo;

public enum Status {

    None(0x00),
    Accepted(0x01),
    Filled(0x02),
    Updated(0x04),
    Finalized(0x08);

    private final int value;

    private Status(int value) {
        this.value = value;
    }

    public int getValue(int value) {
        return value;
    }
}
