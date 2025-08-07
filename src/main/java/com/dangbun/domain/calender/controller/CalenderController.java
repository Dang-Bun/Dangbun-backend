package com.dangbun.domain.calender.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping("/place/{placeId}/calender")
@RestController
public class CalenderController {

    @GetMapping("/cleanings")
    public void getCleaningByDate(@RequestParam LocalDate date){

    }
}
