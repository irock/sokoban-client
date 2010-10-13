package Sokoban;

import java.io.*;

/**
 * A container class for offline puzzles.
 */
public class Puzzle {
    /**
     * The name of the sample file to fetch puzzles from.
     */
    private static String sampleFile = "samples.server";

    /**
     * Getter for a specific puzzle among the samples.
     *
     * @param number The number of the puzzle to get.
     * @return the puzzle with the given number, if exists.
     */
    public static String getPuzzleFromSamples(int number) {
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(sampleFile));
            String line;
            StringBuffer mapString = new StringBuffer();
            boolean inMap = false;

            while ((line = reader.readLine()) != null) {
                if (inMap) {
                    if (line.length() == 0 || line.charAt(0) == ';')
                        return mapString.toString();
                    mapString.append(line);
                    mapString.append('\n');
                } else if (line.length() > 0 && line.charAt(0) == ';' &&
                        Integer.parseInt(line.substring(2)) == number) {
                    reader.readLine();
                    inMap = true;
                }
            }

            if (inMap)
                return mapString.toString();
        } catch (IOException e) { }
        return null;
    }
}
