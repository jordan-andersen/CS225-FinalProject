package model;

import java.util.Optional;

/**
 * Service for browsing PubChem compound pages by CAS registry numbers.
 */
public class PubChemService {
    /// Base URL for PubChem PUG REST API endpoints.
    private static final String PUB_CHEM_URL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/";

    /**
     * Finds the PubChem compound ID for the specified CAS number and opens its page in the default browser.
     * If no match is found or an error occurs, displays a dialog with an appropriate message.
     *
     * @param cas the CAS registry number to lookup
     */
    public void browseByCas(String cas) {
        /*
         - call resolve("compound/xref/RN", cas)
         - if returned CID is empty
            - show dialog "No PubChem match for " + cas
            - return
         - construct URL "https://pubchem.ncbi.nlm.nih.gov/compound/" + cid
         - try to open default browser at that URL
            - on exception, show dialog with error message
         */
    }

    /**
     * Queries the specified PubChem PUG REST endpoint for the given CAS number to retrieve a PubChem CID.
     * Parses the JSON response to extract the first CID value.
     * Returns empty if the HTTP response is not 200 or no CID is found.
     *
     * @param endpoint the PUG REST API endpoint (for example "compound/xref/RN")
     * @param cas the CAS registry number to resolve
     * @return an Optional containing the first found PubChem CID, or empty if unavailable
     */
    private Optional<String> resolve(String endpoint, String cas) {
        /*
         - build request URL = BASE + endpoint + "/" + URL-encode(cas) + "/cids/JSON"
         - open HTTP connection to URL
         - if response code ≠ 200
            - return empty Optional
         - read entire response body into a single JSON string
         - find index of "\"CID\"" in JSON
         - if not found
            - return empty Optional
         - locate '[' after that index and matching ']'
         - if '[' or ']' is missing
            - return empty Optional
         - extract substring between '[' and ']'
         - split on commas, trim each part, discard empty strings
         - return first trimmed value wrapped in Optional
         - if any IOException occurs
            - return empty Optional
         */
        return Optional.empty();
    }

}
