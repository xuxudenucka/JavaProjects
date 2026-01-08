public class Cat extends Animal implements Omnivore{
    Cat(String name, int age) {
        super(name, age);
    }

    @Override
    public String hunt() {
        return "I can hunt for mice";
    }

    @Override
    public String toString() {
        return "Cat name = " + getName() + ", age = " + getAge() + ". " + hunt();
    }
}
