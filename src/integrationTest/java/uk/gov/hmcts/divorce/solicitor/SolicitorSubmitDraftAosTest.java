package uk.gov.hmcts.divorce.solicitor;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ClaimsCostFrom;
import uk.gov.hmcts.divorce.common.model.Court;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.divorce.solicitor.SolicitorCreateApplicationTest.getApplicant;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitDraftAos.SOLICITOR_DRAFT_AOS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class
})
public class SolicitorSubmitDraftAosTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    private final String docUrl = "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27";
    private final String docName = "draft-mini-application-1616591401473378.pdf";
    private final String docBinaryUrl = "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27/binary";

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void givenCaseDataWithDivorceApplicationWhenAboutToSubmitCallbackIsInvokedMiniapplicationlinkIsSet() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseDataWithDocument(DIVORCE_APPLICATION),
                        SOLICITOR_DRAFT_AOS)))
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.miniApplicationLink").isNotEmpty())
            .andExpect(jsonPath("$.data.miniApplicationLink.document_url").value(docUrl))
            .andExpect(jsonPath("$.data.miniApplicationLink.document_filename").value(docName))
            .andExpect(jsonPath("$.data.miniApplicationLink.document_binary_url").value(docBinaryUrl));
    }

    @Test
    void givenCaseDataWithoutDivorceApplicationWhenAboutToSubmitCallbackIsInvokedMiniapplicationlinkIsNotPresent() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseDataWithDocument(DOCUMENT_TYPE_RESPONDENT_INVITATION),
                        SOLICITOR_DRAFT_AOS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.miniApplicationLink").doesNotExist());
    }

    @Test
    void givenCaseDataWithoutDocumentsWhenAboutToSubmitCallbackIsInvokedMiniapplicationlinkIsNotPresent() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(
                objectMapper.writeValueAsString(
                    callbackRequest(caseDataWithoutDivorceApplication(),
                        SOLICITOR_DRAFT_AOS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.miniApplicationLink").doesNotExist());
    }

    private CaseData caseDataWithDocument(DocumentType documentType) {
        Document document = Document.builder()
            .url(docUrl)
            .filename(docName)
            .binaryUrl(docBinaryUrl)
            .build();

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentType(documentType)
            .documentFileName(docName)
            .documentLink(document)
            .build();

        ListValue<DivorceDocument> listValue = ListValue
            .<DivorceDocument>builder()
            .id("V2")
            .value(divorceDocument)
            .build();

        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .divorceCostsClaim(YES)
            .financialOrder(NO)
            .divorceClaimFrom(Set.of(ClaimsCostFrom.APPLICANT_2))
            .divorceUnit(Court.SERVICE_CENTRE)
            .selectedDivorceCentreSiteId("AA07")
            .documentsGenerated(List.of(listValue))
            .build();
    }

    private CaseData caseDataWithoutDivorceApplication() {
        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .divorceCostsClaim(YES)
            .financialOrder(NO)
            .divorceClaimFrom(Set.of(ClaimsCostFrom.APPLICANT_2))
            .divorceUnit(Court.SERVICE_CENTRE)
            .selectedDivorceCentreSiteId("AA07")
            .build();
    }

}