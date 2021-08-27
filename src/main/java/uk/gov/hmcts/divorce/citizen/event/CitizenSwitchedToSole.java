package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSole implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SWITCH_TO_SOLE = "citizen-switch-to-sole";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        EnumSet<State> stateSet = EnumSet.noneOf(State.class);
        stateSet.add(AwaitingApplicant1Response);
        stateSet.add(AwaitingApplicant2Response);

        configBuilder
            .event(CITIZEN_SWITCH_TO_SOLE)
            .forStateTransition(stateSet, Draft)
            .name("Applicant 1 switched to sole")
            .description("Application type switched to sole")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Applicant 1 switched to sole about to submit callback invoked");

        CaseData data = details.getData();

        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}