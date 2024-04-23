package ch.bfh.ti.i4mi.mag.mhd.iti66;

import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ch.bfh.ti.i4mi.mag.TestBase;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.fhir.iti66_v401.Iti66SearchParameters;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.transform.requests.QueryRegistryTransformer;
import org.openehealth.ipf.commons.ihe.xds.core.validate.XDSMetaDataException;
import org.openehealth.ipf.commons.ihe.xds.core.validate.requests.AdhocQueryRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.openehealth.ipf.commons.ihe.xds.XDS.Interactions.ITI_18;

public class Iti66RequestConverterTest extends TestBase {

    @Autowired
    private Iti66RequestConverter _sut;
    private final QueryRegistryTransformer _queryRegistryTransformer;

    public Iti66RequestConverterTest() {
        _queryRegistryTransformer = new QueryRegistryTransformer();
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_ValidId_QueryValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                ._id(new TokenParam().setValue(UUID.randomUUID().toString()))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_ValidIdentifierWithUUIDPrefix_QueryValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .identifier(new TokenParam().setValue("urn:uuid:" + UUID.randomUUID()))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_ValidIdentifierOIDPrefix_QueryValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .identifier(new TokenParam().setValue("urn:oid:" + UUID.randomUUID()))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_ValidIdentifierNoPrefix_QueryNotValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .identifier(new TokenParam().setValue("some-invalid-prefix:" + UUID.randomUUID()))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);


        String exceptionMessage = "";
        try {
            var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
            AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
        } catch (XDSMetaDataException e) {
            exceptionMessage = e.getMessage();
        }
        assertTrue(exceptionMessage.contains("Missing required query parameter"));
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_SimpleQuery_QueryValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .code(new TokenParam().setValue("submissionset"))
                .patientIdentifier(new TokenParam().setSystem("urn:oid:1.2.3.4.5").setValue("some-value"))
                .status(new TokenOrListParam().add("current"))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_InvalidCode_ThrowsInvalidRequestException() {
        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .code(new TokenParam().setValue("some-random-value"))
                .build();

        checkException(iti66SearchParametersQuery, "Only search for submissionsets supported.");
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_InvalidPatientIdentifier_ThrowsInvalidRequestException() {
        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .code(new TokenParam().setValue("submissionset"))
                .patientIdentifier(new TokenParam().setValue("some-value"))
                .build();

        checkException(iti66SearchParametersQuery, "Missing OID for patient");
    }

    @Test
    public void searchParameterIti66ToFindSubmissionSetsQuery_FullQuery_QueryValidated() {

        var iti66SearchParametersQuery = Iti66SearchParameters.builder()
                .code(new TokenParam().setValue("submissionset"))
                .date(new DateRangeParam(new DateParam("2023-01-01"), new DateParam("2023-12-31")))
                .sourceFamily(new StringParam("some-family"))
                .sourceGiven(new StringParam("some-given"))
                .designationType(new TokenOrListParam().add("2.16.840.1.113883.6.96", "some-value"))
                .sourceId(new TokenOrListParam().add("some-value"))
                .status(new TokenOrListParam().add("current"))
                //.patientIdentifier(new TokenParam().setSystem("urn:oid:1.2.3.4.5").setValue("some-value"))
                .patientReference(new ReferenceParam().setValue("Patient?identifier=urn:oid:1.2.3.4.5.6|some-value"))
                .build();

        QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(iti66SearchParametersQuery);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(queryRegistry);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }


    private void checkException(Iti66SearchParameters params, String expectedMessage) {
        var exceptionMessage = "";
        try {
            QueryRegistry queryRegistry = _sut.searchParameterIti66ToFindSubmissionSetsQuery(params);
        }
        catch (InvalidRequestException ex){
            exceptionMessage = ex.getMessage();
        }

        assertTrue(exceptionMessage.contains(expectedMessage));
    }
}