import java.util.ArrayList;
import java.util.HashMap;

public class HTMLhighlighter {

    private ArrayList<String> rawText;
    private ArrayList<String> breadthStack;
    private ArrayList<String> depthStack;
    private HashMap<String, String> seqHash;

    private final int maxTagSize = 7;
    private boolean hasAppliedSeq;
    private String rawLine;
    private String prevRawLine;
    private String tagName;  //to be set in case there's more than one tag on a rawLine
    private String defaultEscSeq;

    public HTMLhighlighter(ArrayList<String> rawText) {

        this.rawText = rawText;
        breadthStack = new ArrayList<>();
        depthStack = new ArrayList<>();
        seqHash = new HashMap<>();
        fillSeqHash();
        defaultEscSeq = "\\color[WHITE]";
    }

    public void fillSeqHash() {
        seqHash.put("<P>", "\\color[DARKGRAY]");
        seqHash.put("<HTML>", "\\color[RED]");
        seqHash.put("<HEAD>", "\\color[YELLOW]");
        seqHash.put("<TITLE>", "\\color[GREEN]");
        seqHash.put("<BODY>", "\\color[TURQUOISE]");
        seqHash.put("<H1>", "\\color[DARKGREEN]");
        seqHash.put("<H2>", "\\color[DARKRED]");
        seqHash.put("<H3>", "\\color[DARKYELLOW");
        seqHash.put("<MAIN>", "\\color[TAN]");
        seqHash.put("<A>", "\\color[BLUE]");
        seqHash.put("<BR>", "\\color[PINK]");
        seqHash.put("<DIV>", "\\color[ORANGE]");
        seqHash.put("<IMG>", "\\color[BROWN]");
        seqHash.put("<UL>", "\\color[GRAY]");
        seqHash.put("<LI>","\\[PURPLE]");
    }

    public void formatHTML() {

        //handles the case when a esc seq is applied before a closing tag on a single line
        hasAppliedSeq = false;

        for(int i = 0; i < rawText.size(); i++) {

            //remove any unnecessary leading or trailing whitespace
            rawLine = rawText.get(i).trim();
            prevRawLine  = (i == 0)? null : rawText.get(i - 1).trim();

            //there are no tags on the line OR there is only a single HTML tag in rawLine
            //<p> is the only element u can fit on one line without going over max size
            if(rawLine.charAt(0) != '<' || (rawLine.length() <= maxTagSize && !rawLine.toUpperCase().contains("<P>"))) {
                handleSimpleLine();
            }
            else { //there's one or mor pairs of HTML tags on that line
                handleComplexLine();
            }
            rawText.set(i, rawLine); //modify the original raw text
        }
    }

    private void handleSimpleLine() {

        //if rawLine is an opening tag
        if (!rawLine.contains("/") && rawLine.charAt(0) == '<') {
            String escSeq = retrieveEscSeq(rawLine);
            rawLine = escSeq + rawLine;
            if(!rawLine.toUpperCase().contains("BR")) { //since BR is void tag, we do not need a pair of esc seq
                depthStack.add(escSeq);
            }
        }
        //if rawLine is an open sentence that follows a closing tag
        else if(rawLine.charAt(0) != '<'  && prevRawLine.contains("/")) {
            rawLine = depthStack.get(depthStack.size() - 1) + rawLine;
            depthStack.remove(depthStack.size() - 1);
            hasAppliedSeq = true;

        }
        //if closing tag; saying "else" includes open sentences with prevLine not containing a closing tag
        else if(rawLine.charAt(0) == '<'){
            //already applied esc seq to open sentence
            if(hasAppliedSeq == true) {
                hasAppliedSeq = false;
            }
            else {
                rawLine = depthStack.get(depthStack.size() - 1) + rawLine;
                depthStack.remove(depthStack.size() - 1);
            }
        }
    }

    private void handleComplexLine() {

        breadthStack.clear();
        for(int k = 0; k < rawLine.length(); k++) {

            if(rawLine.charAt(k) == '<') {
                //extract the tag
                tagName = rawLine.substring(k, rawLine.indexOf(">", k) + 1);
                //if opening tag
                if(!tagName.contains("/") || tagName.toUpperCase().contains("HREF")) {
                    String escSeq = null;
                    if(tagName.toUpperCase().contains("HREF")) { //special case when tag is an <a> tag
                        escSeq = seqHash.get("<A>");
                    }
                    else {
                        escSeq = retrieveEscSeq(tagName);
                    }
                    breadthStack.add(escSeq); //push into stack
                    //insert the escape sequence into rawLine
                    rawLine = rawLine.substring(0, k) + escSeq + rawLine.substring(k);
                }
                //must be closing tag
                else {
                    breadthStack.remove(breadthStack.size() - 1); //pop off top element
                    int index = rawLine.indexOf(">", k) + 1;
                    //index of the last closing tag is found or no closing tag is found
                    if(index > rawLine.length() - 1 || index == -1) {
                        break;
                    }
                    //if another closing tag after closing tag or a sentence
                    if(rawLine.substring(index, index + 2).contains("</") || rawLine.charAt(index) != '<') {
                        rawLine = rawLine.substring(0, index) + breadthStack.get(breadthStack.size() - 1) + rawLine.substring(index);
                        k += rawLine.indexOf(">", k) - k; //readjust cursor
                    }
                }
                k += rawLine.indexOf(">", k) - k; //readjust cursor to combat shift induced by inserting esc seq
            }
        }
    }

    private String retrieveEscSeq(String tagName) {
        return (seqHash.get(tagName.toUpperCase()) == null)? defaultEscSeq : seqHash.get(tagName.toUpperCase());
    }
    public void printHTML() {
        for(String s : rawText) {
            System.out.println(s);
        }
    }
    public ArrayList<String> getFormattedText() {
        return rawText;
    }
}