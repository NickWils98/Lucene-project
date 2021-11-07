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
        testAllQueries();
//        Test a single query
        //tester("what can help arteries dilate");
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

    public static void testAllQueries() throws IOException, ParseException {
        /*
            Go over all the queries and see if they are correct

         */

//        Parse the queries and solutions
        Map<String, String> dev_queries = Parser.parse("./resources/queries/small/dev_queries.tsv", "\t");
        Map<String, String> dev_queries_results = Parser.parse("./resources/queries/small/dev_query_results_small.csv", ",");

//        Counters
        int total_queries = 0;
        int correct_queries = 0;
        int incorrect_queries = 0;

//        Go over all the queries and test them
        for (var query_number : dev_queries_results.keySet()) {

//            Get the query
            String query = dev_queries.get(query_number);
//            Get best result
            String filename = search(query);

//            Get the number from the filename
            String number = filename.split("output_")[1];
            number = number.substring(0, number.length() - 4);

            if (number.equals(dev_queries_results.get(query_number))) {
                correct_queries++;
            } else {
                incorrect_queries++;
            }
            total_queries++;
        }
        System.out.println(total_queries + "   correct queries   " + correct_queries + "   incorrect queries   " + incorrect_queries);

    }

    public static String search(String input) throws IOException, ParseException {
        /*
            Search trough de Index map en find the best match for the query

            Return the best result

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
        int amount_best_results = 1;
        ScoreDoc[] hits = isearcher.search(query, amount_best_results).scoreDocs;

//        Iterate through the results:
        for (ScoreDoc hit : hits) {
            Document hitDoc = isearcher.doc(hit.doc);
        }

//        give the filename of the best result back
        Document hitDoc = isearcher.doc(hits[0].doc);
        String best_result = hitDoc.get("filename");
        ireader.close();
        directory.close();
        return best_result;
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
        String data = ("./resources/full_docs_small/");

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
