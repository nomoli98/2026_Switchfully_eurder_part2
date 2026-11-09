package com.application.eurder2.dtos.response.report;

import java.util.List;
import java.util.UUID;

public record ReportEurderResponse(
   UUID eurderId,
   List<ReportItemGroupResponse> itemGroups,
   double eurderPrice
) {
}
