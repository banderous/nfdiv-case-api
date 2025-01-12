package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_PROFESSIONAL_USERS_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVarsForDivorceNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "divorce.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldSetCommonTemplateVarsForDissolutionNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());
        caseData.setDivorceOrDissolution(DISSOLUTION);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DISSOLUTION_COURT_EMAIL, "dissolution.court@email.com"));

        final Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(4)
            .contains(
                entry(COURT_EMAIL, "dissolution.court@email.com"),
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }

    @Test
    void shouldGetPartner() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("wife");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("husband");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("spouse");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getPartner(caseData, caseData.getApplicant2())).isEqualTo("civil partner");
    }

    @Test
    void shouldGetPartnerWelshContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gwraig");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("gŵr");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("priod");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getPartnerWelshContent(caseData, caseData.getApplicant2())).isEqualTo("partner sifil");
    }

    @Test
    void shouldGetUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetEnglishUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.ENGLISH)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.ENGLISH)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetWelshUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("ysgariad");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(commonContent.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("diddymiad");
    }

    @Test
    void shouldSetTemplateVarsForSoleApplication() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(), respondent());

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "no"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsMale() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(FEMALE), getApplicant(MALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "yes"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDivorceApplicationWhenPartnerIsFemale() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(MALE), getApplicant(FEMALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "yes"),
                entry(CIVIL_PARTNER_JOINT, "no")
            );
    }

    @Test
    void shouldSetTemplateVarsForJointDissolution() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .build();

        final Map<String, String> templateVars = commonContent
            .conditionalOrderTemplateVars(caseData, 1L, getApplicant(MALE), getApplicant(FEMALE));

        assertThat(templateVars)
            .isNotEmpty()
            .contains(
                entry(JOINT_CONDITIONAL_ORDER, "yes"),
                entry(HUSBAND_JOINT, "no"),
                entry(WIFE_JOINT, "no"),
                entry(CIVIL_PARTNER_JOINT, "yes")
            );
    }

    @Test
    void shouldReturnProfessionalSignInUrl() {
        Long caseId = 123456789L;
        when(emailTemplatesConfig.getTemplateVars())
            .thenReturn(Map.of(SIGN_IN_PROFESSIONAL_USERS_URL, "http://professional-sing-in-url/"));

        String professionalSignInUrl = commonContent.getProfessionalUsersSignInUrl(caseId);

        assertThat(professionalSignInUrl).isEqualTo("http://professional-sing-in-url/123456789");
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant1PrefersWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(YES)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant1, applicant2);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gwraig")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant1DoesNotPreferWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant1, applicant2);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "wife")
            );
    }

    @Test
    void shouldAddWelshPartnerContentIfApplicant2PrefersWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .gender(FEMALE)
            .languagePreferenceWelsh(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant2, applicant1);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "gŵr")
            );
    }

    @Test
    void shouldNotAddWelshPartnerContentIfApplicant2DoesNotPreferWelsh() {

        final Applicant applicant1 = Applicant.builder()
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .build();

        final Applicant applicant2 = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .build();

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, String> result = commonContent.mainTemplateVars(caseData, 1L, applicant2, applicant1);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(PARTNER, "husband")
            );
    }
}
