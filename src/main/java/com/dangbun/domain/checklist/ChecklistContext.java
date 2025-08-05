package com.dangbun.domain.checklist;

import com.dangbun.domain.checklist.entity.Checklist;

public class ChecklistContext {
    private static final ThreadLocal<Checklist> currentChecklist = new ThreadLocal<>();

    public static void set(Checklist ch){
        currentChecklist.set(ch);
    }

    public static Checklist get(){
        return currentChecklist.get();
    }

    public static void clear(){
        currentChecklist.remove();
    }
}
