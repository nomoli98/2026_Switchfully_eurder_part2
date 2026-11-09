package com.application.eurder2.service;

import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.dtos.response.report.ReportResponse;
import com.application.eurder2.mapper.EurderMapper;
import com.application.eurder2.repository.EurderRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EurderService {
    private final EurderRepository repository;
    private final ItemGroupService itemGroupService;

    public EurderService(EurderRepository repository, ItemGroupService itemGroupService) {
        this.repository = repository;
        this.itemGroupService = itemGroupService;
    }

    @Transactional
    public EurderResponse createEurder(CreateEurderRequest request, User user) {
        List<ItemGroup> groups = request.itemGroupRequests()
                                        .stream()
                                        .map(itemGroupService::createItemGroup)
                                        .toList();
        return EurderMapper.toResponse(repository.save(EurderMapper.toEntity(groups, user)));
    }

    @Transactional(readOnly = true)
    public ReportResponse getMyEurderReport(User user){
        return EurderMapper.toReport(repository.findByUserOrderByOrderDateDesc(user));
    }
}
