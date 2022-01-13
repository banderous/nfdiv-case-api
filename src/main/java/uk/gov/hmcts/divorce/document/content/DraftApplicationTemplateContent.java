package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_COSTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class DraftApplicationTemplateContent {

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final var application = caseData.getApplication();
        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();
        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE);
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE);
            templateContent.put(FINANCIAL_ORDER_OR_DISSOLUTION, CONDITIONAL_ORDER_OF_DIVORCE_FROM);
            templateContent.put(DIVORCE_OR_DISSOLUTION_COST, DIVORCE_COSTS);

        } else {
            templateContent.put(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP);
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(FINANCIAL_ORDER_OR_DISSOLUTION, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
            templateContent.put(DIVORCE_OR_DISSOLUTION_COST, COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP);
        }

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        if (application.getIssueDate() != null) {
            templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
        }
        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());

        templateContent.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());

        templateContent.put(MARRIAGE_DATE,
            ofNullable(application.getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));
        templateContent.put(COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());

        templateContent.put(HAS_FINANCIAL_ORDERS, applicant1.getFinancialOrder().toBoolean());

        templateContent.put(FINANCIAL_ORDER_CHILD, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2);

        String applicant2PostalAddress;
        AddressGlobalUK applicant2HomeAddress = applicant2.getHomeAddress();

        if (applicant2HomeAddress == null) {
            applicant2PostalAddress = applicant2.getSolicitor().getAddress();
        } else {
            applicant2PostalAddress =
                Stream.of(
                        applicant2HomeAddress.getAddressLine1(),
                        applicant2HomeAddress.getAddressLine2(),
                        applicant2HomeAddress.getAddressLine3(),
                        applicant2HomeAddress.getPostTown(),
                        applicant2HomeAddress.getCounty(),
                        applicant2HomeAddress.getPostCode(),
                        applicant2HomeAddress.getCountry()
                    )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.joining("\n"));
        }
        templateContent.put(APPLICANT_2_POSTAL_ADDRESS, applicant2PostalAddress);

        return templateContent;
    }
}
