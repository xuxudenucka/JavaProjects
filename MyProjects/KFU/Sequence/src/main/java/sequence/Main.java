package sequence;

public class Main {
    public static void main(String[] args) {
        Sequence seq = SequenceGenerator.generate(5);
        seq.printSequence();

        seq.add(5);
        seq.printSequence();

        seq.removeByIndex(1);
        seq.printSequence();

        seq.removeByValue(5);
        seq.printSequence();

        seq.replace(0, 2);
        seq.printSequence();

        seq.insert(0, 1);
        seq.printSequence();
    }
}