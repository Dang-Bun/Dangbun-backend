package com.dangbun.domain.checkList.service;

import com.dangbun.domain.checkList.repository.CheckListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckListService {


    private final CheckListRepository checkListRepository;


    public void deleteCheckList(Long checkListId){
        checkListRepository.deleteById(checkListId);
    }

}
