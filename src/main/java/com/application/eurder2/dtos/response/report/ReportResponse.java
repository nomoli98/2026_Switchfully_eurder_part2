package com.application.eurder2.dtos.response.report;

import java.util.List;

public record ReportResponse(
        List<ReportEurderResponse> eurders,
        double totalPrice
) {
}
