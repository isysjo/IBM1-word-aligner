/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 * @author Isys Johnson
 */
public class WordAlign {

    public static final String NULL = "NULL";
  public HashMap<Integer,HashMap<Integer,Double>> pfe = new HashMap<>(); 
    //public HashMap<String,HashMap<String,Double>> pfe = new HashMap<>();
    public HashMap<String,String> sentences = new HashMap<>();
   public HashMap<Integer,String> english = new HashMap<>();
    public HashMap<Integer,String> foreign = new HashMap<>();
  //  public HashSet<String> english = new HashSet<>();
    //public HashSet<String> foreign = new HashSet<>();
    double thres;
    double INIT = .01;
    int iterate;
    public static final int COUNT_THRES = 5;
    public WordAlign(String e_sentences, String f_sentences, String iterations, String threshold) throws IOException {
        
        iterate = Integer.valueOf(iterations);
        thres = Double.valueOf(threshold);
       // BufferedReader br_eng = new BufferedReader(new FileReader(parameters[0]));
       // BufferedReader br_for = new BufferedReader(new FileReader(parameters[1]));
        BufferedReader br_eng = new BufferedReader(new FileReader(e_sentences));
        BufferedReader br_for = new BufferedReader(new FileReader(f_sentences));
        HashMap<Integer,Integer> counts = new HashMap<>();
        //go through lines of sentences
        String str_eng;
        String str_for;
     
        while ((str_eng = br_eng.readLine()) != null) {
            str_eng = "NULL "+str_eng;
            str_for = br_for.readLine();
         
            sentences.put(str_eng, str_for);
            // get unique words in each sentence
            
            // process each word as an int
             int[] eng_wor = hashCodeArray(str_eng.split("\\s"),english);
             int[] for_wor = hashCodeArray(str_for.split("\\s"),foreign);
           // String[] eng_wor = str_eng.split("\\s");
           // String[] for_wor = str_for.split("\\s");
           // english.addAll(Arrays.asList(eng_wor));
           // foreign.addAll(Arrays.asList(for_wor));
            
            
          
         //   initialize
            for (int i = 0; i < eng_wor.length; i++) {
                for (int j = 0; j < for_wor.length; j++) {
                    if (!pfe.containsKey(eng_wor[i])) {
                        pfe.put(eng_wor[i], new HashMap<>());
                        counts.put(eng_wor[i], 1);
                    } else counts.put(eng_wor[i], counts.get(eng_wor[i])+1);

                  //  HashMap<String, Double> update = pfe.get(eng_wor[i]);
                    HashMap<Integer, Double> update = pfe.get(eng_wor[i]);
                    if (pfe.get(eng_wor[i]).containsKey(for_wor[j])) {
                        update.put(for_wor[j], /*update.get(for_wor[j]) + */INIT);
                        counts.put(for_wor[j], counts.get(for_wor[j])+1);
                    } else {
                        update.put(for_wor[j], INIT);
                        counts.put(for_wor[j],1);
                    }
                    pfe.put(eng_wor[i], update);
                }
            }

        }
        // remove words with low counts
        for(int eng : new HashSet<Integer>(english.keySet()))
            for(int frn : new HashSet<Integer>(foreign.keySet())) {
                if(counts.get(eng) < COUNT_THRES) {
                    english.remove(eng);
                    pfe.remove(eng);
                } else if(counts.get(frn) < COUNT_THRES) {
                    HashMap<Integer,Double> update = pfe.get(eng);
                    update.remove(frn);
                    pfe.put(eng, update);
                    foreign.remove(frn);
                }
                    
            }
        
        
        counts.clear();
        System.out.println("Constructed.");
    }
    /**
     * Run EM algorithm
     */
    public void train() {
        int cur_itr = 1;
       // HashMap<String,Double> agg_counts = new HashMap<>();
       HashMap<Integer,Double> agg_counts = new HashMap<>();
        while(cur_itr <= iterate) {
           long start = System.currentTimeMillis();
             HashMap<Integer,HashMap<Integer,Double>> count_ef = new HashMap<>();
             HashMap<Integer,Double> count_f = new HashMap<>();
           
            // HashMap<String,HashMap<String,Double>> count_ef = new HashMap<>();
             //HashMap<String,Double> count_f = new HashMap<>();
            for(String eng : sentences.keySet()) {
                //for each sentence pair (e,f)
                int[] e = hashCodeArray(eng.split("\\s"),english);
                int[] f = hashCodeArray(sentences.get(eng).split("\\s"),foreign);
               // String[] e = eng.split("\\s");
               // String[] f = sentences.get(eng).split("\\s");
                int j,k;
                
                //find denominator 
                for(j = 0; j < e.length;j++) {
                    double curr_count = 0d;
                        for(k = 0; k < f.length; k++) {
                            if(pfe.containsKey(e[j]))
                            if(pfe.get(e[j]).containsKey(f[k]))
                                curr_count+=pfe.get(e[j]).get(f[k]);
                        }
                       agg_counts.put(e[j], curr_count);
                    }
                
                // calculate posterior over all english positions
                // increment count
                for(j = 0; j < e.length;j++) {
                        for(k = 0; k < f.length; k++) {
                            if(!pfe.containsKey(e[j])) continue;
                            else if(!pfe.get(e[j]).containsKey(f[k])) continue;
                            double ncount = pfe.get(e[j]).get(f[k])/agg_counts.get(e[j]);
                            if(count_ef.containsKey(e[j]))
                                if(count_ef.get(e[j]).containsKey(f[k])) {
                                    count_ef.get(e[j]).put(f[k], count_ef.get(e[j]).get(f[k])+ncount);
                                } else count_ef.get(e[j]).put(f[k], ncount);
                            else {
                              //  HashMap<String,Double> init = new HashMap<>();
                              HashMap<Integer,Double> init = new HashMap<>();
                                init.put(f[k], ncount);
                                count_ef.put(e[j], init);
                                
                            }
                            if(count_f.containsKey(f[k]))
                                count_f.put(f[k], count_f.get(f[k])+ncount);
                            else count_f.put(f[k], ncount);             
                        }
                      
                    }
                
               
               
                
            }
            // M step
//            for(String f : foreign)
//                for(String e : english) {
//                    if(count_ef.containsKey(e))
//                        if(count_ef.get(e).containsKey(f))
//                            pfe.get(e).put(f, count_ef.get(e).get(f)/count_f.get(f));
//                }
            for(int f : foreign.keySet())
                for(int e : english.keySet()) {
                    if(count_ef.containsKey(e))
                        if(count_ef.get(e).containsKey(f))
                            pfe.get(e).put(f, count_ef.get(e).get(f)/count_f.get(f));
                }
         
           cur_itr++;
           
           
           System.out.println("Seconds elapsed: " + (System.currentTimeMillis() - start)/1000);
        }
        
        
        
    }
    /**
     * Print p(f|e) table
     */
    public void output() {
        
        TreeSet<Integer> sorted_eng = new TreeSet<>(pfe.keySet());
        for(int eng : sorted_eng) {
            TreeSet<Integer> sorted_for = new TreeSet<>(pfe.get(eng).keySet());
            for(int fr : sorted_for) {
            // int rand = (int)((Math.random())*99);
                if(pfe.get(eng).get(fr) >= thres/* && rand  < 30*/)
                    System.out.println(english.get(eng) + "\t" + foreign.get(fr) + "\t" + pfe.get(eng).get(fr));
                   // System.out.println(eng + "\t" + fr + "\t" + pfe.get(eng).get(fr));
            }
            
        }
    }
    
    /**
     * Transform array of words to array of integers while adding them to
     * correct dictionary
     * @param str Array of words from sentence
     * @param dict Dictionary that words belong in
     * @return 
     */
    public static int[] hashCodeArray(String[] str, HashMap<Integer,String> dict) {
        int[] arr = new int[str.length];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = str[i].hashCode();
            if(!dict.containsKey(arr[i]))
            dict.put(arr[i], str[i]);
        }
        return arr;
    }
    
    
    
    public static void main(String args[]) throws IOException {
        WordAlign test = new WordAlign(args[0],args[1],args[2],args[3]);
        
        test.train();
        test.output();
        System.out.println("Used Memory: " +(Runtime.getRuntime().maxMemory()- Runtime.getRuntime().freeMemory()));
    }
    
}
