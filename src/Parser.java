import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;


public class Parser
{

    static String readFile(String path)
            throws IOException
            /*
            Read a file to a string

            Return that string
             */
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static Map<String, String> parse(String input, String split_symbol)
            /*
            Read line by line a csv or tsv file and add each entry to a dictionary

            Return the dictionary
             */
    {
        Map<String, String> dictionary = new Hashtable<>();
        String line;
        try
        {
//        parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(input));
//            Don't use the headers
            boolean header = true;
//            Go over all the lines
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                if(!header) {
//                    Split the key and value
                    String[] entry = line.split(split_symbol);    // use comma as separator

                    StringBuilder value = new StringBuilder();
//                    If the value is split up -> merge
                    for (String s : entry) {
                        if (!s.equals(entry[0])) {
                            if (!s.equals(entry[1])) {
                                value.append("\t").append(s);
                            }
                            else{
                                value.append(s);
                            }
                        }
                    }
//                    Add entry in the dictionary
                    try {
                        dictionary.put(entry[0], value.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    header = false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return dictionary;
    }
}
