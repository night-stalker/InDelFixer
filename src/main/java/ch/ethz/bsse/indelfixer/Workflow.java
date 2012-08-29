/**
 * Copyright (c) 2011-2012 Armin Töpfer
 *
 * This file is part of InDelFixer.
 *
 * InDelFixer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * InDelFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * InDelFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.indelfixer;

import ch.ethz.bsse.indelfixer.parallel.AlignWorker;
import ch.ethz.bsse.indelfixer.stored.Globals;
import ch.ethz.bsse.indelfixer.utils.FastaParser;
import ch.ethz.bsse.indelfixer.parallel.IndexWorker;
import ch.ethz.bsse.indelfixer.parallel.ReadWorker;
import ch.ethz.bsse.indelfixer.stored.Genome;
import ch.ethz.bsse.indelfixer.stored.InformationHolder;
import ch.ethz.bsse.indelfixer.stored.Read;
import ch.ethz.bsse.indelfixer.utils.Plot;
import ch.ethz.bsse.indelfixer.utils.StatusUpdate;
import ch.ethz.bsse.indelfixer.utils.Utils;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Workflow {

    private List<Read> reads;
    private Genome genome;
    private InformationHolder ih;

    public Workflow(String genomePath, String[] readsString) {
        this.genome = parseGenome(genomePath);
        Globals.N = readsString.length;
        this.reads = Globals.fjPool.invoke(new ReadWorker(0, Globals.N, readsString));
        this.ih = new InformationHolder(this.reads, this.genome.getSequence().length());
        Globals.INFO_HOLDER = ih;
        StatusUpdate.println("Processing reads:\t\t100%");
        this.map();
        this.align();
    }

    private Genome parseGenomeRead(String genomePath) {
        Genome g;
        String out = "";
        for (int i = 20;; i++) {
            out = "Searching for smallest kmer:\t";
            try {
                Globals.KMER_LENGTH = i;
                for (int j = 0; j < i; j++) {
                    out += ("|");
                }
                g = new Genome(genomePath);
                break;
            } catch (IllegalStateException e) {
            }
            StatusUpdate.print(out);
            Globals.KMER_LENGTH = i;
        }
        StatusUpdate.println(out + " (" + Globals.KMER_LENGTH + ")");
        return g;
    }

    private Genome parseGenome(String genomePath) {
        String g = FastaParser.parseFarFile(genomePath)[0];
        return parseGenomeRead(g);
    }

    private void map() {
        StatusUpdate.print("Finding indizes for kmers");
        Globals.N = reads.size();
        Globals.fjPool.invoke(new IndexWorker(0, reads.size(), reads.toArray(new Read[reads.size()]), genome));
        StatusUpdate.println("Finding indizes for kmers:\t100%");
        Map<Integer, Read> forward = new HashMap<>();
        Map<Integer, Read> reverse = new HashMap<>();
        for (Read r : this.reads) {
            if (r.isReverse()) {
                reverse.put(r.getNumber(), r);
            } else {
                forward.put(r.getNumber(), r);
            }
        }
        this.reads.clear();
        int r = 0;
        for (int i : forward.keySet()) {
            StatusUpdate.print("Sorting:\t\t\t"+i);
            Read readF = forward.get(i);
            Read readR = reverse.get(i);
            if (readF.getMaximumHits() > readR.getMaximumHits()) {
                this.reads.add(readF);
            } else {
                r++;
                this.reads.add(readR);
            }
        }
        StatusUpdate.println("Reads on reverse strand:\t" + r);
    }

    public void saveCoverage() {
        StringBuilder sb2 = new StringBuilder();
        int[] coverage = new int[this.genome.getSequence().length()];
        for (Read r : this.reads) {
            if (r.isAligned()) {
                int end = r.getEnd() >= coverage.length ? coverage.length : r.getEnd();
                for (int i = r.getBegin(); i < end; i++) {
                    coverage[i] += 1;
                }
            }
        }
        sb2.append("x").append("\t").append("y").append("\n");
        for (int i = 0; i < coverage.length; i++) {
            sb2.append(i).append("\t").append(coverage[i]).append("\n");
        }
        Plot.plotCoverage(coverage);
        Utils.saveFile(Globals.output + "coverage.txt", sb2.toString());
    }

    public static void removeLogging() throws SecurityException {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers.length > 0) {
            rootLogger.removeHandler(handlers[0]);
        }
    }

    public static Matrix loadMatrix() {
        Matrix matrix = null;
        try {
            matrix = MatrixLoader.load("EDNAFULL");
        } catch (MatrixLoaderException ex) {
            Logger.getLogger(Workflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matrix;
    }

    public InformationHolder getIh() {
        return ih;
    }

    public Genome getGenome() {
        return genome;
    }

    public List<Read> getReads() {
        return reads;
    }

    private void align() {
        Globals.UNIDENTICAL = this.reads.size();
        Workflow.removeLogging();
        Globals.fjPool.invoke(new AlignWorker(0, this.reads.size(), this.reads.toArray(new Read[this.reads.size()]),
                this.genome.getSequence(), loadMatrix()));
        StatusUpdate.println("Aligning reads:\t\t100%");
    }
}