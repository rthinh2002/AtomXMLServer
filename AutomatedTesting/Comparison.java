import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Comparison {
    private static boolean isEqual(Path firstFile, Path secondFile) {
        try {
            if (Files.size(firstFile) != Files.size(secondFile)) {
                return false;
            }

            byte[] first = Files.readAllBytes(firstFile);
            byte[] second = Files.readAllBytes(secondFile);
            return Arrays.equals(first, second);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.print("Checking " + args[0] + "...");
        File firstFile = new File("testingResult/" + args[0]);
        File secondFile = new File("expectedResult/" + args[1]);

        boolean equal = isEqual(firstFile.toPath(), secondFile.toPath());
        if (equal) {
            System.out.println("\tPASSED");
        } else {
            System.out.println("\tFAILED");
        }
    }
}
