package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolStatementOfTruth;
import uk.gov.hmcts.divorce.common.event.page.SolicitorDetailsWithStatementOfTruth;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueAosDisputed.SYSTEM_ISSUE_AOS_DISPUTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueAosUnDisputed.SYSTEM_ISSUE_AOS_UNDISPUTED;

@Component
@Slf4j
public class SubmitAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_AOS = "submit-aos";

    @Autowired
    private SubmitAosService submitAosService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private final List<CcdPageConfiguration> pages = List.of(
        new Applicant2SolStatementOfTruth(),
        new SolicitorDetailsWithStatementOfTruth()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit AoS about to start callback invoked for Case Id: {}", details.getId());

        final var caseData = details.getData();
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (null != acknowledgementOfService && null != acknowledgementOfService.getDateAosSubmitted()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("The Acknowledgement Of Service has already been submitted."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit AoS about to submit callback invoked for Case Id: {}", details.getId());

        final var caseData = details.getData();
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();

        final List<String> errors = validateAos(acknowledgementOfService);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        final CaseDetails<CaseData, State> updateDetails = submitAosService.submitAos(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }

    private List<String> validateAos(final AcknowledgementOfService acknowledgementOfService) {

        final List<String> errors = new ArrayList<>();

        if (NO.equals(acknowledgementOfService.getStatementOfTruth())) {
            errors.add("You must be authorised by the respondent to sign this statement.");
        }

        if (NO.equals(acknowledgementOfService.getPrayerHasBeenGiven())) {
            errors.add("The respondent must have given their prayer.");
        }

        if (NO.equals(acknowledgementOfService.getConfirmReadPetition())) {
            errors.add("The respondent must have read the application for divorce.");
        }

        return errors;
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SUBMIT_AOS)
            .forStates(ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived))
            .name("Submit AoS")
            .description("Submit AoS")
            .showCondition("applicationType=\"soleApplication\"")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                LEGAL_ADVISOR,
                SUPER_USER));
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit AoS submitted callback invoked for Case Id: {}", details.getId());

        final AcknowledgementOfService acknowledgementOfService = details.getData().getAcknowledgementOfService();

        String eventId = DISPUTE_DIVORCE.equals(acknowledgementOfService.getHowToRespondApplication())
            ? SYSTEM_ISSUE_AOS_DISPUTED
            : SYSTEM_ISSUE_AOS_UNDISPUTED;

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id {}", eventId, details.getId());
        ccdUpdateService.submitEvent(details, eventId, user, serviceAuthorization);

        return SubmittedCallbackResponse.builder().build();
    }
}
