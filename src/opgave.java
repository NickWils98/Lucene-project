
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
import org.apache.lucene.search.similarities.BM25Similarity;
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
        IndexWriter iwriter = index();
        runall(iwriter);
//        tester("in chemical form what do subscripts tell you");
        File folder = new File("./Index/");
        File[] listOfFiles = folder.listFiles();
        for(File file: listOfFiles) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }


    }

    public static void runall(IndexWriter iwriter) throws IOException, ParseException {

        ReadCSVExample2 parser = new ReadCSVExample2();
        Map<String, String > r = parser.parse2("./resources/queries/small/dev_queries.tsv");
        Map<String, String > l = parser.parse("./resources/queries/small/dev_query_results_small.csv");
        int total = 0;int totalcoorect = 0;int totalfalse = 0;
        for(var k : l.keySet()){
            String query = r.get(k);
            System.out.println(query);
            String filename = tester(query, iwriter);
            String filename2 = filename.split("output_")[1];
            filename2 = filename2.substring(0, filename2.length()-4);
            if(filename2.equals(l.get(k))) {
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
    }

    public static String tester(String input, IndexWriter iwriter)throws IOException, ParseException {
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
        QueryParser parser = new QueryParser("contents", analyzer);
        Query query = parser.parse(input);
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
//        isearcher.setSimilarity(new BM25Similarity());
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

    public static IndexWriter index()throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();

        // Store the index in memory:
//    Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        Path index = Paths.get("./Index/");
        Directory directory = FSDirectory.open(index);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        String data = ("./resources/full_docs_small/");

        File[] files = new File(data).listFiles();
        assert files != null;
        for (File file : files) {

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
                Document doc = new Document();
                String filename = file.getPath();
                String content = Files.readString(Paths.get(filename));
                FileReader x = new FileReader(file);
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
        return iwriter;

    }

}