package apriori;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BruteForceApriori<V> extends BaseApriori<V> {

	public BruteForceApriori(List<ItemSet<V>> transactions) {
		super(transactions);
	}

	@Override
	public void apriori(Double minSupport) {


        //Får ut en liste med alle attributter (1-itemset)
        List<ItemSet<V>> singles = getAllItemsetsOfSizeOne();

        //prune all infrequent items in itemset
        List<ItemSet<V>> pruned = pruneInfrequentCandidates(minSupport,singles);
        frequent1Itemsets = pruned;
        frequentItemSets.put(1,pruned);

        //Sysout results
        System.out.println("Level: "+1+"\n"+"\tGenerated "+singles.size() + " candidates.\n");
        System.out.println("\t"+singles+"\n");
        System.out.println("\tAnd kept " + pruned.size() + " frequent sets\n \t" + pruned);

        //Candidates for higher levels
        List<ItemSet<V>> candidates = singles;

        for (int i = 2; ; i++){

            //Generate possible candidates
            List<ItemSet<V>> generatedCandidates = aprioriGen(candidates);

            System.out.println("\nLevel: "+i+"\n"+
                    "\tGenerated "+generatedCandidates.size() + " candidates.\n");
            System.out.print("\t"+generatedCandidates+"\n");

            //legge til settet i forrige sett
            candidates = generatedCandidates;

            //pruning
            pruned = pruneInfrequentCandidates(minSupport, candidates);

            System.out.print(
                            "\tAnd kept " + pruned.size() + " frequent sets\n" +
                            "\t" + pruned);

            //hvis det ikke er mer å prune så er vi ferdige
            if(pruned.size() == 0){
                break;
            }

            //Legge til i sett
            frequentItemSets.put(i,pruned);
        }
    }
}
