package com.dangbun.global.context;

import com.dangbun.domain.duty.entity.Duty;

public class DutyContext {
    private static final ThreadLocal<Duty> currentDuty = new ThreadLocal<>();

    public static void set(Duty duty) {
        currentDuty.set(duty);
    }

    public static Duty get() {
        return currentDuty.get();
    }

    public static void clear() {
        currentDuty.remove();
    }
}
