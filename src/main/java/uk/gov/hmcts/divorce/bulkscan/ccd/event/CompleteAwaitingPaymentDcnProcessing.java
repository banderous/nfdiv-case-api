package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordPageBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.data.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CompleteAwaitingPaymentDcnProcessing implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String COMPLETE_AWAITING_PAYMENT_DCNPROCESSING = "completeAwaitingPaymentDCNProcessing";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        new ExceptionRecordPageBuilder(configBuilder
            .event(COMPLETE_AWAITING_PAYMENT_DCNPROCESSING)
            .forAllStates()
            .name("Complete DCN processing")
            .description("Complete the processing of payment document control numbers")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SYSTEMUPDATE));
    }
}
