package ch.bfh.ti.i4mi.mag.mhd.iti66;

import ch.bfh.ti.i4mi.mag.TestBase;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.xds.core.SampleData;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Iti66ResponseBugfixTest extends TestBase {

    private final Iti66ResponseBugfix _sut;
    private final ObjectFactory objectFactory;

    public Iti66ResponseBugfixTest() {

        _sut = new Iti66ResponseBugfix();
        objectFactory = new ObjectFactory();
    }

    @Test
    public void fixResponse_MissingClassificationType_AddNewClassificationTypeToInput() {

        var registryObjectList = new RegistryObjectListType();

        var toBeFixedInput = new AdhocQueryResponse();

        toBeFixedInput.setStartIndex(BigInteger.valueOf(1));
        toBeFixedInput.setRegistryObjectList(registryObjectList);
        toBeFixedInput.setTotalResultCount(BigInteger.valueOf(1));

        var adhocQueryResponse = _sut.fixResponse(toBeFixedInput);

        var registryPackages = adhocQueryResponse.getRegistryObjectList().getIdentifiable();

        assertionForRegistryPackages(registryPackages);
    }

    @Test
    public void fixResponse_ExistingClassificationType_InputIsReturnedUnmodified() {

        var registryObjectList = new RegistryObjectListType();
        var registryPackagesIdentifiables = registryObjectList.getIdentifiable();

        var registryPackageType = new RegistryPackageType();
        var newClassificationType = new ClassificationType();
        newClassificationType.setClassificationNode("urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd");
        newClassificationType.setClassifiedObject(registryPackageType.getId());
        registryPackageType.getClassification().add(newClassificationType);

        registryPackagesIdentifiables.add(objectFactory.createClassification(newClassificationType));

        var toBeFixedInput = new AdhocQueryResponse();

        toBeFixedInput.setStartIndex(BigInteger.valueOf(1));
        toBeFixedInput.setRegistryObjectList(registryObjectList);
        toBeFixedInput.setTotalResultCount(BigInteger.valueOf(1));

        var result = _sut.fixResponse(toBeFixedInput);
        var registryPackages = result.getRegistryObjectList().getIdentifiable();

        assertionForRegistryPackages(registryPackages);
    }

    private static void assertionForRegistryPackages(List<JAXBElement<? extends IdentifiableType>> registryPackages) {

        ClassificationType neededClassificationType = new ClassificationType();
        neededClassificationType.setClassificationNode("urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd");

        registryPackages.stream().forEach(possibleRegistryPackage -> {
            var type = possibleRegistryPackage.getValue();
            if(type instanceof RegistryPackageType){

                RegistryPackageType registryPackageType = (RegistryPackageType) type;

                neededClassificationType.setClassifiedObject(registryPackageType.getId());

                var classifications = registryPackageType.getClassification();

                assertEquals(1, classifications.stream().filter(classification -> classification.equals(neededClassificationType)).count());
        }});
    }
}