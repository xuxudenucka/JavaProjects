import java.util.Scanner;

public class Main {
    private static int input() {
        Scanner scanner = new Scanner(System.in);
        int seconds = 0;
        while (true) {
            try {
                seconds = scanner.nextInt();
                break;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
                scanner.nextLine();
            }
        }
        return seconds;
    }

    private static int[] convertToHMS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return new int[]{hours, minutes, seconds};
    }

    private static void output(int[] HMS) {
        System.out.printf("%02d:%02d:%02d\n", HMS[0], HMS[1], HMS[2]);
    }

    public static void main(String[] args) {
        int seconds = input();
        if (seconds < 0) {
            System.out.println("Incorrect time");
            return;
        }
        int[] HMS = convertToHMS(seconds);
        output(HMS);
    }
}

