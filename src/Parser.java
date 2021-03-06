import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class Parser {
    FileWriter writer;

    static String readFile(String path)
            throws IOException
            /*
            Read a file to a string

            Return that string
             */ {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static Map<String, List<String>> parse(String input, String split_symbol)
            /*
            Read line by line a csv or tsv file and add each entry to a dictionary

            Return the dictionary
             */ {
        Map<String, List<String>> dictionary = new Hashtable<>();
        String line;
        try {
//        parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(input));
//            Don't use the headers
            boolean header = true;
//            Go over all the lines
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                if (!header) {
//                    Split the key and value
                    String[] entry = line.split(split_symbol);    // use comma as separator

                    StringBuilder value = new StringBuilder();
//                    If the value is split up -> merge
                    for (String s : entry) {
                        if (!s.equals(entry[0])) {
                            if (!s.equals(entry[1])) {
                                value.append("\t").append(s);
                            } else {
                                value.append(s);
                            }
                        }
                    }
//                    Add entry in the dictionary
                    try {
                        if (!dictionary.containsKey(entry[0])) {
                            List<String> value_list = new ArrayList<>();
                            value_list.add(value.toString());
                            dictionary.put(entry[0], value_list);
                        } else {
                            List<String> valuelist = dictionary.get(entry[0]);
                            valuelist.add(value.toString());
                            dictionary.put(entry[0], valuelist);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    header = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public void closeWriter() throws IOException {
        /*
        Close the writer

         */

        writer.flush();
        writer.close();
    }

    public void makeNewCsv() throws IOException {
        /*
        Write the header and open the csv file

         */

        writer = new FileWriter("result.csv");
        writer.append("Query number");
        writer.append("\t");
        writer.append("Document number");
        writer.append("\n");

    }

    public void write(List<String> results, String query_number) throws IOException {
        /*
        For each result find the file number and write the query_number and file_number

         */

        for (String result : results) {
            String number = result.split("output_")[1];
            number = number.substring(0, number.length() - 4);
            writer.append(query_number);
            writer.append("\t");
            writer.append(number);
            writer.append("\n");
        }

    }
}
