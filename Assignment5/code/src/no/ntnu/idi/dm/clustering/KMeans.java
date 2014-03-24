package no.ntnu.idi.dm.clustering;

/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *                     
 *                     and myself
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import no.ntnu.idi.dm.distancemetrics.EuclideanDistance;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;


/**
 * Pretty much the classic K-Means clustering. Tried to keep it simple, though.
 * 
 * @author Robert Neumayer
 * @version $Id: KMeans.java 3921 2010-11-05 12:54:53Z mayer $
 */

public class KMeans {

	protected double[][] data;

	private int k;  //the number of desired clusters
	private int numberOfInstances; //the number of data points 
	private int numberOfAttributes; //the number of atttributes each data point has. The number of dimensions each data point has.

	// keep min, max values and the differences between them
	private double[] minValues, maxValues, differences;

	// this one will be used for looking up cluster assignments
	// which in turn will help to terminate the training process
	// as soon as we don't experience any changes in cluster assignments
	private Hashtable<Integer, Integer> instancesInClusters;

	protected Cluster[] clusters;

	// TODO remove this after testing
	private static long RANDOM_SEED = 1234567;
	
	// Keep track of udates. When the number of new updates is 0, the clustering is complete. 

	/**
	 * Default constructor (as much defaulting as possible). Uses linear
	 * initialisation and Euclidean distance.
	 * 
	 * @param k
	 *            number of clusters
	 * @param data
	 *            guess
	 */
	public KMeans(int k, double[][] data) {
		this(k, data, new EuclideanDistance());
	}

	/**
	 * Construct a new K-Means object.
	 * 
	 * @param k
	 *            number of clusters
	 * @param data
	 *            the data set
	 * @param initialisation
	 *            initialisation type
	 * @param distanceFunction
	 *            an LnMetric of your choice
	 */
	public KMeans(int k, double[][] data, EuclideanDistance distanceFunction) {
		this.k = k;
		this.numberOfAttributes = data[0].length;
		this.numberOfInstances = data.length;
		this.instancesInClusters = new Hashtable<Integer, Integer>();
		this.clusters = new Cluster[k];
		this.data = data;

		// initialise a couple of things
		initMinAndMaxValues();

		// init with random instances
		initClustersRandomlyOnInstances(distanceFunction);

		// printClusters();
		// this one is to do a first assignment of data points to clusters
		recalculateClusters();
	}

	/* getters and setters */

	public double[] getMinValues() {
		return minValues;
	}

	public double[] getMaxValues() {
		return maxValues;
	}

	public double[] getDifferences() {
		return differences;
	}

	public Cluster[] getClusters() {
		return clusters;
	}

	/**
	 * @return Returns the data.
	 */
	public double[][] getData() {
		return data;
	}

	/**
	 * Update for as long as instances move between clusters. "Not moving" means
	 * that there hasn't been a change in the last
	 * {@link #NUMBER_OF_UPDATE_RANGE} steps ({@value #NUMBER_OF_UPDATE_RANGE}).
	 */
	public void assignAndUpdate() {
		// train for as we have updates in the cluster assignments
		boolean update = true;
		while (update) {
			update = recalculateClusters();
		}
		removeEmptyClusters();
	}

	/**
	 * Searches for clusters which have no instances assigned. These are then
	 * removed
	 */
	private void removeEmptyClusters() {
		Vector<Cluster> nonEmptyClusters = new Vector<Cluster>();
		for (Cluster cluster : clusters) {
			if (cluster.getNumberOfInstances() != 0) {
				nonEmptyClusters.add(cluster);
			}
		}
		Cluster[] remainingClusters = new Cluster[nonEmptyClusters.size()];
		for (int i = 0; i < nonEmptyClusters.size(); i++) {
			remainingClusters[i] = nonEmptyClusters.elementAt(i);
		}
		clusters = remainingClusters;
	}

	private int lastNumberOfUpdates = Integer.MAX_VALUE;

