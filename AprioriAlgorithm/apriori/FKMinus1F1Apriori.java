package apriori;

import java.util.*;

public class FKMinus1F1Apriori<V> extends BaseApriori<V> {

	public FKMinus1F1Apriori(List<ItemSet<V>> transactions) {
		super(transactions);
	}

	@Override
	public List<ItemSet<V>> aprioriGen(List<ItemSet<V>> frequentCandidatesKMinus1) {

        Collections.sort(frequentCandidatesKMinus1);
        List<ItemSet<V>> levelCandidates = new ArrayList<ItemSet<V>>();

        // For each frequent item in F(k-1) and F(1) --> Union --> New candidate?

        for (ItemSet<V> FKMinus1 : frequentCandidatesKMinus1) {
            for (ItemSet<V> F1 : frequent1Itemsets) {

                //Check if c1 and c2 is different.
                // We want to generate a candidate containing only different items.

                if (FKMinus1.union(F1).size() == FKMinus1.size() + 1) {
                    ItemSet<V> candidate = FKMinus1.union(F1);

                    // Wont add duplicates to the candidate set
                    if (!levelCandidates.contains(candidate)){
                        levelCandidates.add(candidate);
                    }
                    getAndCacheSupportForItemset(candidate);
                }
            }
        }
        return new LinkedList<ItemSet<V>>(levelCandidates);
    }
}
