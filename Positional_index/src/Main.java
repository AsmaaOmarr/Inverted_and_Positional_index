import java.util.*;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        PositionalIndex positionalIndex = new PositionalIndex();
        positionalIndex.buildIndex();
        InvertedIndex index = new InvertedIndex();
        index.buildIndex();
        //index.search("dog");
        //index.tfIdf("lazy dog");
        System.out.print("Enter query : ");
        String query = input.nextLine();
        //index.search("dog");
        index.tfIdf(query);
        index.calculateCosineSimilarity(query);
        positionalIndex.search(query);
        positionalIndex.getPageLinks("https://www.wikipedia.org/");

    }
}