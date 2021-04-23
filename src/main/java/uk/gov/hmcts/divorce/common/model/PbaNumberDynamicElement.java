package uk.gov.hmcts.divorce.common.model;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.DynamicElementIndicator;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;

import java.util.UUID;

@AllArgsConstructor
public class PbaNumberDynamicElement implements DynamicElementIndicator {

    private UUID code;
    private String label;

    @Override
    public DynamicListElement toDynamicElement() {
        return DynamicListElement.builder().code(code).label(label).build();
    }
}
