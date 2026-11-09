package com.application.eurder2.mapper;


import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.response.ItemGroupResponse;
import com.application.eurder2.dtos.response.report.ReportItemGroupResponse;

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

    public static ReportItemGroupResponse toReport(ItemGroup itemGroup){
        return new ReportItemGroupResponse(itemGroup.getItemName(),
                                           itemGroup.getAmount(),
                                           itemGroup.getGroupPrice());
    }

}
