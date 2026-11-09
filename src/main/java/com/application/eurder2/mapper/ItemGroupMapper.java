package com.application.eurder2.mapper;


import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.dtos.response.ItemGroupResponse;
import com.application.eurder2.dtos.response.report.ReportItemGroupResponse;
import com.application.eurder2.dtos.response.shippingToday.ItemGroupShippingTodayResponse;

public class ItemGroupMapper {
    public static ItemGroup toEntity(Item item, int amount) {
        return new ItemGroup(item, amount);
    }

    public static ItemGroupResponse toResponse(ItemGroup itemGroup) {
        return new ItemGroupResponse(itemGroup.getItem().getName(),
                                     itemGroup.getAmount(),
                                     itemGroup.getGroupPrice(),
                                     itemGroup.getShippingDate());
    }

    public static CreateItemGroupRequest toRequest(ItemGroup itemGroup) {
        return new CreateItemGroupRequest(itemGroup.getItem().getId(),
                                          itemGroup.getAmount());
    }

    public static ReportItemGroupResponse toReport(ItemGroup itemGroup) {
        return new ReportItemGroupResponse(itemGroup.getItemName(),
                                           itemGroup.getAmount(),
                                           itemGroup.getGroupPrice());
    }

    public static ItemGroupShippingTodayResponse toItemGroupShippingToday(ItemGroup itemGroup) {
        return new ItemGroupShippingTodayResponse(itemGroup.getAmount(),
                                                  itemGroup.getItemName(),
                                                  itemGroup.getGroupPrice());
    }

}
