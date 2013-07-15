package gov.fcc.mmba;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import argo.format.JsonFormatter;
import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author smoghull Helper class to basically transform json into csv for mmba project
 */
public class JsonTransformer {

    private static final Logger logger = LoggerFactory.getLogger(JsonTransformer.class);
    private static final JdomParser JDOM_PARSER = new JdomParser();
    private static final JsonFormatter JSON_FORMATTER = new PrettyJsonFormatter();
    private static final List<String> ARRAY_ELEMENTS = Arrays.asList(new String[] { "conditions", "metrics", "tests" });
    private static final List<String> NEW_ARRAY_ELEMENTS = Arrays.asList(new String[] { "cdma_cell_location", "last_known_location", "location",
            "network_data", "gsm_cell_location" });
    private static String DELIMITER = ",";
    private static boolean CONVERT_TO_JSON_FLAG = false;
    private static boolean FORMAT_JSON_FLAG = false;
    private static final String TYPE = "type";
    private static String PARAM_EXPIRED_TYPE = "PARAM_EXPIRED";
    private static String NET_ACTIVITY_TYPE = "NETACTIVITY";
    private static String CPU_ACTIVITY_TYPE = "CPUACTIVITY";
    private static String GET_TYPE = "JHTTPGETMT";
    private static String POST_TYPE = "JHTTPPOSTMT";
    private static String LATENCY_TYPE = "JUDPLATENCY";
    private static String PHONE_IDENTITY_TYPE = "phone_identity";
    private static String NETWORK_DATA_TYPE = "network_data";
    private static String GSM_CELL_LOCATION_TYPE = "gsm_cell_location";
    private static String LOCATION_TYPE = "location";
    private static final String NULL = "\\N";
    private static final String NEW_LINE = "\n";

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        if (args.length == 0) {
            System.out.print("Please specify the input folder with json files -> ");
            File sourcefolder = new File(scan.nextLine());
            System.out.print("Please specify the output location folder for csv,json file  -> ");
            String destination = scan.nextLine();
            System.out.print(String.format("Please specify the delimiter for csv file (default is '%s') -> ", DELIMITER));
            String delimiter = scan.nextLine();
            if (!delimiter.trim().isEmpty())
                DELIMITER = delimiter;
            System.out.print(String.format("Would you like to restructure json files into one json file (defaulted to %s) -> ", (CONVERT_TO_JSON_FLAG) ? "YES" : "NO"));
            String convert = scan.nextLine();
            if (!convert.trim().isEmpty())
                CONVERT_TO_JSON_FLAG = checkYesNo(convert);
            System.out.print(String.format("Would you like to format restructed json file (defaulted to %s) -> ", (FORMAT_JSON_FLAG) ? "YES" : "NO" ));
            String format = scan.nextLine();
            if (!convert.trim().isEmpty())
                FORMAT_JSON_FLAG = checkYesNo(format);
            long startTime = System.currentTimeMillis();
            convert(sourcefolder, destination);
            long timeTaken = System.currentTimeMillis() - startTime;
            System.out.println("Processing time taken -> " + timeTaken);
        } else {
            if (args.length > 1) {
                File sourcefolder = new File(args[0]);
                String destination = args[1];
                if (args.length > 2)
                    DELIMITER = args[2];
                if (args.length > 3)
                    CONVERT_TO_JSON_FLAG = Boolean.valueOf(args[3]);
                if (args.length > 4)
                    FORMAT_JSON_FLAG = Boolean.valueOf(args[4]);
                long startTime = System.currentTimeMillis();
                convert(sourcefolder, destination);
                long timeTaken = System.currentTimeMillis() - startTime;
                System.out.println("Processing time taken -> " + timeTaken);
            } else {
                System.out.println("You need to provider atleast first two arguments delimited by space");
                System.out.println("Here is the arguments list");
                System.out
                        .println("input folder location, output folder location, delimiter(optional), concatenate json files(optional - true/false*), format(optional - true/false*");
            }
            scan.close();
        }
    }

    private static void convert(File source, String destination) {
        try {
        	new File(destination).mkdir();
        	
            File csvDestinationFile = new File(destination + File.separatorChar + "samknows." + System.currentTimeMillis() + ".csv");
            File jsonDestinationFile = new File(destination + File.separatorChar + "samknows." + System.currentTimeMillis() + ".json");

            Writer bwcsv = new BufferedWriter(new FileWriter(csvDestinationFile), 8 * 1024);
            Writer bwjson = new BufferedWriter(new FileWriter(jsonDestinationFile), 8 * 1024);

            File[] files = source.listFiles();
            for (final File fileEntry : files) {
                String json = "";
                if (!fileEntry.isDirectory()) {
                    json = readJsonFile(fileEntry.getAbsolutePath());
                    if (!json.isEmpty()) {
                        JsonObject jObject = transformStringToJsonObject(json, fileEntry.getAbsolutePath());
                        if (jObject != null) {
                            String csv = transformToCSV(jObject, fileEntry.getAbsolutePath());
                            bwcsv.write(csv + NEW_LINE);
                            if (CONVERT_TO_JSON_FLAG) {
                                if (FORMAT_JSON_FLAG) {
                                    JsonRootNode j = JDOM_PARSER.parse(json.toString());
                                    String formattedJson = JSON_FORMATTER.format(j);
                                    bwjson.write(formattedJson + NEW_LINE);
                                } else {
                                    bwjson.write(json + NEW_LINE);
                                }
                            }
                            else {
                            	jsonDestinationFile.deleteOnExit();
                            }
                        }
                    }
                } else {
                    convert(fileEntry, destination);
                }
            }
            bwcsv.close();
            bwjson.close();
        } catch (Exception e) {
            logger.error("Error while processing -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readJsonFile(String path) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            while (br.ready()) {
                sb.append(br.readLine());
            }
            br.close();
        } catch (Exception e) {
            logger.error("Error while reading file -> " + path);
            logger.error(e.getMessage());
        }
        return sb.toString();
    }

    private static JsonObject transformStringToJsonObject(String json, String fileName) {
        JsonParser parser = new JsonParser();
        JsonObject newJObject = new JsonObject();

        try {
            JsonObject oldJObject = (JsonObject) parser.parse(json);
            for (Entry<String, JsonElement> element : oldJObject.entrySet()) {
                if (ARRAY_ELEMENTS.contains(element.getKey())) {
                    JsonArray parent = (JsonArray) element.getValue();
                    JsonObject newParent = new JsonObject();
                    newJObject.add(element.getKey(), newParent);
                    for (JsonElement child : parent) {
                        String type = ((JsonObject) child).getAsJsonPrimitive(TYPE).getAsString();
                        if (NEW_ARRAY_ELEMENTS.contains(type)) {
                            if (newParent.getAsJsonArray(type) == null) {
                                JsonArray childArray = new JsonArray();
                                childArray.add(child);
                                newParent.add(type, childArray);
                            } else {
                                newParent.getAsJsonArray(type).add(child);
                            }
                        } else {
                            newParent.add(type, child);
                        }
                    }
                } else {
                    newJObject.add(element.getKey(), element.getValue());
                }
            }
        } catch (Exception e) {
            logger.error("Json parsing error -> " + fileName);
            logger.error(e.getMessage());
            return null;
        }
        return newJObject;
    }

    public static String transformToCSV(JsonObject jObject, String fileName) {
        StringBuilder sb = new StringBuilder();

        JsonObject conditions = jObject.getAsJsonObject("conditions");
        JsonObject metrics = jObject.getAsJsonObject("metrics");
        JsonObject tests = jObject.getAsJsonObject("tests");

        append(sb, fileName);
        extractBasicInformation(jObject, sb);
        extractConditions(conditions, sb);
        extractMetrics(metrics, sb);
        extractTests(tests, sb);

        String output = sb.toString();
        int length = output.length();
        return output.substring(0, length - 1);
    }

    private static void extractBasicInformation(JsonObject jObject, StringBuilder sb) {
        appendTimeStamp(sb, jObject);
        appendString(sb, jObject.get("schedule_config_version"));
        appendString(sb, jObject.get("app_version_code"));
        appendString(sb, jObject.get("sim_operator_code"));
        appendString(sb, jObject.get("enterprise_id"));
        appendString(sb, jObject.get("app_version_name"));
        appendString(sb, jObject.get("submission_type"));
        appendString(sb, jObject.get("_sourceip"));
    }

    private static void extractConditions(JsonObject conditions, StringBuilder sb) {
        extractParamExpired((JsonObject) conditions.get(PARAM_EXPIRED_TYPE), sb);
        extractNetActivity((JsonObject) conditions.get(NET_ACTIVITY_TYPE), sb);
        extractCpuActivity((JsonObject) conditions.get(CPU_ACTIVITY_TYPE), sb);
    }

    private static void extractMetrics(JsonObject metrics, StringBuilder sb) {
        extractPhoneIdentity((JsonObject) metrics.get(PHONE_IDENTITY_TYPE), sb);
        extractNetworkData(metrics.getAsJsonArray(NETWORK_DATA_TYPE), sb);
        extractGsmCellLocation(metrics.getAsJsonArray(GSM_CELL_LOCATION_TYPE), sb);
        extractLocation(metrics.getAsJsonArray(LOCATION_TYPE), sb);
    }

    private static void extractTests(JsonObject tests, StringBuilder sb) {
        extractGetOrPost((JsonObject) tests.get(GET_TYPE), sb);
        extractGetOrPost((JsonObject) tests.get(POST_TYPE), sb);
        extractLatency((JsonObject) tests.get(LATENCY_TYPE), sb);
    }

    private static void extractParamExpired(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendString(sb, json.get("success"));
        } else {
            appendNull(sb, 2);
        }
    }

    private static void extractNetActivity(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendString(sb, json.get("success"));
            appendBigDecimal(sb, json.get("bytesin"));
            appendBigDecimal(sb, json.get("maxbytesin"));
            appendBigDecimal(sb, json.get("bytesout"));
            appendBigDecimal(sb, json.get("maxbytesout"));
        } else {
            appendNull(sb, 6);
        }
    }

    private static void extractCpuActivity(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendString(sb, json.get("success"));
            appendBigDecimal(sb, json.get("max_average"));
            appendBigDecimal(sb, json.get("read_average"));

        } else {
            appendNull(sb, 4);
        }
    }

    private static void extractLocation(JsonArray array, StringBuilder sb) {
        int size = 0;
        if (array != null) {
            Iterator<JsonElement> it = array.iterator();
            size = array.size();
            while (it.hasNext()) {
                JsonObject json = (JsonObject) it.next();
                if (json != null) {
                    appendTimeStamp(sb, json);
                    appendBigDecimal(sb, json.get("longitude"));
                    appendBigDecimal(sb, json.get("latitude"));
                    appendString(sb, json.get("location_type"));
                    appendBigDecimal(sb, json.get("accuracy"));
                }
            }
        }
        for (; size < 4; size++) {
            appendNull(sb, 5);
        }
    }

    private static void extractGsmCellLocation(JsonArray array, StringBuilder sb) {
        int size = 0;
        if (array != null) {
            Iterator<JsonElement> it = array.iterator();
            size = array.size();
            while (it.hasNext()) {
                JsonObject json = (JsonObject) it.next();
                if (json != null) {
                    appendTimeStamp(sb, json);
                    appendBigDecimal(sb, json.get("umts_psc"));
                    appendBigDecimal(sb, json.get("signal_strength"));
                    appendBigDecimal(sb, json.get("location_area_code"));
                    appendBigDecimal(sb, json.get("bit_error_rate"));
                    appendString(sb, json.get("cell_tower_id"));
                }
            }
        }
        for (; size < 3; size++) {
            appendNull(sb, 6);
        }
    }

    private static void extractNetworkData(JsonArray array, StringBuilder sb) {
        int size = 0;
        if (array != null) {
            Iterator<JsonElement> it = array.iterator();
            size = array.size();
            while (it.hasNext()) {
                JsonObject json = (JsonObject) it.next();
                if (json != null) {
                    appendTimeStamp(sb, json);
                    appendString(sb, json.get("phone_type"));
                    appendBigDecimal(sb, json.get("phone_type_code"));
                    appendString(sb, json.get("sim_operator_code"));
                    appendBoolean(sb, json.get("connected"));
                    appendBigDecimal(sb, json.get("active_network_type_code"));
                    appendBigDecimal(sb, json.get("network_type_code"));
                    appendString(sb, json.get("network_type"));
                    appendString(sb, json.get("network_operator_name"));
                    appendString(sb, json.get("active_network_type"));
                    appendString(sb, json.get("network_operator_code"));
                    appendString(sb, json.get("sim_operator_name"));
                    appendBoolean(sb, json.get("roaming"));
                }
            }
        }
        for (; size < 3; size++) {
            appendNull(sb, 13);
        }

    }

    private static void extractPhoneIdentity(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendString(sb, json.get("model"));
            appendString(sb, json.get("manufacturer"));
            appendString(sb, json.get("os_version"));
            appendString(sb, json.get("os_type"));
        } else {
            appendNull(sb, 5);
        }
    }

    private static void extractLatency(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendBoolean(sb, json.get("success"));
            appendBigDecimal(sb, json.get("rtt_avg"));
            appendBigDecimal(sb, json.get("rtt_min"));
            appendBigDecimal(sb, json.get("rtt_max"));
            appendBigDecimal(sb, json.get("rtt_stddev"));
            appendBigDecimal(sb, json.get("received_packets"));
            appendBigDecimal(sb, json.get("lost_packets"));
            appendString(sb, json.get("target"));
            appendString(sb, json.get("target_ipaddress"));
        } else {
            appendNull(sb, 10);
        }

    }

    private static void extractGetOrPost(JsonObject json, StringBuilder sb) {
        if (json != null) {
            appendTimeStamp(sb, json);
            appendBoolean(sb, json.get("success"));
            appendBigDecimal(sb, json.get("number_of_threads"));
            appendBigDecimal(sb, json.get("warmup_bytes"));
            appendBigDecimal(sb, json.get("warmup_time"));
            appendBigDecimal(sb, json.get("transfer_bytes"));
            appendBigDecimal(sb, json.get("transfer_time"));
            appendBigDecimal(sb, json.get("bytes_sec"));
            appendString(sb, json.get("target"));
            appendString(sb, json.get("target_ipaddress"));

        } else {
            appendNull(sb, 10);
        }
    }

    private static void append(StringBuilder sb, Object obj) {
        sb.append(obj).append(DELIMITER);
    }

    private static void appendString(StringBuilder sb, JsonElement obj) {
        if (obj == null || obj instanceof JsonNull)
            appendNull(sb, 1);
        else
            sb.append(obj.getAsString()).append(DELIMITER);
    }

    private static void appendBigDecimal(StringBuilder sb, JsonElement obj) {
        if (obj != null)
            sb.append(obj.getAsBigDecimal()).append(DELIMITER);
        else
            appendNull(sb, 1);
    }

    private static void appendBoolean(StringBuilder sb, JsonElement obj) {
        if (obj != null)
            sb.append(obj.getAsBoolean()).append(DELIMITER);
        else
            appendNull(sb, 1);
    }

    private static void appendTimeStamp(StringBuilder sb, JsonObject obj) {
        if (obj != null) {
            Timestamp timestamp = new Timestamp(obj.get("timestamp").getAsLong() * 1000);
            append(sb, timestamp.toString());
        } else
            appendNull(sb, 1);
    }

    private static void appendNull(StringBuilder sb, int numOfFields) {
        for (int i = 0; i < numOfFields; i++)
            sb.append(NULL).append(DELIMITER);
    }
    
    /* http://www.javaprogrammingforums.com/java-programming-tutorials/9391-valid-user-input.html */
    private static boolean checkYesNo(String checking) {
    	String input = checking.toLowerCase().trim();
    	if (input.equals("yes") || input.equals("y")) {
    		return true;
    	}
    	//Behavioural: only a discrete 'yes' will return true.
    	//It does not recurse on invalid input because incorrect input
    	//is logically identical to the non-destructive 'no'
    	return false;
    }
}
