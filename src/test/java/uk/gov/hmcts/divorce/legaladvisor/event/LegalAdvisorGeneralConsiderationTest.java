package uk.gov.hmcts.divorce.legaladvisor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorGeneralReferralDecisionNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.REFUSE;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorGeneralConsideration.LEGAL_ADVISOR_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorGeneralConsiderationTest {

    @Mock
    private Clock clock;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private LegalAdvisorGeneralReferralDecisionNotification notification;

    @InjectMocks
    private LegalAdvisorGeneralConsideration legalAdvisorGeneralConsideration;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorGeneralConsideration.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_GENERAL_CONSIDERATION);
    }

    @Test
    void shouldSetGeneralReferralDecisionDateAndCreateGeneralReferralsListAndBlankGenralReferralFieldsWhenAboutToSubmitIsInvoked() {

        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(APPROVE)
                    .generalReferralDecisionReason("approved")
                    .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        final GeneralReferral responseGeneralReferral = responseData.getGeneralReferrals().get(0).getValue();
        assertThat(responseData.getGeneralReferrals()).hasSize(1);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseGeneralReferral.getGeneralReferralDecision()).isEqualTo(APPROVE);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionReason()).isEqualTo("approved");
        assertThat(responseData.getGeneralReferral()).isNull();

        verify(notificationDispatcher).send(notification, caseData, 12345L);
    }

    @Test
    void shouldNotSendEmailNotificationsIfGeneralReferralIsNotApproved() {

        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(REFUSE)
                    .generalReferralDecisionReason("rejected")
                    .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        final GeneralReferral responseGeneralReferral = responseData.getGeneralReferrals().get(0).getValue();
        assertThat(responseData.getGeneralReferrals()).hasSize(1);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseGeneralReferral.getGeneralReferralDecision()).isEqualTo(REFUSE);
        assertThat(responseGeneralReferral.getGeneralReferralDecisionReason()).isEqualTo("rejected");
        assertThat(responseData.getGeneralReferral()).isNull();

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetGeneralReferralDecisionDateAndCopyIntoGeneralReferralsListAndBlankGenralReferralFieldsWhenAboutToSubmitIsInvoked() {

        setMockClock(clock);

        final List<ListValue<GeneralReferral>> generalReferrals = new ArrayList<>();
        generalReferrals.add(
            ListValue.<GeneralReferral>builder()
                .value(GeneralReferral.builder()
                    .generalReferralDecision(OTHER)
                    .generalReferralDecisionReason("reason")
                    .build())
                .build());

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral.builder()
                    .generalReferralDecision(APPROVE)
                    .generalReferralDecisionReason("approved")
                    .build())
            .generalReferrals(generalReferrals)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        final CaseData responseData = response.getData();
        assertThat(responseData.getGeneralReferrals()).hasSize(2);
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecision()).isEqualTo(APPROVE);
        assertThat(responseData.getGeneralReferrals().get(0).getValue().getGeneralReferralDecisionReason()).isEqualTo("approved");

        assertThat(responseData.getGeneralReferrals().get(1).getValue().getGeneralReferralDecision()).isEqualTo(OTHER);
        assertThat(responseData.getGeneralReferrals().get(1).getValue().getGeneralReferralDecisionReason()).isEqualTo("reason");

        assertThat(responseData.getGeneralReferral()).isNull();
    }
}
