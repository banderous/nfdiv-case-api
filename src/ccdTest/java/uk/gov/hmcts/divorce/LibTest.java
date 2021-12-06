package uk.gov.hmcts.divorce;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.UserRoleController;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LibTest {

  @Autowired
  UserRoleController roleController;

  @Autowired
  MockMvc mockMvc;

  @MockBean
  IdamApi idamApi;

  @MockBean
  private WebMvcConfig webMvcConfig;

  private static final String JURISDICTION = "DIVORCE";
  private static final String CASE_TYPE = "NFD";
  private static final String SOLICITOR_CREATE = "solicitor-create-application";

  @Autowired
  protected CoreCaseDataApi coreCaseDataApi;

  MockMultipartFile loadNFDivDef() throws IOException {

    when(idamApi.retrieveUserInfo(anyString())).thenReturn(UserInfo.builder()
        .uid("1")
        .givenName("A")
        .familyName("Person")
        .roles(List.of())
        .build());

    return new MockMultipartFile(
        "file",
        "hello.txt",
        MediaType.MULTIPART_FORM_DATA_VALUE,
        getClass().getClassLoader().getResourceAsStream("NFD-dev.xlsx").readAllBytes()
    );
  }

  @Test
  void contextLoads() throws Exception {
    createRoles(
        "caseworker-divorce-courtadmin_beta",
        "caseworker-divorce-superuser",
        "caseworker-divorce-courtadmin-la",
        "caseworker-divorce-courtadmin",
        "caseworker-divorce-solicitor",
        "caseworker-divorce-pcqextractor",
        "caseworker-divorce-systemupdate",
        "caseworker-divorce-bulkscan",
        "caseworker-caa",
        "citizen"
    );

    mockMvc.perform(multipart("/import").file(loadNFDivDef())
            .with(
                jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
        )
        .andExpect(status().is2xxSuccessful());

    String solicitorToken = "green";
    String s2sTokenForCaseApi = "eggs";
    String solicitorUserId = "ham";
    StartEventResponse
        startEventResponse = startEventForCreateCase(solicitorToken, s2sTokenForCaseApi, solicitorUserId);

    CaseDataContent caseDataContent = CaseDataContent.builder()
        .eventToken(startEventResponse.getToken())
        .event(Event.builder()
            .id("solicitor-create-application")
            .summary("Create draft case")
            .description("Create draft case for functional tests")
            .build())
        .data(Map.of(
            "applicant1SolicitorName", "functional test",
            "applicant1LanguagePreferenceWelsh", "NO",
            "divorceOrDissolution", "divorce",
            "applicant1FinancialOrder", "NO"
        ))
        .build();

    submitNewCase(caseDataContent, solicitorToken, s2sTokenForCaseApi, solicitorUserId);
  }

  private StartEventResponse startEventForCreateCase(
      String solicitorToken,
      String s2sToken,
      String solicitorUserId
  ) throws Exception {

    MvcResult result = mockMvc.perform(
            get("/caseworkers/ham/jurisdictions/DIVORCE/case-types/NFD/event-triggers/solicitor-create-application/token")
                .with(jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
    var r = new ObjectMapper().readValue(result.getResponse().getContentAsString(), StartEventResponse.class);
    return r;
  }

  private void submitNewCase(
      CaseDataContent caseDataContent,
      String solicitorToken,
      String s2sToken,
      String solicitorUserId
  ) throws Exception {
    MvcResult result = mockMvc.perform(
            post("/caseworkers/ham/jurisdictions/DIVORCE/case-types/NFD/cases")
                .with(jwt().authorities(new SimpleGrantedAuthority("caseworker-divorce-solicitor")))
                .content(new ObjectMapper().writeValueAsString(caseDataContent))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  void createRoles(String... roles) {
    for (String role : roles) {
      UserRole r = new UserRole();
      r.setRole(role);
      r.setSecurityClassification(SecurityClassification.PUBLIC);
      roleController.userRolePut(r);
    }

  }

}
