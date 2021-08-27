package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

class CaseworkerCourtAdminWithSolicitorAccessTest {

    @Test
    void shouldGrantCaseworkerCourtAdminWithSolicitorAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerCourtAdminWithSolicitorAccess().getGrants();

        assertThat(grants)
            .hasSize(9)
            .contains(
                entry(CASEWORKER_LEGAL_ADVISOR, R),
                entry(CASEWORKER_SUPERUSER, R),
                entry(SOLICITOR, R),
                entry(CASEWORKER_COURTADMIN_CTSC, C),
                entry(CASEWORKER_COURTADMIN_CTSC, R),
                entry(CASEWORKER_COURTADMIN_CTSC, U),
                entry(CASEWORKER_COURTADMIN_RDU, C),
                entry(CASEWORKER_COURTADMIN_RDU, R),
                entry(CASEWORKER_COURTADMIN_RDU, U)
            );
    }
}