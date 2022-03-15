package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;

@Component
@Slf4j
public class ApplyForConditionalOrderNotification {

    public static final String JOINT_CONDITIONAL_ORDER = "joint conditional order";
    public static final String HUSBAND_JOINT = "husbandJoint";
    public static final String WIFE_JOINT = "wifeJoint";
    public static final String CIVIL_PARTNER_JOINT = "civilPartnerJoint";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        log.info("Notifying applicant 1 that they can apply for a conditional order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Notifying applicant 2 that they can apply for a conditional order: {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            CITIZEN_APPLY_FOR_CONDITIONAL_ORDER,
            templateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()),
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        boolean jointApplication = !caseData.getApplicationType().isSole();
        templateVars.put(JOINT_CONDITIONAL_ORDER, jointApplication ? YES : NO);
        templateVars.put(HUSBAND_JOINT,
            jointApplication && isDivorce(caseData) && partner.getGender().equals(Gender.MALE) ? YES : NO);
        templateVars.put(WIFE_JOINT,
            jointApplication && isDivorce(caseData) && partner.getGender().equals(Gender.FEMALE) ? YES : NO);
        templateVars.put(CIVIL_PARTNER_JOINT, jointApplication && !isDivorce(caseData) ? YES : NO);
        return templateVars;
    }
}