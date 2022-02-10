package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class UpdateApplicant2ContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("RespondentContactDetails")
            .pageLabel("Update the respondent's contact details")
            .showCondition("applicationType=\"soleApplication\"")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getHomeAddress)
                .label("LabelHorizontalLine1Resp", "<hr>")
                .mandatory(Applicant::getContactDetailsType)
                .done()
            .page("Applicant2ContactDetails")
            .pageLabel("Update applicant 2 contact details")
            .showCondition("applicationType=\"jointApplication\"")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getEmail)
                .mandatory(Applicant::getPhoneNumber)
                .mandatory(Applicant::getHomeAddress)
                .label("LabelHorizontalLine1App2", "<hr>")
                .mandatory(Applicant::getContactDetailsType)
                .done();
    }
}
