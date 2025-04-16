package utils;

import config.Config;
import model.TenantData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import scaletest.CdoDataIngestion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CdoAuthUtils {
    private static final String API_TOKENS_FILE = Config.get("api.tokens.file", "api_user_tokens.csv");
    private static final String DEVICE_LIST_FILE  = Config.get("api.device_list.file", "device_uuids.csv");

    public static List<TenantData> getCdoTokenList() throws Exception{
        List<TenantData> columnValues = new ArrayList<>();
        // Load the file from the resources folder
        try (InputStream inputStream = CdoDataIngestion.class.getClassLoader().getResourceAsStream(API_TOKENS_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + API_TOKENS_FILE);
            }

            // Read and parse the CSV file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()  // Use the first row as headers
                        .parse(reader);

                // Iterate through records and get the desired column values
                for (CSVRecord record : csvParser) {
                    TenantData data = new TenantData();
                    data.setToken(record.get("API_USER_TOKEN"));
                    data.setUuid(record.get("tenant_id"));
                    columnValues.add(data);
                }
            }
        }
        return columnValues;
    }

    public static ArrayList<String> getDeviceList() throws Exception{
        ArrayList<String> columnValues = new ArrayList<>();
        // Load the file from the resources folder
        try (InputStream inputStream = CdoDataIngestion.class.getClassLoader().getResourceAsStream(DEVICE_LIST_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + DEVICE_LIST_FILE);
            }

            // Read and parse the CSV file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()  // Use the first row as headers
                        .parse(reader);

                // Iterate through records and get the desired column values
                for (CSVRecord record : csvParser) {
                    TenantData data = new TenantData();
                    columnValues.add(record.get("device_uuid"));
                }
            }
        }
        return columnValues;
    }
}
