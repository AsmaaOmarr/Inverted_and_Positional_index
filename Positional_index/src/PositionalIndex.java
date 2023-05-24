import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class PositionalIndex {

    private HashMap<String, DictEntry> index;
    private HashSet<String> links;
    public PositionalIndex() {
        index = new HashMap<>();
        links = new HashSet<>();
    }
    public void buildIndex() {

        for (int i = 1; i <= 10; i++) {
            File file = new File("file" + i + ".txt");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int docId = i;
                int position = 1;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("[, .]");
                    for (String word : words) {
                        String w = word.toLowerCase();

                        if (index.containsKey(w)) {
                            DictEntry entry = index.get(w);
                            entry.term_freq++;
                            List<Posting> postings = entry.pList;
                            boolean found = false;
                            for (Posting p : postings) {
                                if (p.docId == docId) {
                                    p.positions.add(position);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Posting newP = new Posting();
                                newP.docId = docId;
                                newP.positions.add(position);
                                postings.add(newP);
                                entry.doc_freq++;
                            }
                        } else {
                            DictEntry entry = new DictEntry();
                            entry.term_freq = 1;
                            Posting newP = new Posting();
                            newP.docId = docId;
                            newP.positions.add(position);
                            List<Posting> postings = new ArrayList<>();
                            postings.add(newP);
                            entry.pList = postings;
                            entry.doc_freq = 1;
                            index.put(w, entry);
                        }
                        position++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void search(String phrase) {

        String[] words = phrase.toLowerCase().split(" ");
        DictEntry firstEntry = index.get(words[0]);

        if (firstEntry != null) {

            List<Posting> result = new ArrayList<>(firstEntry.pList);

            for (int i = 1; i < words.length; i++) {
                String word = words[i];
                DictEntry entry = index.get(word);
                if (entry == null) {
                    result.clear();
                    break;
                }

                List<Posting> postings = entry.pList;
                List<Posting> tempResult = new ArrayList<>();

                int j = 0, k = 0;
                while (j < result.size() && k < postings.size()) {

                    Posting p1 = result.get(j);
                    Posting p2 = postings.get(k);

                    if (p1.docId == p2.docId) {

                        List<Integer> positions1 = p1.positions;
                        List<Integer> positions2 = p2.positions;

                        List<Integer> mergedPositions = mergePositions(positions1, positions2);

                        if (!mergedPositions.isEmpty()) {
                            Posting newP = new Posting();
                            newP.docId = p1.docId;
                            newP.positions = mergedPositions;
                            tempResult.add(newP);
                        }
                        j++;
                        k++;
                    } else if (p1.docId < p2.docId) {
                        j++;
                    } else {
                        k++;
                    }
                }

                result = tempResult;
            }

            if (!result.isEmpty()) {

                System.out.println("__Files containing the phrase '" + phrase.toLowerCase() + "__");
                System.out.println("");

                for (Posting p : result) {
                    int docId = p.docId;
                    System.out.println("file" + docId + ".txt");
                }

                System.out.println("");
                System.out.println("The phrase exists in " + result.size() + " files");
                System.out.println("");
            } else {
                System.out.println("Phrase not found in the Files.");
            }
        } else {
            System.out.println("Phrase not found in the Files.");
        }
    }

    private List<Integer> mergePositions(List<Integer> positions1, List<Integer> positions2) {
        List<Integer> mergedPositions = new ArrayList<>();
        int i = 0, j = 0;

        while (i < positions1.size() && j < positions2.size()) {
            int pos1 = positions1.get(i);
            int pos2 = positions2.get(j);

            if (pos2 - pos1 == 1) {
                mergedPositions.add(pos2);
                i++;
                j++;
            } else if (pos2 > pos1) {
                i++;
            } else {
                j++;
            }
        }

        return mergedPositions;
    }

    public void getPageLinks(String URL) {
        if (!links.contains(URL)) {
            try {
                if (links.add(URL)) {
                    System.out.println(URL);
                }
                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }
}
