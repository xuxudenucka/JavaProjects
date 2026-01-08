import java.util.Scanner;

public class Main {
    public static void sort(float[] array) {
        for (int i = 0; i < array.length; i++) {
            int pos = i;
            float min = array[i];
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < min) {
                    pos = j;
                    min = array[j];
                }
            }
            array[pos] = array[i];
            array[i] = min;
        }
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = -1;
        while (true) {
            try {
                n = scanner.nextInt();
                if (n <= 0) {
                    System.out.println("Input error. Size <= 0");
                }
                break;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
        float[] array = new float[n];
        while (true) {
            try {
                for (int i = 0; i < n; i++) {
                    array[i] = scanner.nextFloat();
                }
                break;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
        sort(array);
        for (float num : array) {
            System.out.print(num + " ");
        }
    }
}
