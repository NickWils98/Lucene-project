import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

public class ReadCSVExample2
{
    public Map<String, String> parse(String input)
    {
        Map<String, String> d = new Hashtable<>();
        String line = "";
        String splitBy = ",";
        try
        {
//parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(input));
            boolean first = false;
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                if(first) {
                    String[] employee = line.split(splitBy);    // use comma as separator
                    d.put(employee[0], employee[1]);
                }
                else{
                    first = true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return d;
    }
    public Map<String, String> parse2(String input)
    {
        Map<String, String> d = new Hashtable<>();
        String line = "";
        String splitBy = "\t";
        try
        {
//parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(input));
            boolean first = false;
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                if(first) {
                    String[] employee = line.split(splitBy);    // use comma as separator
                    String end = "";
                    for (String s : employee) {
                        if (!s.equals(employee[0])) {
                            end += s;
                        }
                    }
                    try {
                        d.put(employee[0], end);
                    } catch (Exception e) {
                    }
                }
                else{
                    first = true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return d;
    }
}  