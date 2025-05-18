package model;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Service for browsing PubChem compound pages by CAS registry numbers.
 * @author Andrew Lightfoot
 */
public class PubChemService {
    /// Base URL for PubChem PUG REST API endpoints.
    private static final String PUB_CHEM_JSON_URL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/";
    // base URL for compound page
    private static final String PUB_CHEM_URL = "https://pubchem.ncbi.nlm.nih.gov/compound/";


    /**
     * Finds the PubChem compound ID for the specified CAS number and opens its page in the default browser.
     * If no match is found or an error occurs, displays a dialog with an appropriate message.
     *
     * @param cas the CAS registry number to lookup
     */
    public void browseByCas(String cas) {
        Optional<String> cidOptional= resolve("compound/xref/RN", cas);
        if (cidOptional.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No PubChem match for " + cas);
            return;
        }
        String cid = cidOptional.get();
        String url = PUB_CHEM_URL + cid;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(null, "Desktop has no supported browser.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open browser: " + e.getMessage());
        }
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
        HttpURLConnection connection = null;
        try {
            String encodedCAS = URLEncoder.encode(cas, StandardCharsets.UTF_8.name());
            URL url = new URL(PUB_CHEM_JSON_URL + endpoint + "/" + encodedCAS + "/cids/JSON");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return Optional.empty();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String json = stringBuilder.toString();
                int cidIndex = json.indexOf("CID");
                if (cidIndex < 0) {
                    return Optional.empty();
                }
                int startIndex = json.indexOf('[', cidIndex);
                int endIndex = json.indexOf(']', startIndex);
                if (startIndex < 0 || endIndex < 0) {
                    return Optional.empty();
                }
                String listString = json.substring(startIndex + 1, endIndex);
                String[] cids = listString.split(",");
                for (String cid : cids) {
                    String value = cid.trim();
                    if (!value.isEmpty()) {
                        return Optional.of(value);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.empty();
    }

    /**
     * Opens the PubChem SDS or general info page for a given CAS number in the default browser.
     * @param casNumber The CAS number of the chemical.
     */
    public void openSDSPage(String casNumber) {
        String url = "https://pubchem.ncbi.nlm.nih.gov/#query=" + casNumber;

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("Opening SDS info page for CAS: " + casNumber);
            } catch (IOException | URISyntaxException e) {
                System.err.println("Failed to open browser: " + e.getMessage());
            }
        } else {
            System.err.println("Desktop browsing not supported on this platform.");
        }
    }

}
