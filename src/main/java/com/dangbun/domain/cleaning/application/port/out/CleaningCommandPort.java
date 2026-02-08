package com.dangbun.domain.cleaning.application.port.out;

import com.dangbun.domain.cleaning.domain.Cleaning;

import java.util.List;

public interface CleaningCommandPort {

    Cleaning save(Cleaning cleaning);

    void saveAll(List<Cleaning> cleanings);

    void delete(Cleaning cleaning);

    void deleteById(Long cleaningId);
}
