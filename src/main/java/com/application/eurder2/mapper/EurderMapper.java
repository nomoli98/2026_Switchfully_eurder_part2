package com.application.eurder2.mapper;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.response.EurderResponse;
import com.application.eurder2.dtos.response.report.ReportEurderResponse;
import com.application.eurder2.dtos.response.report.ReportResponse;

import java.util.List;

public class EurderMapper {
    public static Eurder toEntity(List<ItemGroup> items, User user) {
        return new Eurder(items, user);
    }

    public static EurderResponse toResponse(Eurder eurder) {
        return new EurderResponse(eurder.getId(),
                                  eurder.getItemGroups()
                                        .stream()
                                        .map(ItemGroupMapper::toResponse)
                                        .toList(),
                                  eurder.getTotalPrice(),
                                  eurder.getUser().getId());
    }

    public static CreateEurderRequest toCreateRequest(Eurder eurder){
        return new CreateEurderRequest(eurder.getItemGroups().stream().map(ItemGroupMapper::toRequest).toList());
    }

    public static ReportResponse toReport(List<Eurder> eurders) {
        return new ReportResponse(eurders.stream().map(EurderMapper::toReportEurderRes).toList(),
                                  EurderMapper.getTotalPrice(eurders));
    }

    private static ReportEurderResponse toReportEurderRes(Eurder eurder) {
        return new ReportEurderResponse(
                eurder.getId(),
                eurder.getItemGroups().stream().map(ItemGroupMapper::toReport).toList(),
                eurder.getTotalPrice()
        );
    }

    private static double getTotalPrice(List<Eurder> eurders) {
        return eurders.stream().mapToDouble(Eurder::getTotalPrice).sum();
    }
}
