package CSVParsing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
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
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDoc(writer, Paths.get(FILE_PATH + FILE_NAME));

            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryBuilder parser = new QueryBuilder(analyzer);
            Query q = parser.createPhraseQuery("contents", "MGMT");
            TopDocs docs = searcher.search(q, MAX_SEARCH);
            ScoreDoc[] scores = docs.scoreDocs;

            for (ScoreDoc score : scores) {
                System.out.println(score);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void indexDoc(@NotNull IndexWriter writer, @NotNull Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();

            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            doc.add(pathField);
            doc.add(new TextField("contents", fileReader));

            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                System.out.println("adding " + file);
                writer.addDocument(doc);
            }
            else {
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }

            fileReader.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}