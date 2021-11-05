package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;

import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVarsForNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());

        final Map<String, String> templateVars = commonContent.commonNotificationTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(3)
            .contains(
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldGetService() {
        CaseData caseData = caseData();
        assertThat(commonContent.getService(caseData.getDivorceOrDissolution())).isEqualTo("divorce");

        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getService(caseData.getDivorceOrDissolution())).isEqualTo("civil partnership");
    }

    @Test
    void shouldGetPartner() {
        CaseData caseData = caseData();
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("wife");

        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("husband");

        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("civil partner");
    }

    @Test
    void shouldReturnDivorceOrDissolution() {
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        assertTrue(isDivorce(caseData));

        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertFalse(isDivorce(caseData));
    }
}
