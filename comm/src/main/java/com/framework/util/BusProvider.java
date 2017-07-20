package com.framework.util;

import org.greenrobot.eventbus.EventBus;


public final class BusProvider {
    private final static EventBus BUS = EventBus.getDefault();

    public static EventBus getInstance() {
        return BUS;
    }

    private BusProvider() {
    }
}
