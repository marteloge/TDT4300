package apriori;

import java.util.*;

public class FkMinus1FKMinus1<V> extends BaseApriori<V> {

	public FkMinus1FKMinus1(List<ItemSet<V>> transactions) {
		super(transactions);
	}

	@Override
	public List<ItemSet<V>> aprioriGen(List<ItemSet<V>> frequentCandidatesKMinus1) {

        Collections.sort(frequentCandidatesKMinus1);
        Set<ItemSet<V>> levelCandidates = new HashSet<ItemSet<V>>();

        for(ItemSet<V> freq1 : frequentCandidatesKMinus1){
            for (ItemSet<V> freq2 : frequentCandidatesKMinus1){

                //System.out.println("FREQ1: " + freq1);
                //System.out.println("FREQ2: " + freq2);
                //System.out.println("FRIST: " + "< " + freq1.first() + ", " + freq2.first() + " >");

                if(freq1.size()==1){
                    ItemSet<V> candidate = freq1.union(freq2);
                    if(candidate.size() == freq1.size() + 1){
                        levelCandidates.add(candidate);
                        getAndCacheSupportForItemset(candidate);
                    }
                }
                else if(freq1.first().equals(freq2.first())){
                  //  System.out.println("True");
                    ItemSet<V> candidate = freq1.union(freq2);
                    if(candidate.size() == freq1.size() + 1){
                        levelCandidates.add(candidate);
                        getAndCacheSupportForItemset(candidate);
                    }
                }
            }
        }

        return new LinkedList<ItemSet<V>>(levelCandidates);
    }

    public List<ItemSet<V>> generateLevel2Set (List<ItemSet<V>> transactions) {

        List<ItemSet<V>> level2Set = new ArrayList<ItemSet<V>>();

        for(ItemSet<V> set1: transactions){
            for(ItemSet<V> set2:transactions){
                ItemSet<V> candidate = set1.union(set2);

                if(candidate.size() == set1.size()+1 && !level2Set.contains(candidate)){
                    level2Set.add(set1.union(set2));
                }
            }
        }

        return level2Set;
    }
}

