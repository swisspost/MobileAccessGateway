# EPR Primary System Integration

The REST variants of XDS transactions presented here are based on [CH EPR mHealth](https://fhir.ch/ig/ch-epr-mhealth/index.html),
itself based on various IHE profiles.

<svg width="20" height="20" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg"><path fill="#ffce31" d="M5.9 62c-3.3 0-4.8-2.4-3.3-5.3L29.3 4.2c1.5-2.9 3.9-2.9 5.4 0l26.7 52.5c1.5 2.9 0 5.3-3.3 5.3H5.9z"/><g fill="#231f20"><path d="m27.8 23.6l2.8 18.5c.3 1.8 2.6 1.8 2.9 0l2.7-18.5c.5-7.2-8.9-7.2-8.4 0"/><circle cx="32" cy="49.6" r="4.2"/></g></svg>
Don't forget to properly encode the HTTP parameters in the queries (especially colons and pipes).
In the following examples, they are shown decoded for a better readability.

<svg width="20" height="20" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg"><circle cx="24" cy="24" r="21" fill="#2196F3"/><path fill="#fff" d="M22 22h4v11h-4z"/><circle cx="24" cy="16.5" r="2.5" fill="#fff"/></svg>
For HTTP GET requests, there is usually an equivalent HTTP POST request that should be supported.

## Authentication

You should integrate one of the supported IDPs in your application.
The SAML flow is the only one currently supported.

In the REST transaction, the SAML assertion you got shall be [base64-url](https://datatracker.ietf.org/doc/html/rfc4648#section-5)
encoded, prefixed with `Bearer ` and inserted in an `Authorization` HTTP header.

```http title="Example of request with authentication"
GET / HTTP/1.1
Authorization: Bearer U0FNTGFzc2VydGlvbg
```

## Patient directory

The patient directory (called MPI) contains identifiers and demographics for all registered patients.
Identifiers include the MPI-PID and EPR-SPID, and identifiers used by primary systems that choose to share them.
Demographics include the given and family names, date of birth, gender, nationality and telecoms.
It can be queried and updated.

### Retrieve patient identifiers

Patient identifiers (commonly the MPI-PID and EPR-SPID) can be queried with an
[ITI-83 (_Mobile Patient Identifier Cross-reference Query_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-83.html).

The transaction is an HTTP GET request to the endpoint `/Patient/$ihe-pix`, with the following parameters:

1. sourceIdentifier (_[token][]_, mandatory): the known patient identifier
2. targetSystem (_[uri][]_, optional): to restrict the results to the MPI-PID and/or EPR-SPID.

<details><summary>Examples</summary>

```http title="To retrieve all known identifiers from a local identifier ('1234' in the system 2.999.42)"
GET /Patient/$ihe-pix?sourceIdentifier=urn:oid:2.999.42|1234 HTTP/1.1
```

```http title="To retrieve the EPR-SPID from the MPI-PID"
GET /Patient/$ihe-pix?sourceIdentifier=urn:oid:2.16.756.5.30.1.191.1.0.12.3.101|{mpi-pid}&targetSystem=urn:oid:2.16.756.5.30.1.127.3.10.3 HTTP/1.1
```

</details>

### Retrieve patient demographics

Patient demographics can be queried with an
[ITI-78 (_Mobile Patient Demographics Query_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-78.html).

The transaction can be done in two ways, either by specifying the MPI-PID to retrieve a single patient, or by
specifying other information.

=== "With the MPI-PID"

    If the MPI-PID is known, the transaction is an HTTP GET request to the endpoint `/Patient/{id}`, where {id} is
    the system OID and the identifier value, separated by a dash.

    ```http title="Retrieve by MPI-PID"
    GET /Patient/2.16.756.5.30.1.191.1.0.2.1-e7963774-9098-445f-9cab-5d52234b52c3 HTTP/1.1
    ```

    ```http title="Retrieve by EPR-SPID"
    GET /Patient/2.16.756.5.30.1.127.3.10.3-761337615866818761 HTTP/1.1
    ```

=== "With other information"

    Otherwise, parameters can be used to search patients with other information:

    - `family` and `given` (_[string][]_)
    - `identifier` (_[token][]_)
    - `telecom` (_[token][]_)
    - `birthdate` (_[date][]_)
    - `address` (_[string][]_): to search in any part of the address.
    - `address-city`,
      `address-country`,
      `address-postalcode`,
      `address-state` (_[string][]_)
    - `gender` (_[token][]_)

    ```http title="Example"
    GET /Patient?family=MOHR&given=ALICE&gender=female HTTP/1.1
    ```

### Feed patient information

Feeding patient information can be done with the [ITI-104 (_Patient Identity Feed FHIR_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-104.html).
The following profile shall be used: `http://fhir.ch/ig/ch-epr-mhealth/StructureDefinition-ch-pixm-patient.html`.

The MPI-PID is required in `identifier`.
You don't have to re-specify the other identifiers, they won't be deleted if they're missing from the request.
If you want to add an identifier, you can put it in `identifier`.

<details><summary>Examples</summary>

```http
PUT /Patient?identifier=urn:oid:2.16.756.5.30.1.196.3.2.1|MAGMED001 HTTP/1.1
Content-Type: application/fhir+json

{
    "active": true,
    "birthDate": "1987-10-08",
    "gender": "male",
    "id": "2.16.756.5.30.1.127.3.10.3-761337611735842172",
    "identifier": [
        {
            "system": "urn:oid:2.16.756.5.30.1.196.3.2.1",
            "value": "MAGMED001"
        },
        {
            "system": "urn:oid:2.999.42",
            "value": "new-identifier-value"
        }
    ],
    "name": [
        {
            "family": "NEFF-WINGEIER",
            "given": [
                "Trong Sang"
            ]
        }
    ],
    "resourceType": "Patient"
}
```

</details>

## Document directory

The document directory stores documents (in the document repository) and their metadata (in the document registry).

### Searching

You can search the document registry with the [ITI-67 (_Find Document References_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-67.html).

The transaction is an HTTP GET request to the endpoint `/DocumentReference`, the search parameters are described in
the [MHD ITI-67 specifications](https://profiles.ihe.net/ITI/MHD/ITI-67.html).

<details><summary>Examples</summary>

```http title="Search all documents from a patient"
GET /DocumentReference?patient.identifier=urn:oid:2.999|11111111&status=current HTTP/1.1
```

```http title="Search by class and type codes"
GET /DocumentReference?patient.identifier=urn:oid:2.999|11111111&category=http://snomed.info/sct|371531000&type=http://snomed.info/sct|419891008 HTTP/1.1
```

```http title="Search by creation date"
GET /DocumentReference?patient.identifier=urn:oid:2.999|11111111&creation=ge2023-07-10&creation=le2023-07-17 HTTP/1.1
```

</details>

You can also search for _SubmissionSets_ with the [ITI-66 (_Find Document Lists_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-66.html).

### Reading

Retrieving a document is done with the [ITI-68 (_Retrieve Document_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-68.html).
It is a simple HTTP GET request to a URL that you will find in the linked _DocumentReference_ (that you can obtain
with search results): `DocumentReference.content.attachment.url`.

### Publishing

You can publish a document with the [ITI-65 (_Provide Document Bundle_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-65.html).

The transaction is an HTTP POST request to the endpoint `/`. The following profile shall be used:
`https://profiles.ihe.net/ITI/MHD/StructureDefinition/IHE.MHD.Comprehensive.ProvideBundle`.

<details><summary>Examples</summary>

</details>

## Professional and organization directory

The HPD (Healthcare Provider Directory) contains information about the healthcare professionals and organizations that
are part of the EPR.
Relationships between them (i.e. membership of professionals to organizations, or relationships between
organizations) are also available.

### Searching

Professionals, organizations and relationships can be queried with an
[ITI-90 (_Find Matching Care Services_) transaction](https://fhir.ch/ig/ch-epr-mhealth/iti-90.html). See the
specifications for the complete list of search parameters.

=== "Professionals"

    For professionals, the endpoint is `/Practitioner`.

    ```http title="Search for 'Müller'"
    GET /Practitioner?family=Müller HTTP/1.1
    ```

    ```http title="Search by GLN"
    GET /Practitioner?identifier=urn:oid:2.51.1.3|7601000102737 HTTP/1.1
    ```

    ```http title="Retrieve from identifier"
    GET /Practitioner/DrPeterPan HTTP/1.1
    ```

=== "Organizations"

    For organizations, the endpoint is `/Organization`.

    ```http title="Search for active Organization whose name contains 'Medical'"
    GET /Organization?active=true&name:contains=Medical HTTP/1.1
    ```

    ```http title="Retrieve from identifier"
    GET /Organization/SpitalXDept3 HTTP/1.1
    ```

=== "Relationships"

    For relationships (memberships), the endpoint is `/PractitionerRole`.

    ```http title="Search for all professionals working at the 'HUG' organization"
    GET /PractitionerRole?organization=Organization/HUG&_include=PractitionerRole:practitioner HTTP/1.1
    ```

    ```http title="Retrieve from identifier"
    GET /PractitionerRole/PeterPanPraxisP HTTP/1.1
    ```

### Updating

The HPD update is not supported in a REST transaction. Please use the
[ITI-59 (_Provider Information Feed_) transaction](https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_Suppl_HPD.pdf).

## Audit messages

### Creating

For all transactions, it is required to send the same audit messages. You can use the regular ITI-20 transaction,
or use the [restful one](https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_Suppl_RESTful-ATNA.pdf).

See the [mapping from DICOM to FHIR](https://hl7.org/fhir/R4/auditevent-mappings.html#dicom).

<details><summary>Examples</summary>

```http
POST /ARR/fhir/AuditEvent HTTP/1.1
Content-Type: application/fhir+xml

<Bundle>
  <entry>
    ...
    <request>
      <method value="POST"/>
    </request>
  </entry>
</Bundle>
```

</details>

### Reading

You can read the audit messages for a given patient with an ITI-81 transaction.

!!! warning

    The endpoint for this transaction is the EPR community itself, not the MobileAccessGateway.
    This transaction is still implemented on a previous CH:ATC specification (March 2020), based on the [IHE Restfull
    ATNA supplement rev. 2.2](https://www.ihe.net/uploadedFiles/Documents/ITI/IHE_ITI_Suppl_RESTful-ATNA_Rev2.2_TI_2017-07-21.pdf).
    A lot have changed since.

The transaction is an HTTP GET request on the endpoint, with the parameter `entity-id` that contain the patient EPR-SPID,
and `date` to constraint the audit message date.
The `Authorization` header uses the prefix `IHE-SAML` and the SAML assertion is encoded with the [regular base64
alphabet](https://datatracker.ietf.org/doc/html/rfc4648#section-4).

<details><summary>Examples</summary>

```http
GET /ARR/fhir/AuditEvent?entity-id=urn:oid:2.16.756.5.30.1.127.3.10.3|{epr-spid}&date=ge2023-07-10&date=le2023-07-17 HTTP/1.1
Authorization: IHE-SAML Zm9vYmE=
```

</details>

[string]: http://hl7.org/fhir/R4/search.html#string "String search type"
[token]: http://hl7.org/fhir/R4/search.html#string "Token search type"
[date]: http://hl7.org/fhir/R4/search.html#date "Date search type"
[uri]: http://hl7.org/fhir/R4/search.html#uri "URI search type"