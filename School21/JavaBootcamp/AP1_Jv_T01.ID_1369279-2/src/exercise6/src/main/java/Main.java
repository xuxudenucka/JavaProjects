import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        String filePath = inputScanner.nextLine();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Input error. File doesn't exist");
            return;
        }
        try (Scanner fileScanner = new Scanner(file)) {
            if (!fileScanner.hasNext()) {
                System.out.println("Input error. Size <= 0");
                return;
            }
            int n;
            try {
                n = fileScanner.nextInt();
                if (n <= 0) {
                    System.out.println("Input error. Size <= 0");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Input error. Size <= 0");
                return;
            }
            double[] numbers = new double[n];
            int count = 0;
            while (fileScanner.hasNext() && count < n) {
                try {
                    numbers[count] = fileScanner.nextDouble();
                    count++;
                } catch (Exception e) {
                    fileScanner.next();
                }
            }
            if (count < n) {
                System.out.println("Input error. Insufficient number of elements");
                return;
            }
            for (int i = 0; i < count; i++) {
                System.out.print(numbers[i] + " ");
            }
            System.out.println();
            double min = numbers[0];
            double max = numbers[0];
            for (int i = 1; i < count; i++) {
                if (numbers[i] < min) min = numbers[i];
                if (numbers[i] > max) max = numbers[i];
            }
            try (FileWriter writer = new FileWriter("result.txt")) {
                writer.write(min + " ");
                writer.write(max + " ");
                System.out.println("Saving min and max values in file");
            } catch (IOException e) {
                System.out.println("Error writing to result.txt");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input error. File doesn't exist");
        }
    }
}
