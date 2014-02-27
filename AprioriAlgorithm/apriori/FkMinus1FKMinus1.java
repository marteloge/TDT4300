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

                if(freq1.size()==1){
                    ItemSet<V> candidate = freq1.union(freq2);
                    if(candidate.size() == freq1.size() + 1){
                        levelCandidates.add(candidate);
                        getAndCacheSupportForItemset(candidate);
                    }
                }
                else if(freq1.first().equals(freq2.first())){
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
}

