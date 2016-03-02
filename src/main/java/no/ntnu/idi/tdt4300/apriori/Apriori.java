package no.ntnu.idi.tdt4300.apriori;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import com.sun.tools.doclets.formats.html.SplitIndexWriter;
import org.apache.commons.cli.*;git
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.*;

/**
 * This is the main class of the association rule generator.
 * <p>
 * It's a dummy reference program demonstrating the accepted command line arguments, input file format and standard output
 * format also required from your implementation. The generated standard output follows the CSV (comma-separated values) format.
 * <p>
 * It's up to you if you use this program as your base, however, it's very important to strictly follow the given formatting
 * of the inputs and outputs. Your assignment will be partly automatically evaluated, therefore keep the input arguments
 * and output format identical.
 * <p>
 * Alright, I believe it's enough to stress three times the importance of the input and output formatting. Four times...
 *
 * @author tdt4300-undass@idi.ntnu.no
 */
public class Apriori {

    List<SortedSet<String>> frequentSets;

    /**
     * Loads the transaction from the ARFF file.
     *
     * @param filepath relative path to ARFF file
     * @return list of transactions as sets
     * @throws java.io.IOException signals that I/O error has occurred
     */
    public static List<SortedSet<String>> readTransactionsFromFile(String filepath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        List<String> attributeNames = new ArrayList<String>();
        List<SortedSet<String>> itemSets = new ArrayList<SortedSet<String>>();

        String line = reader.readLine();
        while (line != null) {
            if (line.contains("#") || line.length() < 2) {
                line = reader.readLine();
                continue;
            }
            if (line.contains("attribute")) {
                int startIndex = line.indexOf("'");
                if (startIndex > 0) {
                    int endIndex = line.indexOf("'", startIndex + 1);
                    attributeNames.add(line.substring(startIndex + 1, endIndex));
                }
            } else {
                SortedSet<String> is = new TreeSet<String>();
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                int attributeCounter = 0;
                String itemSet = "";
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken().trim();
                    if (token.equalsIgnoreCase("t")) {
                        String attribute = attributeNames.get(attributeCounter);
                        itemSet += attribute + ",";
                        is.add(attribute);
                    }
                    attributeCounter++;
                }
                itemSets.add(is);
            }
            line = reader.readLine();
        }
        reader.close();

