public class People {
    String name;
    int age;
    boolean setName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isSetName() {
        return setName;
    }

    public void setSetName(boolean setName) {
        this.setName = setName;
    }

    @Override
    public String toString() {
        return "name=" + name + " age=" + age + " setName=" +setName;
    }
}