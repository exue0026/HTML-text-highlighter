import java.io.*;
import java.util.ArrayList;

public class Controller {

    public static void main(String[] args) {

        ArrayList<String> rawText = new ArrayList<>();
        getInput(rawText);
        filterInput(rawText);
        HTMLhighlighter highlighter = new HTMLhighlighter(rawText);
        highlighter.formatHTML();
        sendOutput(highlighter.getFormattedText());
    }

    public static void getInput(ArrayList<String> rawText) {

        String fileName = "input.txt";
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = br.readLine()) != null) {
                rawText.add(line);
            }
            br.close();
        }
        catch(IOException error) {
            System.out.println("Error: ");
            error.printStackTrace();
        }
    }

    public static void filterInput(ArrayList<String> rawText) {
        for(int i = 0; i < rawText.size(); i++) {
            if(rawText.get(i).isEmpty()) {
                rawText.remove(i);
                i--;
            }
        }
    }
    public static void sendOutput(ArrayList<String> formattedText) {
        String output = "";
        for(int i = 0; i < formattedText.size(); i++) {
            output += formattedText.get(i);
            if(i != formattedText.size() - 1) {
                output += "\n";
            }
        }
        BufferedWriter bw = null;
        String fileName = "output.txt";
        try {
            bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(output);
            bw.close();
        }
        catch (IOException error) {
            System.out.println("Error writing to file: ");
            error.printStackTrace();
        }
    }

}
