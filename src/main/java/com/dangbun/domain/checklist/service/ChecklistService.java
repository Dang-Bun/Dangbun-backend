package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.repository.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChecklistService {


    private final ChecklistRepository checklistRepository;


    public void deleteChecklist(Long checkListId){
        checklistRepository.deleteById(checkListId);
    }

    public void completeChecklist(Long placeId, Long checklistId) {
        checklistRepository.findById(checklistId);
    }
}
