package com.application.eurder2.service;

import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.mapper.EurderMapper;
import com.application.eurder2.repository.EurderRepository;
import com.application.eurder2.security.SecurityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EurderService {
    private final EurderRepository repository;
    private final ItemGroupService itemGroupService;

    public EurderService(EurderRepository repository, ItemGroupService itemGroupService,
                         UserService userService, SecurityService securityService) {
        this.repository = repository;
        this.itemGroupService = itemGroupService;
    }

    public EurderResponse createEurder(CreateEurderRequest request, User user) {
        List<ItemGroup> items = request.itemGroupRequests()
                                       .stream()
                                       .map(itemGroupService::createItemGroup)
                                       .toList();
        return EurderMapper.toResponse(repository.save(EurderMapper.toEntity(items, user)));
    }
}
