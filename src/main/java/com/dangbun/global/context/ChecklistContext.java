package com.dangbun.global.context;

import com.dangbun.domain.checklist.refactor.adapter.out.persistence.ChecklistJpaEntity;

public class ChecklistContext {
    private static final ThreadLocal<ChecklistJpaEntity> currentChecklist = new ThreadLocal<>();

    public static void set(ChecklistJpaEntity ch){
        currentChecklist.set(ch);
    }

    public static ChecklistJpaEntity get(){
        return currentChecklist.get();
    }

    public static void clear(){
        currentChecklist.remove();
    }
}