	/**
	 * A classic training step in the K-Means world.
	 * 
	 * @return whether this step brought any changes or not. Note, this one also
	 *         says no if there were as many changes as in the last step.
	 */
	private boolean recalculateClusters() {
	
		boolean recalculate = false;
		int numUpdates = 0;

		for(int i = 0; i < this.data.length; i++){
			int index = getIndexOfClosestCluster(this.data[i]);
			
			if(!clusters[index].getIndices().contains(i)){
				int oldCluster = 0;
				for(int j=0; j<clusters.length;j++){
					if(clusters[j].getIndices().contains(i)){
						oldCluster = j;
					}
				}
				// Remove from old cluster and add to new cluster. 
				clusters[oldCluster].removeInstanceIndex(i);
				clusters[index].addIndex(i);
			
		        numUpdates++;
		        recalculate = true;
			}
		}
		
		calculateNewCentroids();
		
		//This step don't make sense, but the javadoc specified it. 
		if (numUpdates==this.lastNumberOfUpdates){
			recalculate = false;
		}

		return recalculate;
		
	}

	/**
	 * Batch calculation of all cluster centroids.
	 */
	private void calculateNewCentroids() {
		for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
			// go look for a new centroid if no instances are assigned to the
			// cluster
			if (clusters[clusterIndex].getNumberOfInstances() == 0) {
				if (getSubstituteCentroid() != null) {
					clusters[clusterIndex].setCentroid(getSubstituteCentroid());
				}
			}
			clusters[clusterIndex].calculateCentroid(data);
		}
	}

	/**
	 * Get a new centroid for empty clusters. We therefore take the instance
	 * with the largest SSE to the cluster centroid having the largest SSE. Get
	 * the idea? Read slowly.
	 * 
	 * @return a new centroid (rather: a clone thereof :))
	 */
	private double[] getSubstituteCentroid() {
		double maxSSE = Double.NEGATIVE_INFINITY;
		int maxSSEIndex = -1;
		for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
			clusters[clusterIndex].calculateCentroid(data);
			double currentSSE = clusters[clusterIndex].SSE(data);
			if (currentSSE > maxSSE) {
				maxSSE = currentSSE;
				maxSSEIndex = clusterIndex;
			}
		}

		if (clusters[maxSSEIndex].getInstanceIndexWithMaxSSE(data) == -1) {
			return null;
		}
		return data[clusters[maxSSEIndex].getInstanceIndexWithMaxSSE(data)]
				.clone();
	}

	/**
	 * Get the index of the closest cluster for the given instance index. Note
	 * that in case of equally distant clusters we assign the first found
	 * cluster. At the end of the day this means that the clusters with lower
	 * indices will have a tendency to be larger. It hopefully won't have too
	 * much impact, possibly a random assignment in case of equal weights would
	 * make sense, however, this would require a couple of steps more in here.
	 * 
	 * @param instance
	 *            the data vector to be assigned
	 * @return index of the closest cluster centre
	 */
	private int getIndexOfClosestCluster(double[] instance) {
		int indexOfClosestCluster = 0;
		double smallestDistance = Double.POSITIVE_INFINITY;
		double currentDistance = 0;
		for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
			currentDistance = clusters[clusterIndex]
					.getDistanceToCentroid(instance);
			if (currentDistance < smallestDistance) {
				smallestDistance = currentDistance;
				indexOfClosestCluster = clusterIndex;
			}
		}
		return indexOfClosestCluster;
	}

	/** Take random points from the input data as centroids. */
	private void initClustersRandomlyOnInstances(
			EuclideanDistance distanceFunction) {
		ArrayList<double[]> usedInstances = new ArrayList<double[]>();
		RandomGenerator rg = new JDKRandomGenerator();
		rg.setSeed(RANDOM_SEED);
		// for each cluster
		for (int clusterIndex = 0; clusterIndex < k; clusterIndex++) {
			// draw a random input
			double[] centroid = data[rg.nextInt(data.length - 1)].clone();
			while (usedInstances.contains(centroid)) {
				centroid = data[rg.nextInt(data.length - 1)].clone();
			}
			usedInstances.add(centroid);
			clusters[clusterIndex] = new Cluster(centroid, distanceFunction);
		}
	}

	/**
	 * Utility method to get the min, max, and diff values of the data set. This
	 * is used for scaling the (random) values in the initialisation functions.
	 */
	private void initMinAndMaxValues() {
		minValues = new double[numberOfAttributes];
		maxValues = new double[numberOfAttributes];
		differences = new double[numberOfAttributes];
		// for each attribute
		for (int j = 0; j < numberOfAttributes; j++) {
			// in each instance (i.e. each single value now :-))
			minValues[j] = Double.MAX_VALUE;
			maxValues[j] = Double.MIN_VALUE;
			for (double[] element : data) {
				if (element[j] < minValues[j]) {
					minValues[j] = element[j];
				}
				if (element[j] > maxValues[j]) {
					maxValues[j] = element[j];
				}
			}
			differences[j] = maxValues[j] - minValues[j];
		}
	}

	/**
	 * Get the sum of the squared error for all clusters.
	 * 
	 * @return SSE.
	 */
	public double getSSE() {
		double sse = 0;
		
		for(int i=0; i<k; i++){
			sse += clusters[i].SSE(data);
		}
		
		return sse; 
	}

	/**
	 * 
	 * @return SSB.
	 */
	public double getSSB() {
		double ssb=0;
        double[] m = getM();
        
        for (int i = 0; i < clusters.length; i ++) {
            ssb += clusters[i].SSB(m);
        }
        return ssb;
	}
	
	/**
	 * 
	 * @return Center for all centroids.
	 */
	public double[] getM (){
		
		//Coordinates for the m
        double[] m = new double[clusters[0].getCentroid().length];

        //Sum all coordinates for each cluster.
        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < clusters[i].getCentroid().length; j++) {
                m[j] += clusters[i].getCentroid()[j];
            }
        }
        
        //Get the mean
        for (int i = 0; i < clusters[0].getCentroid().length; i++) {
            m[i] = m[i] / clusters[0].getCentroid().length;
        }
        
        return m;
	}

	/**
	 * Get the sum of the squared error for single clusters.
	 * 
	 * @return several SSEs.
	 */
	public double[] getSSEs() {
		double[] sse = new double[k];
		for (int i = 0; i < clusters.length; i++) {
			sse[i] = clusters[i].SSE(this.data);
		}
		return sse;
	}

	public void printCentroids() {
		for (int i = 0; i < clusters.length; i++) {
			System.out.println(i + " / " + clusters[i].SSE(data) + " / "
					+ clusters[i].getNumberOfInstances() + " / "
					+ ArrayUtils.toString(clusters[i].getCentroid()));
		}
	}

	public void printCentroidsShort() {
		for (int i = 0; i < clusters.length; i++) {
			System.out.println("\t" + i + " / " + clusters[i].SSE(data) + " / "
					+ clusters[i].getNumberOfInstances());
		}
	}

	public double getAverageSilhouetteValue() {
		double silhouette = 0;
		
		for(int i=0; i<clusters.length;i++){
			for(int j=0; j<clusters[i].getNumberOfInstances();j++){
				
				//Distance from point i in cluster c to all other points in cluster c.
				double a =	clusters[i].getAverageDistanceOfPoint(data, clusters[i].getIndices().get(j));
				//Sets b to the max value, because we want the min distance 
				//(closest point to i from a other cluster)
				double b = Double.MAX_VALUE;
				
					for(int l = 0; l < getClusters().length; l++) {
	                    if(l != k) {
	                        double tmpB = clusters[l].getAverageDistanceOfPointToAllPoints(data,clusters[i].getIndices().get(j));
	                        if(tmpB < b) {
	                            b = tmpB;
	                        }
	                    }
	                }
	                silhouette += (b-a)/Math.max(a, b);
	            }
	        }
	        silhouette /= data.length;
	        
	        return silhouette;
	}
}
