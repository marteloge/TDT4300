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

        for (int i = 0; i < singles.size(); i++){
            /*
            * pruning
            * */
            List<ItemSet<V>> pruned = pruneInfrequentCandidates(minSupport,singles);

            //Legge til i sett
            frequentItemSets.put(i,pruned);

            //System.out.println("yiha");

            //Generere kombinasjoner
            singles = aprioriGen(singles);
        }
    }
}
