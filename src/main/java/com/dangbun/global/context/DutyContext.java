package com.dangbun.global.context;

import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;

public class DutyContext {
    private static final ThreadLocal<DutyJpaEntity> currentDuty = new ThreadLocal<>();

    public static void set(DutyJpaEntity duty) {
        currentDuty.set(duty);
    }

    public static DutyJpaEntity get() {
        return currentDuty.get();
    }

    public static void clear() {
        currentDuty.remove();
    }
}
