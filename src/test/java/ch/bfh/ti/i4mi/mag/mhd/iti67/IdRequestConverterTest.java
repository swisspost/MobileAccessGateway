package ch.bfh.ti.i4mi.mag.mhd.iti67;
import ch.bfh.ti.i4mi.mag.TestBase;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.GetDocumentsQuery;
import org.openehealth.ipf.commons.ihe.xds.core.transform.requests.QueryRegistryTransformer;
import org.openehealth.ipf.commons.ihe.xds.core.validate.requests.AdhocQueryRequestValidator;

import static org.junit.jupiter.api.Assertions.*;
import static org.openehealth.ipf.commons.ihe.xds.XDS.Interactions.ITI_18;

public class IdRequestConverterTest extends TestBase{
    private final IdRequestConverter _sut;
    private final QueryRegistryTransformer _queryRegistryTransformer;

    public IdRequestConverterTest() {
        _sut = new IdRequestConverter();
        _queryRegistryTransformer = new QueryRegistryTransformer();
    }

    @Test
    public void idToGetDocumentQuery_MissingFhirHeader_null() {
        QueryRegistry result = _sut.idToGetDocumentsQuery("");
        assertNull(result);
    }

    @Test
    public void idToGetDocumentQuery_NotContainingSlash_null() {
        QueryRegistry result = _sut.idToGetDocumentsQuery("some-fhir-uri");
        assertNull(result);
    }

    @Test
    public void idToGetDocumentQuery_ValidFhirHttpUri_QueryValidated() {
        QueryRegistry result = _sut.idToGetDocumentsQuery("some-fhir-uri/12345");

        var uuid = ((GetDocumentsQuery) result.getQuery()).getUuids().get(0);
        assertTrue(result.getQuery() instanceof GetDocumentsQuery);
        assertEquals("urn:uuid:12345", uuid);

        var ebXmlResult = _queryRegistryTransformer.toEbXML(result);
        AdhocQueryRequestValidator.getInstance().validate(ebXmlResult, ITI_18);
    }

}