package uk.gov.hmcts.divorce.citizen;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.citizen.event.Applicant1StatementOfTruth.APPLICANT_1_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class Applicant1StatementOfTruthTest extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-applicant1-statement-of-truth.json";
    private static final String RESPONSE = "classpath:responses/ccd-callback-casedata-applicant1-statement-of-truth.json";

    @Test
    public void shouldPassValidationAndGiveSuccessWhenCaseDataValid() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("marriageDate", LocalDate.now().minus(1, YEARS).minus(1, DAYS));

        Response response = triggerCallback(request, APPLICANT_1_STATEMENT_OF_TRUTH, ABOUT_TO_START_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertEquals(
            expectedResponse(RESPONSE).replace("2020-04-28", LocalDate.now().minus(1, YEARS).minus(1, DAYS).toString()),
            response.asString(),
            STRICT
        );
    }

}