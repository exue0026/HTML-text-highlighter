import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class HTMLhighlighter {

    public static void main(String[] args) {

        String fileName = "input.txt";
        BufferedReader br = null;
        ArrayList<String> rawText = null;
        final int maxTagSize = 7;

        ArrayList<String> breadthStack = new ArrayList<>();
        ArrayList<String> depthStack = new ArrayList<>();
        HashMap<String, String> seqHash = new HashMap<>();

        seqHash.put("<P>", "\\color[DARKGRAY]");
        seqHash.put("<HTML>", "\\color[RED]");
        seqHash.put("<HEAD>", "\\color[YELLOW]");
        seqHash.put("<TITLE>", "\\color[GREEN]");
        seqHash.put("<BODY>", "\\color[TURQUOISE]");
        seqHash.put("<H1>", "\\color[DARKGREEN]");
        seqHash.put("<A>", "\\color[BLUE]");
        seqHash.put("<BR>", "\\color[PINK]");
        seqHash.put("<DIV>", "\\color[ORANGE]");
        seqHash.put("<IMG>", "\\color[BROWN]");
        seqHash.put("<UL>", "\\color[GRAY]");
        seqHash.put("<LI>","\\[PURPLE]");

        //READ THE FILE AND RETRIEVE THE INPUT
        try {
            br = new BufferedReader(new FileReader(fileName));
            rawText = new ArrayList<String>();
            String line;
            while((line = br.readLine()) != null) {
                rawText.add(line);
            }
            br.close();
        }
        catch(IOException error) {
            System.out.println("Error: " + error.getMessage());
        }

        boolean applied  = false;

        for(int i = 0; i < rawText.size(); i++) { //loop through each line of HTML text

            String rawLine = rawText.get(i).trim();
            String prevRawLine  = (i == 0)? null : rawText.get(i - 1).trim();
            String tagName = null;

            //if only one HTML tag on that line:
            if(rawLine.charAt(0) != '<' || (rawLine.length() <= maxTagSize && (!rawLine.toUpperCase().contains("<P>")))) { //p is the only element u can fit on one line without going over max size
                if(rawLine.toUpperCase().contains("<P>")) {
                    rawLine = seqHash.get("<P>") + rawLine;

                }
                else if(!rawLine.contains("/") && rawLine.charAt(0) == '<') {
                    String esqpSeq = seqHash.get(rawLine.toUpperCase());
                    rawLine = esqpSeq + rawLine;
                    if(!rawLine.toUpperCase().contains("BR")) {
                        depthStack.add(esqpSeq);
                    }
                }
                else {
                    if(rawLine.charAt(0) != '<'  && prevRawLine.contains("/")) {
                       rawLine = depthStack.get(depthStack.size() - 1) + rawLine;
                       depthStack.remove(depthStack.size() - 1);
                       applied = true;

                    }
                    else if(rawLine.charAt(0) == '<'){
                        if(applied == true) {
                            applied = false;
                            continue;
                        }
                        rawLine = depthStack.get(depthStack.size() - 1) + rawLine;
                        depthStack.remove(depthStack.size() - 1);
                    }
                }
            }
            else { //there's one or mor pairs of HTML tags on that line

                breadthStack.clear();
                boolean isInTag = false;
              for(int k = 0; k < rawLine.length(); k++) {
                  char c = rawLine.charAt(k);
                  if(c != '<') {
                      continue;
                  }
                  else {
                      tagName = rawLine.substring(k, rawLine.indexOf(">", k) + 1);
                      if(!tagName.contains("/") || tagName.contains("HREF") || tagName.contains("href")) {
                          isInTag = true;
                          String esqpSeq = null;
                          if(tagName.contains("HREF") || tagName.contains("href")) { //special case when tag is an a tag
                              esqpSeq = seqHash.get("<A>");
                          }
                          else {
                              esqpSeq = seqHash.get(tagName.toUpperCase());
                          }
                          breadthStack.add(esqpSeq); //push into stack

                          //insert the escape sequence
                          rawLine = rawLine.substring(0, k) + esqpSeq + rawLine.substring(k);
                          k += rawLine.indexOf(">", k) - k; //readjust cursor
                      }
                      else {
                          //we found a closing tag
                          if(isInTag = true) {
                              isInTag = false;
                              breadthStack.remove(breadthStack.size() - 1); //pop off top element
                              int index = rawLine.indexOf(">", k) + 1;
                              if(index > rawLine.length() - 1 || index == -1) {
                                  break;
                              }
                              if(rawLine.substring(index, index + 2).contains("</") || rawLine.charAt(index) != '<') {
                                  rawLine = rawLine.substring(0, index) + breadthStack.get(breadthStack.size() - 1) + rawLine.substring(index);
                                  k += rawLine.indexOf(">", k) - k; //readjust cursor
                              }
                          }
                      }
                  }
              }
            }
            rawText.set(i, rawLine);
        }

        for(String s : rawText) {
            System.out.println(s);
        }
    }
}
