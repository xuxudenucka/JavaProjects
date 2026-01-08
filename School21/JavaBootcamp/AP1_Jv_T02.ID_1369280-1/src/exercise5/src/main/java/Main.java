import java.util.*;
import java.util.stream.Stream;

public class Main {
    private static long startTime;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        startTime = System.currentTimeMillis();

        int n = readNumber(scanner);

        List<Animal> pets = readPets(scanner, n);

        printPetsWalkTimes(pets);

        scanner.close();
    }

    private static int readNumber(Scanner scanner) {
        return Stream.of(scanner)
                .map(Main::tryParseInt)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> readNumber(scanner));
    }

    private static Integer tryParseInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Could not parse a number. Please, try again");
            return null;
        }
    }

    private static List<Animal> readPets(Scanner scanner, int n) {
        return Stream.generate(() -> readSinglePet(scanner))
                .limit(n)
                .filter(Objects::nonNull)
                .toList();
    }

    private static Animal readSinglePet(Scanner scanner) {
        String type = scanner.nextLine().trim().toLowerCase();
        if (!type.equals("dog") && !type.equals("cat")) {
            System.out.println("Incorrect input. Unsupported pet type");
            return null;
        }

        String name = scanner.nextLine();
        Integer age = readAge(scanner);

        if (age == null) {
            return null;
        }

        return switch (type) {
            case "dog" -> new Dog(name, age);
            case "cat" -> new Cat(name, age);
            default -> null;
        };
    }

    private static Integer readAge(Scanner scanner) {
        try {
            int age = Integer.parseInt(scanner.nextLine());

            if (age <= 0) {
                System.out.println("Incorrect input. Age <= 0");
                return null;
            }

            return age;
        } catch (Exception e) {
            System.out.println("Could not parse a number. Please, try again");
            return readAge(scanner);
        }
    }

    private static void printPetsWalkTimes(List<Animal> pets) {
        pets.parallelStream().forEach(pet -> {
            try {
                double startWalk = currentTime();
                double endWalk = currentTime() + pet.goToWalk();

                System.out.printf("%s, start time = %.2f, end time = %.2f%n",
                        pet, startWalk, endWalk);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static double currentTime() {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }
}