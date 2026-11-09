package com.application.eurder2.controller;

import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.dtos.response.report.ReportResponse;
import com.application.eurder2.dtos.response.shippingToday.EurderShippingTodayResponse;
import com.application.eurder2.enums.Permission;
import com.application.eurder2.security.SecurityService;
import com.application.eurder2.service.EurderService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public EurderResponse createEurder(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization,
            @Valid @RequestBody CreateEurderRequest request) {
        User user = securityService.validateAuthorization(authorization, Permission.CREATE_ORDER);
        return eurderService.createEurder(request, user);
    }

    @PostMapping(path = "re-eurder/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public EurderResponse reEurder(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization, @PathVariable("id") UUID eurderId) {
        User user = securityService.validateAuthorization(authorization, Permission.CREATE_ORDER);
        return eurderService.reeurderAnExistingEurder(eurderId, user);
    }

    @GetMapping(path = "my-report")
    public ReportResponse getMyEurderReport(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization) {
        User user = securityService.validateAuthorization(authorization, Permission.VIEW_MY_REPORT);
        return eurderService.getMyEurderReport(user);
    }

    @GetMapping(path = "shipping-today")
    public List<EurderShippingTodayResponse> getAllEurderShippingToday(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            @Parameter(hidden = true) String authorization) {
        securityService.validateAuthorization(authorization, Permission.VIEW_ORDERS);
        return eurderService.getAllEurdersShippingToday();
    }

}
