package uk.gov.hmcts.divorce.divorcecase.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.util.AlternativeServiceUtil.archiveAlternativeServiceApplicationOnCompletion;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


class AlternativeServiceUtilTest {

    @Test
    public void shouldAddNewServiceApplicationToCollectionAndSetApplicationToNull() {

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);

        archiveAlternativeServiceApplicationOnCompletion(caseData);

        assertThat(caseData.getAlternativeServiceApplications()).isNotNull();
        assertThat(caseData.getAlternativeServiceApplications().size()).isEqualTo(1);
        assertThat(caseData.getAlternativeService()).isNull();
    }

    @Test
    public void shouldAddSecondServiceApplicationToCollectionIfOneExists() {

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);
        archiveAlternativeServiceApplicationOnCompletion(caseData);

        caseData.setAlternativeService(new AlternativeService());
        caseData.getAlternativeService().setAlternativeServiceType(DISPENSED);
        archiveAlternativeServiceApplicationOnCompletion(caseData);

        caseData.setAlternativeService(new AlternativeService());
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);
        caseData.getAlternativeService().getBailiff().setSuccessfulServedByBailiff(YesOrNo.YES);
        archiveAlternativeServiceApplicationOnCompletion(caseData);

        assertThat(caseData.getAlternativeServiceApplications().size()).isEqualTo(3);
        assertThat(caseData.getAlternativeServiceApplications().get(0).getValue().getAlternativeServiceType()).isEqualTo(BAILIFF);
        assertThat(caseData.getAlternativeServiceApplications().get(0).getValue().getBailiff().getSuccessfulServedByBailiff())
            .isEqualTo(YesOrNo.YES);
        assertThat(caseData.getAlternativeServiceApplications().get(1).getValue().getAlternativeServiceType()).isEqualTo(DISPENSED);
        assertThat(caseData.getAlternativeServiceApplications().get(2).getValue().getAlternativeServiceType()).isEqualTo(DEEMED);
        assertThat(caseData.getAlternativeService()).isNull();
    }

    @Test
    public void shouldNotAddToServiceApplicationCollectionIfServiceApplicationIsNull() {
        final CaseData caseData = caseData();
        caseData.setAlternativeService(null);
        archiveAlternativeServiceApplicationOnCompletion(caseData);
        assertThat(caseData.getAlternativeServiceApplications()).isNull();
    }

}
