import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.builders.MultiPhraseQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.PhraseQueryNodeBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
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
//        index();
//        Test all the queries with the given solution
//        testAllQueries();
//        Test a single query
//        search("what is a sales cycle process?");
//        Delete the indexed input files for the next time
//        deleteIndex();
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
        Map<String, List<String>> dev_queries = Parser.parse("./resources/queries/large/dev_queries.tsv", "\t");
        Map<String, List<String>> dev_queries_results = Parser.parse("./resources/queries/large/dev_query_results.csv", ",");

//        Counters
        int total_queries = 0;
        int correct_queries = 0;
        int incorrect_queries = 0;

//        Go over all the queries and test them
        for (var query_number : dev_queries_results.keySet()) {

//            Get the query
            List<String>querylist = dev_queries.get(query_number);
            String query = querylist.get(0);
//            Get best result
            List<String> filenameList = search(query);

//            Get the number from the filename
            try {
                boolean test = false;
                for (String filename : filenameList) {
                    String number = filename.split("output_")[1];
                    number = number.substring(0, number.length() - 4);
                    List<String> results = dev_queries_results.get(query_number);
                    if (results.contains(number)) {
                        test = true;
                    } else {
                    }
                }
                if(test){
                    correct_queries++;

                }else{
                    incorrect_queries++;

                }
                total_queries++;

            } catch (Exception e) {
            }
        }
        System.out.println(total_queries + "   correct queries   " + correct_queries + "   incorrect queries   " + incorrect_queries);

    }

    public static List<String> search(String input) throws IOException, ParseException {
        /*
            Search trough de Index map en find the best match for the query

            Return the best result

         */

        Directory directory = FSDirectory.open(index);
//        Search in content of documents with the given analyzer
        String[] queries = input.split(" ");
//

//        MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
//        for(String query : queries){
//            Term term = new Term("content", query);
//            builder.add(term);
////
//        }
//        builder.setSlop(99);
//        MultiPhraseQuery query = builder.build();
//
//        System.out.println("Query: " + parser.toString());
//        FuzzyQuery query = new FuzzyQuery(new Term("content", input), 2,0);
        QueryParser parser = new QueryParser("content", analyzer);
//        Remove special symbols and give the query
        Query query = parser.parse(QueryParser.escape(input));
//        give the directory where you are going to search
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

//        Scoring system
        isearcher.setSimilarity(new BooleanSimilarity());

//        Search the query and given the n best results
        int amount_best_results = 1;
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;

//        Iterate through the results:
        List<String> best_resultlist = new ArrayList<String>();

        for (ScoreDoc hit : hits) {
            Document hitDoc = isearcher.doc(hit.doc);
            String best_result = hitDoc.get("filename");
            best_resultlist.add(best_result);
//            System.out.println(best_result);
        }

//        give the filename of the best result back
//        Document hitDoc = isearcher.doc(hits[0].doc);
//        String best_result = hitDoc.get("filename");
//
//        try{
//            Document hitDoc = isearcher.doc(hits[].doc);
//            best_result.add(hitDoc.get("filename"));
//        } catch (Exception e){
//
//        }
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
