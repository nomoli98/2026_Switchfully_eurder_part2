package com.application.eurder2.controller;

import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.enums.Permission;
import com.application.eurder2.security.SecurityService;
import com.application.eurder2.service.EurderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/eurders")
public class EurderController {
    private final EurderService eurderService;
    private final SecurityService securityService;

    public EurderController(EurderService eurderService, SecurityService securityService) {
        this.eurderService = eurderService;
        this.securityService = securityService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EurderResponse createEurder(@RequestHeader(value = HttpHeaders.AUTHORIZATION,
                                                      required = false) String authorization,
                                       @Valid @RequestBody CreateEurderRequest request){
        securityService.validateAuthorization(authorization, Permission.CREATE_ORDER);
        return eurderService.createEurder(request, securityService.authenticate(authorization));
    }
}
