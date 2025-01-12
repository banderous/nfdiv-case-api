package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.join;
import static java.time.temporal.ChronoUnit.DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_PRONOUNCEMENT_DATE_PLUS_43;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_HEARING_MINUS_SEVEN_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;

@Component
@Slf4j
public class EntitlementGrantedConditionalOrderNotification implements ApplicantNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Value("${toggle.enable_entitlement_email}")
    private boolean enableSolicitorEntitlementEmail;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending entitlement granted on conditional order notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference());
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        if (enableSolicitorEntitlementEmail && !caseData.getApplicant1().isOffline()) {
            log.info("Sending entitlement granted on conditional order notification to applicant 1 solicitor for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant1().getCorrespondenceEmail(),
                SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
                templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
                caseData.getApplicant1().getLanguagePreference());
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending entitlement granted on conditional order notification to applicant 2 for case : {}", id);

        EmailTemplateName emailTemplateName =
            caseData.getApplicationType().isSole()
                ? SOLE_RESPONDENT_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED : CITIZEN_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            emailTemplateName,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference());
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        if (enableSolicitorEntitlementEmail
            && !StringUtils.isEmpty(caseData.getApplicant2().getEmail()) || !caseData.getApplicant2().isOffline()) {

            log.info("Sending entitlement granted on conditional order notification to applicant 2 solicitor for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2().getCorrespondenceEmail(),
                SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED,
                templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
                caseData.getApplicant2().getLanguagePreference());
        }
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();

        templateVars.put(IS_SOLE, caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(IS_JOINT, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        templateVars.put(COURT_NAME, conditionalOrder.getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING, dateAndTimeOfHearing.format(DATE_TIME_FORMATTER));
        templateVars.put(TIME_OF_HEARING, dateAndTimeOfHearing.format(TIME_FORMATTER));
        templateVars.put(CO_PRONOUNCEMENT_DATE_PLUS_43, dateAndTimeOfHearing.plusDays(43).format(DATE_TIME_FORMATTER));
        templateVars.put(DATE_OF_HEARING_MINUS_SEVEN_DAYS, dateAndTimeOfHearing.minus(7, DAYS).format(DATE_TIME_FORMATTER));

        if (applicant.isRepresented()) {
            templateVars.put(APPLICANT_NAME, join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
            templateVars.put(RESPONDENT_NAME, join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));

            templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
            templateVars.put(SOLICITOR_REFERENCE, Objects.nonNull(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : "not provided");
        }

        return templateVars;
    }
}
