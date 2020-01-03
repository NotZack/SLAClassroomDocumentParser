package CSVParsing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

public class LuceneIndex {

    public static final String FILE_NAME = "SLA_Classroom_Schedules_Fall_2019.csv";
    public static final String FILE_PATH = "src/main/resources/";

    private String[] indexFields;
    private IndexSearcher searcher;


    public LuceneIndex() {
        createIndex();
    }

    private void createIndex() {
        try {
            Directory dir = FSDirectory.open(Paths.get(FILE_PATH));

            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);
            writer.deleteAll();
            writer.commit();

            for (Document doc : indexCSVDoc(Paths.get(FILE_PATH + FILE_NAME))) {
                writer.addDocument(doc);
            }
            writer.commit();
            writer.close();

            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(FILE_PATH)));
            searcher = new IndexSearcher(reader);

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @NotNull
    private ArrayList<Document> indexCSVDoc(@NotNull Path file) {
        ArrayList<Document> indexedDocs = new ArrayList<>();
        try {
            BufferedReader fileReader = Files.newBufferedReader(file);
            indexFields = fileReader.readLine().split("[,]");

            for (String line = fileReader.readLine(); line != null; line = fileReader.readLine()) {
                indexedDocs.add(indexCSVLine(line.split("[,]")));
            }

            fileReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return indexedDocs;
    }

    @NotNull
    private Document indexCSVLine(@NotNull String [] lineToIndex) {
        Document doc = new Document();
        for (int i = 0; i < indexFields.length; i++) {
            doc.add(new TextField(indexFields[i], lineToIndex[i], Field.Store.YES));
        }

        return doc;
    }

    public String parseQuery(String clientQuery) {

        StringBuilder topResults = new StringBuilder();
        ArrayList<Integer> hitDocIndices = new ArrayList<>();
        ArrayList<String> filteredHits = new ArrayList<>();

        try {
            for (String indexField : indexFields) {
                Query query = new QueryParser(indexField, new StandardAnalyzer()).parse(clientQuery + "*");
                ScoreDoc[] rawResults = searcher.search(query, 763).scoreDocs;
                for (ScoreDoc rawDoc : rawResults) {
                    hitDocIndices.add(rawDoc.doc);
                }
            }
            
            for (Integer rawDoc : hitDocIndices) {
                filteredHits.add(searcher.doc(rawDoc).getField(indexFields[6]).stringValue());
            }

            HashSet<String> setFilter = new HashSet<>(filteredHits);
            filteredHits.clear();
            filteredHits.addAll(setFilter);

            Collections.sort(filteredHits);

            if (filteredHits.size() > 0) {
                int counter = 0;
                for (String buildingRoom : filteredHits) {

                    if (counter < 10) {
                        counter++;
                        topResults.append(buildingRoom).append(",");
                    }
                    else {
                        break;
                    }
                }
            }
            System.out.println("Found " + filteredHits.size() + " unique hits.");
        }
        catch (IOException | ParseException e) {
            System.out.println(e);
        }
        return topResults.toString().equals("") ? "No results found" : topResults.toString();
    }
}