        return itemSets;
    }

    /**
     * Generates the frequent itemsets given the support threshold. The results are returned in CSV format.
     *
     * @param transactions list of transactions
     * @param support      support threshold
     * @return frequent itemsets in CSV format with columns size and items; columns are semicolon-separated and items are comma-separated
     */
    public static String generateFrequentItemsets(List<SortedSet<String>> transactions, double support) {
        // TODO: Generate and print frequent itemsets given the method parameters.

        String frequentItemSets = "size;items\n";


        Map map = new HashMap<String, Integer>();
        List<SortedSet<String>> listOfFrequentItemSets = new ArrayList<SortedSet<String>>();
        SortedSet<String> oneItemSet = new TreeSet<String>();
        int numOfTransactions = 0;

        for (int i = 0; i < transactions.size(); i++) {
                Iterator<String> iterator = transactions.get(i).iterator();
                String str;
                numOfTransactions = i;
                while(iterator.hasNext()){
                    str = iterator.next();
                    if (!map.containsKey(str)) {
                        map.put(str, 1);
                    } else if(map.containsKey(str)){
                        map.put(str, (Integer) map.get(str)+1);
                    }
                }
        }
        double minsup = support * numOfTransactions;

        for (Object o : map.keySet()) {

            if ((Integer) map.get(o) >= minsup) {
                oneItemSet.add((String) o);

            }
        }
        for (String str: oneItemSet) {
            SortedSet<String> strSet = new TreeSet<String>();
            strSet.add(str);
            listOfFrequentItemSets.add(strSet);
        }

        List<SortedSet<String>> nextItemSet;

        if(oneItemSet.size() > 0){
            nextItemSet = generate2itemsets(oneItemSet);
        }else{
            return frequentItemSets;
        }

        List<SortedSet<String>> nextFreqSet = findFreqSets(nextItemSet, transactions, minsup);

        listOfFrequentItemSets.addAll(nextFreqSet);

        int sizeOfFreq = nextFreqSet.size();

        while(sizeOfFreq > 0){

            nextFreqSet = generateNItemsets(nextFreqSet, oneItemSet);
            nextFreqSet = findFreqSets(nextFreqSet, transactions, minsup);


            listOfFrequentItemSets.addAll(nextFreqSet);

            sizeOfFreq = nextFreqSet.size();

        }


        return frequentItemsToString(listOfFrequentItemSets);
    }

    /**
     * Generates the association rules given the support and confidence threshold. The results are returned in CSV
     * format.
     *
     * @param transactions list of transactions
     * @param support      support threshold
     * @param confidence   confidence threshold
     * @return association rules in CSV format with columns antecedent, consequent, confidence and support; columns are semicolon-separated and items are comma-separated
     */
    public static String generateAssociationRules(List<SortedSet<String>> transactions, double support, double confidence) {
        // TODO: Generate and print association rules given the method parameters.


        String frequentSets = generateFrequentItemsets(transactions, support);

        System.out.println(stringToSets(generateFrequentItemsets(transactions, support)));

        return "antecedent;consequent;confidence;support\n" +
                "diapers;beer;0.6;0.5\n" +
                "beer;diapers;1.0;0.5\n" +
                "diapers;bread;0.8;0.67\n" +
                "bread;diapers;0.8;0.67\n" +
                "milk;bread;0.8;0.67\n" +
                "bread;milk;0.8;0.67\n" +
                "milk;diapers;0.8;0.67\n" +
                "diapers;milk;0.8;0.67\n" +
                "diapers,milk;bread;0.75;0.5\n" +
                "bread,milk;diapers;0.75;0.5\n" +
                "bread,diapers;milk;0.75;0.5\n" +
                "bread;diapers,milk;0.6;0.5\n" +
                "milk;bread,diapers;0.6;0.5\n" +
                "diapers;bread,milk;0.6;0.5\n";
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // definition of the accepted command line arguments
        Options options = new Options();
        options.addOption(Option.builder("f").argName("file").desc("input file with transactions").hasArg().required(true).build());
        options.addOption(Option.builder("s").argName("support").desc("support threshold").hasArg().required(true).build());
        options.addOption(Option.builder("c").argName("confidence").desc("confidence threshold").hasArg().required(false).build());
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            // extracting filepath and support threshold from the command line arguments
            String filepath = cmd.getOptionValue("f");
            double support = Double.parseDouble(cmd.getOptionValue("s"));

            // reading transaction from the file
            List<SortedSet<String>> transactions = readTransactionsFromFile(filepath);

            if (cmd.hasOption("c")) {
                // extracting confidence threshold
                double confidence = Double.parseDouble(cmd.getOptionValue("c"));

                // printing generated association rules
                System.out.println(generateAssociationRules(transactions, support, confidence));
            } else {
                // printing generated frequent itemsets
                System.out.println(generateFrequentItemsets(transactions, support));
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setOptionComparator(null);
            helpFormatter.printHelp("java -jar apriori.jar", options, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<SortedSet<String>> generate2itemsets(SortedSet<String> smallerItemSet) {
        List<SortedSet<String>> twoItemSet = new ArrayList<SortedSet<String>>();

        for (Object o : smallerItemSet) {
            for (Object obj: smallerItemSet) {
                if (!(o.equals(obj))){

                    SortedSet<String> tempTwoSet = new TreeSet<String>();
                    tempTwoSet.add((String) o);
                    tempTwoSet.add((String) obj);
                    if(!twoItemSet.contains(tempTwoSet))
                        twoItemSet.add(tempTwoSet);
                }
            }
        }

        return twoItemSet;
    }
    
    private static List<SortedSet<String>> generateNItemsets(List<SortedSet<String>> smallerItemSet, SortedSet<String> oneItemSets){
        
        List<SortedSet<String>> newItemSets = new ArrayList<SortedSet<String>>();


        for (SortedSet set: smallerItemSet) {
            for (String str: oneItemSets) {
                SortedSet<String> oneItem = new TreeSet<String>();
                oneItem.add(str);
                if(!isSubset(oneItem, set)){
                    //System.out.println("Added itemset");
                    SortedSet<String> newItemSet = new TreeSet<String>();

                    newItemSet.addAll(set);
                    newItemSet.add(str);
                    if(!newItemSets.contains(newItemSet))
                    newItemSets.add(newItemSet);
                }
            }
        }
        
        return newItemSets;
    }

    public static boolean isSubset(SortedSet<String> a, SortedSet<String> b){



        for (String str: a) {
            int similarCount = 0;
            for (String string : b) {
                if(str.equals(string)){
                    similarCount++;
                }
            }
            if (similarCount==0){
                return false;
            }
        }
        return true;
    }
    private static List<SortedSet<String>> findFreqSets(List<SortedSet<String>> itemSets, List<SortedSet<String>> trans, double minsup) {

        HashMap<SortedSet<String>, Integer> itemCount = new HashMap<SortedSet<String>, Integer>();

        List<SortedSet<String>> outList = itemSets;

        for (SortedSet set: itemSets) {
            itemCount.put(set, 0);
        }

        for (SortedSet<String> set: itemSets) {
            for (SortedSet<String> tran: trans) {
                if(isSubset(set,tran)){
                    itemCount.put(set, itemCount.get(set)+1);
                }
            }
        }

        for (Object o: itemCount.keySet()) {
            if(itemCount.get(o) < minsup){
                outList.remove(o);
            }
        }

        return outList;

    }

    private static String frequentItemsToString(List<SortedSet<String>> sets){
        String outString = "";

        for (SortedSet set: sets) {
            outString += set.size()+";";
            Iterator it = set.iterator();
            while (it.hasNext()){
                String tempString = (String) it.next();
                if(it.hasNext())
                outString += tempString+",";
                else{
                    outString += tempString+"\n";
                }
            }
        }
        return outString;
    }

    private static List<SortedSet<String>> stringToSets(String str){

        List<SortedSet<String>> freqSets = new ArrayList<SortedSet<String>>();
        String[] lines;


        lines = str.split("\n");

        System.out.println(lines);

        /*for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i)==';' && Integer.parseInt(str.charAt(i-1)+"") == 1){
                String item = str.substring(i+1, str.indexOf('\n'));
            }else if(str.charAt(i)==';' && Integer.parseInt(str.charAt(i-1)+"") > 1){

            }
        }*/

        return null;

    }

}
