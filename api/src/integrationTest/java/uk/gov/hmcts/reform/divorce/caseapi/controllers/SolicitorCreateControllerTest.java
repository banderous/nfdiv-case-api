package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.caseapi.config.WebMvcConfig;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_ABOUT_TO_START_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.SOLICITOR_CREATE_ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.util.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SolicitorCreateControllerTest {

    private static final Instant INSTANT = Instant.parse("2021-04-06T16:00:00.00Z");
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final String SET_LANGUAGE_PREFERENCE = "classpath:set-language-preference-response.json";
    private static final String SOLICITOR_CREATE_ABOUT_TO_SUBMIT = "classpath:solicitor-create-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitorCreateController solicitorCreateController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @Test
    void givenValidCaseDataWhenAboutToStartCallbackIsInvokedLanguagePreferenceIsSet()
        throws Exception {

        final String jsonStringResponse = mockMvc.perform(post(SOLICITOR_CREATE_ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest()))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SET_LANGUAGE_PREFERENCE), STRICT);
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitCallbackIsInvokedCaseDataIsSetCorrectly()
        throws Exception {

        when(clock.instant()).thenReturn(INSTANT);
        when(clock.getZone()).thenReturn(ZONE_ID);

        final String jsonStringResponse = mockMvc.perform(post(SOLICITOR_CREATE_ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(jsonStringResponse, expectedResponse(SOLICITOR_CREATE_ABOUT_TO_SUBMIT), STRICT);
    }

    private String expectedResponse(final String jsonFile) throws IOException {
        final File issueFeesResponseJsonFile = getFile(jsonFile);

        return new String(readAllBytes(issueFeesResponseJsonFile.toPath()));
    }

    private CaseData caseData() {
        return CaseData.builder()
            .petitionerFirstName(TEST_FIRST_NAME)
            .petitionerLastName(TEST_LAST_NAME)
            .petitionerEmail(TEST_USER_EMAIL)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .divorceCostsClaim(YES)
            .build();
    }
}
