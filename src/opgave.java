
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class opgave {
    public static void main(String[] args) throws IOException, ParseException {
        /*
            calll indexer
            call the searcher
            delete indexfiles
         */
        IndexWriter iwriter = index(); // Eerst alles indexen
        runall(iwriter);

        //tester("what can help arteries dilate", iwriter);
        File folder = new File("./Index/");
        File[] listOfFiles = folder.listFiles();
        for(File file: listOfFiles) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    public static void runall(IndexWriter iwriter) throws IOException, ParseException {
        /*
            go over all the queries and see if they are correct
         */
        ReadCSVExample2 parser = new ReadCSVExample2();
        Map<String, String > dev_queries = parser.parse2("./resources/queries/small/dev_queries.tsv"); // OK
        Map<String, String > dev_queries_results = parser.parse("./resources/queries/small/dev_query_results_small.csv"); // Niet OK

        int total = 0;int totalcoorect = 0;int totalfalse = 0;

        for(var query_number : dev_queries_results.keySet()){
            // get query
            String query = dev_queries.get(query_number); //TODO: Daar sta nog wel een /t voor, is dat ok?
            System.out.println(query);
            // get best result
            String filename = tester(query, iwriter);

            // Van de output gaan we dat opsplitsen zodat we enkel de nummer krijgen en met da nummerke kunnen we dan
            // in de dict vergelijken of da het nr is da we moesten hebben
            String filename2 = filename.split("output_")[1];
            filename2 = filename2.substring(0, filename2.length()-4);

            if(filename2.equals(dev_queries_results.get(query_number))) {
                totalcoorect++;
                System.out.println(totalcoorect+" Correct " + filename);
            } else{
//1077002
                totalfalse++;
                System.out.println(totalfalse+" You were wrong on file "+filename);
//                break;
            }
            total ++;


        }
        System.out.println(total);

    }

    public static String tester(String input, IndexWriter iwriter)throws IOException, ParseException {
        /*
            search trough de Index map en find the best match for the query

         */
        Analyzer analyzer = new StandardAnalyzer();

        Path index = Paths.get("./Index/");
        Directory directory = FSDirectory.open(index);
//        Document doc = new Document();
//        String text = "This is the text to be indexed.";
//        doc.add(new Field("fieldname",text, TextField.TYPE_STORED));
//        iwriter.addDocument(doc);
//        iwriter.close();

        // Now search the index:
        // Parse a simple query that searches for "text":
        // Ge gaat in contens zoeken
        QueryParser parser = new QueryParser("contents", analyzer);
        Query query = parser.parse(QueryParser.escape(input)); // Welke input da ge ga zoeken

        DirectoryReader ireader = DirectoryReader.open(directory); // In i reader nen dicteroy zetten
        IndexSearcher isearcher = new IndexSearcher(ireader); // Met die reader ook een searcher aanmaken
        isearcher.setSimilarity(new BM25Similarity());
        ScoreDoc[] hits = isearcher.search(query, 1).scoreDocs;
//        assertEquals(1, hits.length);
        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
//            System.out.println(isearcher.explain(query, hits[i].doc));

//            System.out.println(isearcher.search() );
            System.out.println(hitDoc.get("filepath")+"\n");
//            assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
        }


        Document hitDoc = isearcher.doc(hits[0].doc);
        String s =  hitDoc.get("filename");
        ireader.close();
        directory.close();
        return s;
    }

    //TODO: OK, buiten mss die string
    public static IndexWriter index()throws IOException, ParseException {
        /*
        index all the output_xxx.txt files
         */
        Analyzer analyzer = new StandardAnalyzer();

        // Store the index in memory:
        //Directory directory = new RAMDirectory();

        // To store an index on disk, use this instead:
        Path index = Paths.get("./Index/");                         // TODO: Moet daar een _ tussen of is Path een datatype??
        Directory directory = FSDirectory.open(index); // Openen index
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        String data = ("./resources/full_docs_small/"); //output stored
        int count = 100;
        File[] files = new File(data).listFiles();
        assert files != null;
        for (File file : files) {
            // enkel de txt files bekijken
            String extension = ".txt";

            try {
                if (file != null && file.exists()) {
                    String name = file.getName();
                    extension = name.substring(name.lastIndexOf("."));
                }
            } catch (Exception e) {
                extension = "";
            }

            if (extension.equals(".txt")) {
                count ++;
                Document doc = new Document();
                String filepath = file.getPath(); // relatief path van file
                String content = Files.readString(Paths.get(filepath)); // first string in file TODO aanpassen
//                FileReader x = new FileReader(file);
                Field contentField = new Field("contents", content, TextField.TYPE_STORED);
                Field fileNameField = new Field("filename", file.getName(), TextField.TYPE_STORED);
                Field filePathField = new Field("filepath", file.getCanonicalPath(), TextField.TYPE_STORED);
                doc.add(contentField);
                doc.add(fileNameField);
                doc.add(filePathField);
//            doc.add(new Field("fieldname",new FileReader(file), TextField.TYPE_STORED));

                iwriter.addDocument(doc);
            }
        }
        iwriter.close();
        // in iwirter per document geindexeerde content / filename/filepath
        System.out.println(count);
        return iwriter;

    }

}
