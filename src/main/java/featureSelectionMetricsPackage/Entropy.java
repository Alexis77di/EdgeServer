/*******************************************************************************
 ** Entropy.java
 ** Part of the Java Mutual Information toolbox
 **
 ** Author: Adam Pocock
 ** Created: 20/1/2012
 **
 **  Copyright 2012 Adam Pocock, The University Of Manchester
 **  www.cs.manchester.ac.uk
 **
 **  This file is part of MIToolboxJava.
 **
 **  MIToolboxJava is free software: you can redistribute it and/or modify
 **  it under the terms of the GNU Lesser General Public License as published by
 **  the Free Software Foundation, either version 3 of the License, or
 **  (at your option) any later version.
 **
 **  MIToolboxJava is distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU Lesser General Public License for more details.
 **
 **  You should have received a copy of the GNU Lesser General Public License
 **  along with MIToolboxJava.  If not, see <http://www.gnu.org/licenses/>.
 **
 *******************************************************************************/

package featureSelectionMetricsPackage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements common discrete Shannon Entropy functions.
 * Provides: univariate entropy H(X),
 * conditional entropy H(X|Y),
 * joint entropy H(X,Y).
 * Defaults to log_2, and so the entropy is calculated in bits.
 *
 * @author apocock
 */
public abstract class Entropy {
    public static double LOG_BASE = 2.0;

    private Entropy() {
    }

    /**
     * Calculates the univariate entropy H(X) from a vector.
     * Uses histograms to estimate the probability distributions, and thus the entropy.
     * The entropy is bounded 0 &#8804; H(X) &#8804; log |X|, where log |X| is the log of the number
     * of states in the random variable X.
     *
     * @param dataVector Input vector (X). It is discretised to the floor of each value before calculation.
     * @return The entropy H(X).
     */
    public static double calculateEntropy(double[] dataVector) {
        featureSelectionMetricsPackage.ProbabilityState state = new ProbabilityState(dataVector);

        double entropy = 0.0;
        for (Double prob : state.probMap.values()) {
            if (prob > 0) {
                entropy -= prob * Math.log(prob);
            }
        }

        entropy /= Math.log(LOG_BASE);

        return entropy;
    }//calculateEntropy(double [])

    public static List<Double> calculate(Reader in) {
        List<Double> row = new ArrayList<>(14);
        try {
            CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            List<List<String>> csv = new ArrayList<>();
            for (int i = 0; i < 14; i++) {
                csv.add(new ArrayList<>());
            }
            for (CSVRecord record : records) {
                Iterator<String> iterator = record.iterator();
                for (int i = 0; i < 14; i++) {
                    csv.get(i).add(iterator.next());
                }
            }
            for (List<String> column : csv) {
                row.add(Entropy.calculateEntropy(column.stream().map(Double::parseDouble).mapToDouble(Double::doubleValue).toArray()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }
}//class Entropy
