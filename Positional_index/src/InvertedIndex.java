import java.io.*;
import java.util.*;

public class InvertedIndex {
    private HashMap<String, DictEntry> index;
    private int[] docLengths;

    public InvertedIndex() {
        index = new HashMap<>();
        docLengths = new int[10];
    }

    public void buildIndex() {

        for (int i = 1; i <= 10; i++) {
            File file = new File("file" + i + ".txt");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int docId = i;
                int docLength = 0;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("[, .]");
                    docLength += words.length;
                    for (String word : words) {
                        String w = word.toLowerCase();
                        if (index.containsKey(w)) {
                            DictEntry entry = index.get(w);
                            entry.term_freq++;
                            Posting p = entry.postingList;
                            boolean found = false;
                            while (p != null) {
                                if (p.docId == docId) {
                                    p.dtf++;
                                    found = true;
                                    break;
                                }
                                p = p.next;
                            }
                            if (!found) {
                                Posting tmp = entry.postingList;
                                Posting newP = new Posting();
                                newP.docId = docId;
                                newP.dtf = 1;
                                if (docId < tmp.docId) {
                                    newP.next = tmp;
                                    tmp = newP;
                                } else {
                                    Posting current = tmp;
                                    while (current.next != null && current.next.docId < docId) {
                                        current = current.next;
                                    }
                                    newP.next = current.next;
                                    current.next = newP;
                                }
                                entry.doc_freq++;
                            }
                        } else {
                            DictEntry entry = new DictEntry();
                            entry.term_freq = 1;
                            Posting newP = new Posting();
                            newP.docId = docId;
                            // newP.dtf = 1;
                            entry.postingList = newP;
                            entry.doc_freq = 1;
                            index.put(w, entry);
                        }
                    }
                }
                docLengths[docId - 1] = docLength;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void search(String word) {
        DictEntry entry = index.get(word.toLowerCase());

        if (entry != null) {
            Posting p = entry.postingList;
            System.out.println("__Files containing the word '" + word.toLowerCase() + "__");
            System.out.println("");
            while (p != null) {
                int docTermFreq = p.dtf;
                System.out.println("file" + p.docId + ".txt" + " | Document term frequency : "
                        + docTermFreq + " | Doc len :" + docLengths[p.docId - 1]);
                p = p.next;
            }
            System.out.println("");
            System.out.println("Document frequency(The word exist in .. files): " + entry.doc_freq);
            System.out.println("Term frequency(The word frequency all over all files): " + entry.term_freq);
        } else {
            System.out.println("Word not found in the Files.");
        }
    }

    public void tfIdf(String query) {

        int N = 10;
        String[] terms = query.toLowerCase().split("\\W+");
        for(String term : terms) {
            DictEntry entry = index.get(term.toLowerCase());
            if (entry != null) {
                Posting p = entry.postingList;
                System.out.println("\n___Tf-Idf for "+term+"___");
                while (p != null) {
                    double NormalizedTf = (double) p.dtf / (double) docLengths[p.docId - 1];
                    double idf = Math.log10((double) N / (double) entry.doc_freq);
                    System.out
                            .println("between '" + term + "' and file" + p.docId + ".txt = "
                                    + (NormalizedTf * idf));
                    p = p.next;
                }
            }
            //System.out.println("\n");
        }
    }

    public void calculateCosineSimilarity(String query) {

        Map<String, Integer> queryVector = new HashMap<>();

        String[] terms = query.toLowerCase().split("\\W+");

        for (String word : terms) {
            queryVector.put(word, queryVector.getOrDefault(word, 0) + 1);
        }

        Map<String, Double> cosineSimilarities = new HashMap<>();

        for (int i = 1; i <= 10; i++) {
            Map<String, Integer> documentVector = new HashMap<>();
            File file = new File("file" + i + ".txt");
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNext()) {
                    String word = scanner.next().toLowerCase().replaceAll("\\W+", "");
                    documentVector.put(word, documentVector.getOrDefault(word, 0) + 1);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            double dotProduct = 0.0;
            double queryMagnitude = 0.0;
            double documentMagnitude = 0.0;

            for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {

                String word = entry.getKey();
                int queryFrequency = entry.getValue();
                int documentFrequency = documentVector.getOrDefault(word, 0);
                dotProduct += queryFrequency * documentFrequency;
                queryMagnitude += queryFrequency * queryFrequency;
            }
            for (int frequency : documentVector.values()) {
                documentMagnitude += frequency * frequency;
            }

            double cosineSimilarity = dotProduct / (Math.sqrt(queryMagnitude) *
                    Math.sqrt(documentMagnitude));
            cosineSimilarities.put("file" + i + ".txt", cosineSimilarity);
        }

        List<Map.Entry<String, Double>> sortedFiles = new ArrayList<>(cosineSimilarities.entrySet());
        sortedFiles.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        System.out.println("___Cosine similarity___");
        for (Map.Entry<String, Double> entry : sortedFiles) {
            String fileName = entry.getKey();
            double similarity = entry.getValue();
            System.out.println("Cosine similarity between query and " + fileName + ": " +
                    similarity);
        }
    }
}
