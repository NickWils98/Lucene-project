import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class searchEngine {

    //    path where the indexing is stored
    static final Path index = Paths.get("./Index/");
    //    Analyzer that is used
    static final Analyzer analyzer = new ClassicAnalyzer();


    public static void main(String[] args) throws IOException, ParseException {
        /*
            Calls all the needed functions

         */

//        Index all the input files
        index();
//        Test all the queries with the given solution
        writeMainQueries();
//        testAllQueries();
//        Test a single query
//        search("in chemical form what do subscripts tell you");
//        Delete the indexed input files for the next time
        deleteIndex();
    }

    public static void deleteIndex() {
        /*
        Delete the index files
         */

        File folder = new File("./Index/");

        File[] listOfFiles = folder.listFiles();
//        Go over all the files and delete them if there are any
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    public static void writeMainQueries() throws IOException, ParseException {
        /*
            Go over all the queries and output the results

         */

//            Get all the queries
        Map<String, List<String>> main_queries = Parser.parse("./resources/queries/main/queries.csv", "\t");
//            The parser where we open a new csv file
        Parser p = new Parser();
        p.makeNewCsv();
//            For each query -> find the top 10 results and write them to the csv file
        for (int i = 1; i < main_queries.size() + 1; i++) {
            String s = String.valueOf(i);
            List<String> querylist = main_queries.get(s);
            String query = querylist.get(0);
            List<String> results = search(query);
            p.write(results, s);
        }
//            close the csv file
        p.closeWriter();
    }

    public static void testAllQueries() throws IOException, ParseException {
        /*
            Go over all the queries and see if they are correct

         */

//        Parse the queries and solutions
        Map<String, List<String>> dev_queries = Parser.parse("./resources/queries/large/dev_queries.tsv", "\t");
        Map<String, List<String>> dev_queries_results = Parser.parse("./resources/queries/large/dev_query_results.csv", ",");

//        Counters
        int total_queries = 0;
        int correct_queries = 0;
        int incorrect_queries = 0;

//        Go over all the queries and test them
        for (var query_number : dev_queries_results.keySet()) {

//            Get the query
            List<String> querylist = dev_queries.get(query_number);
            String query = querylist.get(0);
//            Get best result
            List<String> filenameList = search(query);

//            Get the number from the filename
            try {
                for (String filename : filenameList) {
                    String number = filename.split("output_")[1];
                    number = number.substring(0, number.length() - 4);
                    List<String> results = dev_queries_results.get(query_number);

//                    Count the times we had the right output file
                    if (results.contains(number)) {
                        correct_queries++;

                    } else {
                        incorrect_queries++;

                    }
                    total_queries++;

                }
            } catch (Exception ignored) {
            }
        }
        System.out.println(total_queries + "   correct queries   " + correct_queries + "   incorrect queries   " + incorrect_queries);

    }

    public static List<String> search(String input) throws IOException, ParseException {
        /*
            Search trough de Index map en find the best match for the query

            Return the best 10 results

         */

        Directory directory = FSDirectory.open(index);
//        Search in content of documents with the given analyzer
        QueryParser parser = new QueryParser("content", analyzer);
//        Remove special symbols and give the query
        Query query = parser.parse(QueryParser.escape(input));
//        give the directory where you are going to search
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

//        Scoring system
        isearcher.setSimilarity(new BM25Similarity());

//        Search the query and given the n best results
        int amount_best_results = 10;
        ScoreDoc[] hits = isearcher.search(query, amount_best_results).scoreDocs;

//        Iterate through the results:
        List<String> best_resultlist = new ArrayList<String>();

        for (ScoreDoc hit : hits) {
            Document hitDoc = isearcher.doc(hit.doc);
            String best_result = hitDoc.get("filename");
            best_resultlist.add(best_result);
        }

        ireader.close();
        directory.close();
        return best_resultlist;
    }


    public static void index() throws IOException {
        /*
            Index all the output_xxx.txt files
         */

//        Store an index on disk
        Directory directory = FSDirectory.open(index);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
//        Where the files are stored
        String data = ("./resources/full_docs/");

//        open all the files
        File[] files = new File(data).listFiles();
        assert files != null;
        for (File file : files) {
//            Only look at txt files
            String extension = "";

            try {
                if (file != null && file.exists()) {
                    String name = file.getName();
                    extension = name.substring(name.lastIndexOf("."));
                }
            } catch (Exception e) {
                extension = "";
            }

//            If not a txt file ignore it
            if (extension.equals(".txt")) {
//                Make a new document for each file
                Document doc = new Document();
                String filepath = file.getPath();

//                Read all content from a file
                String content = Parser.readFile(filepath);

//                Store the content, filename and the path in the document
                Field contentField = new Field("content", content, TextField.TYPE_STORED);
                Field fileNameField = new Field("filename", file.getName(), TextField.TYPE_STORED);
                Field filePathField = new Field("filepath", file.getCanonicalPath(), TextField.TYPE_STORED);
                doc.add(contentField);
                doc.add(fileNameField);
                doc.add(filePathField);

//                Save the document
                iwriter.addDocument(doc);
            }
        }
        iwriter.close();
    }

}
