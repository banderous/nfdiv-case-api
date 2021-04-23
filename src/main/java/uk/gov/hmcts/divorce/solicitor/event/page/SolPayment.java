package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.PbaNumberDynamicElement;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PbaValidationClient;
import uk.gov.hmcts.divorce.payment.model.PbaOrganisationResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.DynamicList.toDynamicList;

@Slf4j
@Component
public class SolPayment implements CcdPageConfiguration {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private PbaValidationClient pbaValidationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolPayment", this::midEvent)
            .pageLabel("Payment")
            .label(
                "LabelSolPaymentPara-1",
                "Amount to pay: **£${solApplicationFeeInPounds}**")
            .mandatory(CaseData::getSolPaymentHowToPay);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit petition mid event callback invoked");

        final CaseData data = details.getData();
        final Long caseId = details.getId();
        final String authorisation = httpServletRequest.getHeader(AUTHORIZATION);

        final PbaOrganisationResponse pbaOrganisationResponse = pbaValidationClient.retrievePbaNumbers(
            authorisation,
            authTokenGenerator.generate(),
            idamService.retrieveUser(authorisation).getUserDetails().getEmail()
        );

        final List<String> paymentAccount = pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();

        if (isEmpty(paymentAccount)) {
            log.info("CaseId: {}. No PBA numbers found for this solicitor", caseId);
            data.setPbaNumbers(null); // Ensures previously retrieved PBA numbers are not used
        } else {
            log.info("CaseId: {}. Successfully retrieved {} PBA numbers for solicitor", caseId, paymentAccount.size());

            final DynamicList dynamicList = toDynamicList(paymentAccount.stream()
                .map(s -> new PbaNumberDynamicElement(UUID.fromString(s), s))
                .collect(toList()), null);

            data.setPbaNumbers(dynamicList);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
