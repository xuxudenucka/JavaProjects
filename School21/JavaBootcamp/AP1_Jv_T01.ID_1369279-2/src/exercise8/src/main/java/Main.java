import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static List<String> filterBySubstring(List<String> list, String substring) {
        List<String> result = new ArrayList<>();
        for (String str : list) {
            if (str.contains(substring)) {
                result.add(str);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> strings = new ArrayList<>();

        int n;
        try {
            n = scanner.nextInt();
            if (n <= 0) {
                System.out.println("Input error. Size <= 0");
                return;
            }
            scanner.nextLine();
        } catch (NumberFormatException e) {
            System.out.println("Could not parse a number. Please, try again");
            return;
        }

        for (int i = 0; i < n; i++) {
            strings.add(scanner.nextLine());
        }

        String substring = scanner.nextLine();

        List<String> filtered = filterBySubstring(strings, substring);

        if (!filtered.isEmpty()) {
            for (int i = 0; i < filtered.size(); i++) {
                System.out.print(filtered.get(i));
                if (i < filtered.size() - 1) {
                    System.out.print(", ");
                }
            }
        }
    }
}

