package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.repository.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateChecklistService {

    private final ChecklistRepository checklistRepository;
}
