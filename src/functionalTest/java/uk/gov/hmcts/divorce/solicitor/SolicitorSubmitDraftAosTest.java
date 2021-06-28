package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitDraftAos.SOLICITOR_DRAFT_AOS;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationContactInformation;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorSubmitDraftAosTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-solicitor-draft-aos.json";

    @Test
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessful() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicant2OrgContactInformation", organisationContactInformation());

        final Response response = triggerCallback(caseData, SOLICITOR_DRAFT_AOS, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-solicitor-draft-aos-about-to-start.json"
            )));
    }
}