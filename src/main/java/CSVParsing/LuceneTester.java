package CSVParsing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

public class LuceneTester {

    public static final String FILE_NAME = "SLA_Classroom_Schedules_Fall_2019.csv";
    public static final String FILE_PATH = "src/main/resources/";
    public static final int MAX_SEARCH = 10;

    public static void main(String[] args) {
        LuceneTester tester = new LuceneTester();
        tester.createIndex();
    }

    private void createIndex() {
        try {
            Directory dir = FSDirectory.open(Paths.get(FILE_PATH));

            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);
            writer.deleteAll();

            for (Document doc : indexCSVDoc(writer, Paths.get(FILE_PATH + FILE_NAME))) {
                writer.addDocument(doc);
            }
            writer.commit();
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            // QUERY
            Query query = new QueryParser("Subject", new StandardAnalyzer()).parse("mangt");

            // SEARCH
            TopDocs topDocs = searcher.search(query, 763);
            ScoreDoc[] hits = topDocs.scoreDocs;
            System.out.println("Found " + hits.length + " hits.");

        } catch (IOException | ParseException e) {
            System.out.println(e);
        }
    }

    @NotNull
    private ArrayList<Document> indexCSVDoc(@NotNull IndexWriter writer, @NotNull Path file) {
        ArrayList<Document> indexedDocs = new ArrayList<>();
        try {
            BufferedReader fileReader = Files.newBufferedReader(file);
            String [] fields = fileReader.readLine().split("[,]");

            for (String line = fileReader.readLine(); line != null; line = fileReader.readLine()) {
                indexedDocs.add(indexCSVLine(fields, line.split("[,]")));
            }

            fileReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return indexedDocs;
    }

    @NotNull
    private Document indexCSVLine(@NotNull String [] fields, @NotNull String [] lineToIndex) {
        Document doc = new Document();
        for (int i = 0; i < fields.length; i++) {
            doc.add(new TextField(fields[i], lineToIndex[i], Field.Store.YES));
        }

        return doc;
    }
}