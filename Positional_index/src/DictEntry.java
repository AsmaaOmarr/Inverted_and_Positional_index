
import java.util.*;

public class DictEntry {

    int doc_freq = 0; // number of documents that contain term
    int term_freq = 0; // number of times the term is mentioned in the collection
    Posting postingList = null;
    List<Posting> pList;

}
