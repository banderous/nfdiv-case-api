package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant1ApplicationReviewed.SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED;

@Component
@Slf4j
public class SystemRemindApplicant1ApplicationApprovedTask {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(cron = "${schedule.system_remind_applicant1}")
    public void execute() {

        log.info("Remind applicant 1 application approved scheduled task started");

        try {
            final List<CaseDetails> casesInAwaitingApplicant1Response =
                ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant1Response);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant1Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    LocalDate dueDate = caseData.getDueDate();

                    if (dueDate == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as due date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        if (!dueDate.isAfter(LocalDate.now())
                            && !caseData.getApplication().hasNotificationBeenSent(caseData.getApplication().getApplicant1ReminderSent())
                        ) {
                            notifyApplicant1(caseDetails, caseData, dueDate);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Remind applicant 1 application approved scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Remind applicant 1 application approved schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 1 application approved scheduled task stopping"
                + " due to conflict with another running Remind applicant 1 application approved task"
            );
        }
    }

    private void notifyApplicant1(CaseDetails caseDetails, CaseData caseData, LocalDate dueDate) {
        log.info("Due date {} for Case id {} is on/before current date - sending reminder to Applicant 1",
            dueDate,
            caseDetails.getId()
        );

        jointApplicationOverdueNotification.sendApplicationApprovedReminderToApplicant1(caseData, caseDetails.getId());
        ccdUpdateService.submitEvent(caseDetails, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED);
    }
}