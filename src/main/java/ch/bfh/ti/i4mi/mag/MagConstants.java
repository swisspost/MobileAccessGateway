package ch.bfh.ti.i4mi.mag;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MagConstants {

    @UtilityClass
    public static class FhirExtensionUrls {
        public static final String CH_AUTHOR_ROLE = "http://fhir.ch/ig/ch-epr-mhealth/StructureDefinition/ch-ext-author-authorrole";
        public static final String CH_DELETION_STATUS = "http://fhir.ch/ig/ch-epr-mhealth/StructureDefinition/ch-ext-deletionstatus";
        public static final String POST_REPOSITORY_UNIQUE_ID = "http://post.ch/e-health/windseeker/repositoryUniqueId";
        public static final String POST_DOCUMENT_ENTRY_VERSION = "http://post.ch/e-health/windseeker/documentEntryVersion";
    }

    @UtilityClass
    public static class XdsExtraMetadataSlotNames {
        public static final String CH_ORIGINAL_PROVIDER_ROLE = "urn:e-health-suisse:2020:originalProviderRole";
        public static final String CH_DELETION_STATUS = "urn:e-health-suisse:2019:deletionStatus";
    }

}
