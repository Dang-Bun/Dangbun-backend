package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.ChecklistContext;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class ChecklistService {


    private final ChecklistRepository checklistRepository;


    public void deleteChecklist(Long checklistId){
        checklistRepository.deleteById(checklistId);
    }

    public void completeChecklist() {
        Checklist checklist = ChecklistContext.get();
        checklist.completeChecklist();
    }

    public void incompleteChecklist() {
        Checklist checklist = ChecklistContext.get();
        checklist.incompleteChecklist();
    }
}
