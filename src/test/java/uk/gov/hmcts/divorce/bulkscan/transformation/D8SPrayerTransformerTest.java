package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;

@ExtendWith(MockitoExtension.class)
public class D8SPrayerTransformerTest {

    @InjectMocks
    private D8SPrayerTransformer d8SPrayerTransformer;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyTransformSoleApplicationWithoutWarningsWhenDivorce() throws Exception {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8s-prayer-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = d8SPrayerTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/d8s-prayer-transformed.json", Application.class);

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);
    }

    @Test
    void shouldSuccessfullyTransformJointApplicationWithoutWarnings() throws Exception {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8s-prayer-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(JOINT_APPLICATION).divorceOrDissolution(DIVORCE).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = d8SPrayerTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/d8s-prayer-transformed.json", Application.class);

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);
    }

    @Test
    void shouldSuccessfullyTransformSoleApplicationWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-d8s-prayer-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = d8SPrayerTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review prayer in the scanned form"
            );
    }
}