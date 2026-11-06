package com.application.eurder2.controller;

import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.enums.Permission;
import com.application.eurder2.security.SecurityService;
import com.application.eurder2.service.ItemService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;
    private final SecurityService securityService;

    public ItemController(ItemService itemService, SecurityService securityService) {
        this.itemService = itemService;
        this.securityService = securityService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createItem(@RequestHeader(value = HttpHeaders.AUTHORIZATION,
                                                  required = false) @Parameter(hidden = true)
                                   String authorization,
                                   @Valid @RequestBody CreateItemRequest request) {
        securityService.validateAuthorization(authorization, Permission.CREATE_ITEM);
        return itemService.createItem(request);
    }
}
