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

        /*
        * Får ut en liste med single transactions
        * */
        List<ItemSet<V>> singles = getAllItemsetsOfSizeOne();

        /*
         * pruning
         * */
        List<ItemSet<V>> pruned = pruneInfrequentCandidates(minSupport,singles);

        System.out.println("Level: "+1+"\n"+"\tGenerated "+singles.size() + " candidates.\n");
        System.out.print("\t"+singles+"\n");
        System.out.print(
                "\tAnd kept " + pruned.size() + " frequent sets\n" +
                        "\t" + pruned);
        frequent1Itemsets = pruned;
        frequentItemSets.put(1,pruned);

        //Candidates for lower levels
        List<ItemSet<V>> candidates = singles;

        for (int i = 2; ; i++){

            //Generate possible candidates
            List<ItemSet<V>> generatedCandidates = aprioriGen(candidates);

            System.out.println("Level: "+i+"\n"+
                    "\tGenerated "+generatedCandidates.size() + " candidates.\n");
            System.out.print("\t"+generatedCandidates+"\n");

            //legge til settet i forrige sett
            candidates = generatedCandidates;

            //pruning
            pruned = pruneInfrequentCandidates(minSupport,generatedCandidates);

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
