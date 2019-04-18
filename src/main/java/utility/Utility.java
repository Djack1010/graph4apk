package utility;

public class Utility {

    public static String clean4CCS(String s){
        return s.replaceAll("<", "").replaceAll(">", "").replaceAll("\\(", "-_-")
                .replaceAll("\\)", "-_-").replaceAll("\\[", "-__-").replaceAll("]", "-__-")
                .replaceAll(",", "_").replaceAll("\\.", "-").replaceAll(" ","");
    }

}
