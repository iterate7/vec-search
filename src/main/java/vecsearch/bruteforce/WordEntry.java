package vecsearch.bruteforce;


public class WordEntry implements Comparable<WordEntry> {
    public String name;
    public float score;
    public boolean isconcrete = true;
     
        public WordEntry(String name, float score) {
        	   this.name = name;
        this.score = score;
    }
    public WordEntry(String name, float score,boolean isconcrete) {
        this.name = name;
        this.score = score;
        this.isconcrete = isconcrete;
    }

    @Override
    public String toString() {
        return this.name +" "+((int) (score*100))/100.00;
    }

    public int compareTo(WordEntry o) {
        if (this.score < o.score) {
            return 1;
        } else {
            return -1;
        }
    }

}