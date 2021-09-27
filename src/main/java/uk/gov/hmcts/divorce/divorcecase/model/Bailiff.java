package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bailiff {

    @CCD(label = "Court name")
    private String localCourtName;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String localCourtEmail;
}