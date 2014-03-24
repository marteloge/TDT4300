/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
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
package no.ntnu.idi.dm.clustering;

import java.util.ArrayList;

import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject;
import no.ntnu.idi.dm.distancemetrics.EuclideanDistance;

/**
 * A Cluster used in KMeans clustering. Has a centroid and a number of indices
 * of a data set assigned to it.
 * 
 * @see KMeans
 * @author Robert Neumayer
 * @version $Id: Cluster.java 3583 2010-05-21 10:07:41Z mayer $
 */

public class Cluster {

	private ArrayList<Integer> indices;

	private double[] centroid;

	private EuclideanDistance distanceFunction;

	public Cluster() {
		indices = new ArrayList<Integer>();
		// defaulting to Euclidean distance.
		distanceFunction = new EuclideanDistance();
	}

	public Cluster(double[] centroid) {
		this();
		this.centroid = centroid;
	}

	public Cluster(double[] centroid, EuclideanDistance distanceFunction) {
		this(centroid);
		this.distanceFunction = distanceFunction;
	}

	public ArrayList<Integer> getIndices() {
		return indices;
	}

	/**
	 * Calculate the centroid of this cluster. This is done by summing up all
	 * individual values divided by the number of instances assigned to it.
	 * 
	 * @param data
	 *            the data set.
	 */
	public void calculateCentroid(double[][] data) {
		for (int instanceIndex = 0; instanceIndex < indices.size(); instanceIndex++) {
			for (int attributeIndex = 0; attributeIndex < data[indices
					.get(instanceIndex)].length; attributeIndex++) {
				if (instanceIndex == 0) {
					centroid[attributeIndex] = 0;
				}
				centroid[attributeIndex] += data[indices.get(instanceIndex)][attributeIndex]
						/ indices.size();
			}
		}
	}

	/** Removes the instance according to the given index. */
	public void removeInstanceIndex(int instanceIndex) {
		indices.remove(new Integer(instanceIndex));
	}

	/**
	 * Add the index of a data point to this cluster.
	 * 
	 * @param index
	 *            to add.
	 */
	public void addIndex(int index) {
		indices.add(new Integer(index));
	}

	/**
	 * Set the centroid of this cluster.
	 * 
	 * @param centroid
	 *            to set.
	 */
	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	public double[] getCentroid() {
		return centroid.clone();
	}

	public int getNumberOfInstances() {
		return indices.size();
	}

	/**
	 * Returns all the instances belonging to this cluster according to the
	 * given data set.
	 * 
	 * @param data
	 *            instances.
	 * @return plain matrix of all assigned instances.
	 */
	public double[][] getInstances(double[][] data) {
		double[][] instances = new double[indices.size()][data[0].length];
		for (int i = 0; i < indices.size(); i++) {
			instances[i] = data[indices.get(i)];
		}
		return instances;
	}

	/**
	 * Calculate the sum of the squared error (SSE) for this cluster. This is
	 * the distances of the cluster's centroid to all units assigned.
	 * 
	 * @param data
	 *            matrix to compute the SSE for.
	 * @return the SSE value for this cluster.
	 */
	public double SSE(double[][] data) {
		double sse = 0;
		for(int i=0; i<2; i++){
			sse += Math.pow(EuclideanDistance.distance(this.centroid, data[this.indices.get(i)]), 2);
		}
		return sse;
	}

	/**
	 * calculate ssb
	 * 
	 * @param m
	 * @param data
	 * @return
	 */
	public double SSB(double[] m) {
		return this.getIndices().size() * Math.pow(getDistanceToCentroid(m), 2 );
	}

	/**
	 * SSE again, this time the average one (i.e. divided by the number of
	 * instances within this cluster)
	 */
	public double averageSSE(double[][] data) {
		return SSE(data) / this.getNumberOfInstances();
	}

	/**
	 * Get the distance of a given instance to this cluster's centroid.
	 * 
	 * @param instance
	 *            some instance.
	 * @return the distance according to the used distance function.
	 */
	public double getDistanceToCentroid(double[] instance) {
		return distanceFunction.distance(centroid, instance);
	}

	/**
	 * Get the instance with the maximum SSE of all instances assigned to this
	 * cluster.
	 */
	public int getInstanceIndexWithMaxSSE(double[][] data) {
		int index = -1;
		double maxSSE = Double.NEGATIVE_INFINITY;
		double currentSSE = 0;

		for (int i = 0; i < indices.size(); i++) {
			currentSSE = distanceFunction.distance(data[indices.get(i)],
					centroid);

			if (currentSSE > maxSSE) {
				maxSSE = currentSSE;
				index = indices.get(i);
			}

		}
		return index;
	}

	public double getAverageDistanceOfPointToAllPoints(double[][] data,
			int index) {
		double sumDistance = 0d;
		// this one we compare to all others
		double[] vector = data[index];
		for (int i = 0; i < indices.size(); i++) {
			double distance = distanceFunction.distance(data[indices.get(i)],
					vector);
			sumDistance += distance;
		}
		// return the average
		return sumDistance / (indices.size() - 1);
	}

	public double getAverageDistanceOfPoint(double[][] data, int index) {
		double sumDistance = 0d;
		// this one we compare to all others
		double[] vector = data[index];
		for (int i = 0; i < indices.size(); i++) {
			if (i != index) {
				double distance = distanceFunction.distance(
						data[indices.get(i)], vector);
				sumDistance += distance;
			}
		}
		// return the average
		return sumDistance / (indices.size() - 1);
	}
}