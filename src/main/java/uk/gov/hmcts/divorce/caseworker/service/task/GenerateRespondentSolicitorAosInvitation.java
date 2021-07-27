package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentSolicitorAosInvitationTemplateContent;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator.generateAccessCode;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Component
@Slf4j
public class GenerateRespondentSolicitorAosInvitation implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    //TODO: Use correct template content when application template requirements are known.
    @Autowired
    private RespondentSolicitorAosInvitationTemplateContent templateContent;

    @Autowired
    private HttpServletRequest request;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();

        log.info("Executing handler for generating respondent aos invitation for case id {} ", caseId);

        if (caseData.getApplicant2().isRepresented()) {

            final Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, caseId, createdDate);
            final String userAuthorisation = request.getHeader(AUTHORIZATION);

            caseData.getCaseInvite().setAccessCode(generateAccessCode());

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                DOCUMENT_TYPE_RESPONDENT_INVITATION,
                templateContentSupplier,
                caseId,
                userAuthorisation,
                RESP_SOLICITOR_AOS_INVITATION,
                RESP_AOS_INVITATION_DOCUMENT_NAME,
                caseData.getApplicant1().getLanguagePreference()
            );
        }

        return caseDetails;
    }
}