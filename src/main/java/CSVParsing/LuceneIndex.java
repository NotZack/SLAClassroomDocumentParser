package CSVParsing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Indexes all using lucene
 */
public class LuceneIndex {

    public static final String FILE_NAME = "SLA_Classroom_Schedules_Fall_2019.csv";
    public static final String FILE_PATH = "src/main/resources/";

    private String[] indexFields;
    private IndexSearcher searcher;


    public LuceneIndex() {
        createIndex();
    }

    /**
     * Creates a lucene index using the constant file name at the constant file path.
     */
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

    /**
     * Indexes the document's content into lucene that is at the given path
     * @param file The path of which to index the file's content
     * @return The indexed fields of the document
     */
    @NotNull
    private ArrayList<Document> indexCSVDoc(@NotNull Path file) {
        ArrayList<Document> indexedDocs = new ArrayList<>();
        try {
            BufferedReader fileReader = Files.newBufferedReader(file);
            indexFields = fileReader.readLine().split("[,]");

            for (String line = fileReader.readLine(); line != null; line = fileReader.readLine()) {

                String[] splitLine = new String[17];
                System.arraycopy(line.split("[,]", 16), 0, splitLine, 0, 16);

                splitLine[16] = splitLine[15].substring(splitLine[15].lastIndexOf(',') + 1);
                splitLine[15] = splitLine[15].substring(0, splitLine[15].lastIndexOf(',')).replace(',', ' ');

                indexedDocs.add(indexCSVLine(splitLine));
            }

            fileReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return indexedDocs;
    }

    /**
     * Indexes a single line of a document split by the lines fields into a lucene document
     * @param lineToIndex The field split raw line to be indexed
     * @return The indexed lucene document
     */
    @NotNull
    private Document indexCSVLine(@NotNull String [] lineToIndex) {
        Document doc = new Document();
        for (int i = 0; i < indexFields.length; i++) {
            doc.add(new TextField(indexFields[i], lineToIndex[i], Field.Store.YES));
        }

        return doc;
    }

    /**
     * Searches the lucene index for the given phrase then returns the results as a string.
     * @param clientQuery The inexact initial query text
     * @return The result of the parsed query
     */
    public String parseInexactQuery(@NotNull String clientQuery) {

        StringBuilder topResults = new StringBuilder();
        ArrayList<Integer> hitDocIndices = new ArrayList<>();
        ArrayList<String> filteredHits = new ArrayList<>();

        // Builds an exact query accounting for multi-word queries and leading/trailing whitespace
        try {
            if (clientQuery.endsWith(" ")) {
                clientQuery = clientQuery.substring(0, clientQuery.length() - 1);
            }
            if (clientQuery.contains(" ")) {
                clientQuery = clientQuery.replaceAll(" ", "* AND ");
            }
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

            //Gets rid of duplicate room results
            HashSet<String> setFilter = new HashSet<>(filteredHits);
            filteredHits.clear();
            filteredHits.addAll(setFilter);

            Collections.sort(filteredHits);

            //Combines results into a string
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

    /**
     * Given an exact room name, returns the room use schedule
     * @param roomName The room name to search for
     * @return The sorted room schedule with no duplicates
     */
    public String collectRoomData(String roomName) {
        ArrayList<Integer> hitDocIndices = new ArrayList<>();
        ArrayList<List<String>> unfilteredResults = new ArrayList<>();

        //Queries the lucene index for the exact room data
        roomName = roomName.replaceAll(" ", "* AND ");
        try {
            Query query = new QueryParser(indexFields[6], new StandardAnalyzer()).parse(roomName);
            ScoreDoc[] rawResults = searcher.search(query, 763).scoreDocs;
            for (ScoreDoc rawDoc : rawResults) {
                hitDocIndices.add(rawDoc.doc);
            }
            for (Integer rawDoc : hitDocIndices) {
                ArrayList<String> fieldsList = new ArrayList<>();
                searcher.doc(rawDoc).getFields().forEach((field) -> fieldsList.add(field.stringValue().trim()));
                unfilteredResults.add(fieldsList);
            }

            return sortRoomByStartTime(unfilteredResults).toString();

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sorts the room data according to the start times of the class sessions; removes duplicates and assumes
     * non-overlapping times.
     * @param unsortedList The list to sort start times
     * @return The sorted list of of rooms in order of class session start times
     */
    @NotNull
    @Contract("_ -> param1")
    private ArrayList<List<String>> sortRoomByStartTime(@NotNull ArrayList<List<String>> unsortedList) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        unsortedList.sort(Comparator.comparing(o -> {
            try {
                return timeFormat.parse(o.get(7));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            return null;
        }));
        return unsortedList;
    }
}