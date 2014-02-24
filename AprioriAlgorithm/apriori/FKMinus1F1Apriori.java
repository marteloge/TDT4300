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
        for (int i = 0; i < frequentCandidatesKMinus1.size(); i++) {
            for (int j = 0; j < frequent1Itemsets.size(); j++) {

                ItemSet<V> c1 = frequentCandidatesKMinus1.get(i);
                ItemSet<V> c2 = frequent1Itemsets.get(j);

                System.out.println("C1: " + c1);
                System.out.println("C2: " + c2);
                System.out.println("Union: " + c1.union(c2));

                if (c1.union(c2).size() == c1.size() + 1) {
                    ItemSet<V> candidate = c1.union(c2);

                    if (!levelCandidates.contains(candidate)){
                        levelCandidates.add(candidate);
                        System.out.println("Added candidate: " + candidate);
                    }

                    getAndCacheSupportForItemset(candidate);
                }
            }
        }
        return new LinkedList<ItemSet<V>>(levelCandidates);
    }
}
