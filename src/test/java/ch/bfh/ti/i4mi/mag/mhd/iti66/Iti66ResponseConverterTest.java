package ch.bfh.ti.i4mi.mag.mhd.iti66;

import ch.bfh.ti.i4mi.mag.Config;
import ch.bfh.ti.i4mi.mag.TestBase;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.xds.core.SampleData;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssigningAuthority;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Identifiable;
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class Iti66ResponseConverterTest extends TestBase {

    private final Iti66ResponseConverter _sut;

    @Autowired
    public Iti66ResponseConverterTest(Config config) {
        _sut = new Iti66ResponseConverter(config);
    }

    @Test
    public void translateToFhir_ValidInput_ValidTranslation() {

        var queryResponse = SampleData.createQueryResponseWithLeafClass(Status.SUCCESS, new Identifiable("some-id", new AssigningAuthority("1.2.3.4.5")));

        var fhirStuff = _sut.translateToFhir(queryResponse, null);

        // TODO verify translation -> Dmytro



    }
}