package com.dangbun.domain.cleaning.application.port.in.query;


import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningUnassignedResponse;

import java.util.List;

public interface CleaningQuery {

    List<GetCleaningListResponse> getCleaningList(List<Long> memberIds);

    List<GetCleaningDetailListResponse> getCleaningDetailList(List<Long> memberIds);

    List<GetCleaningUnassignedResponse> getUnassignedCleanings();
}
