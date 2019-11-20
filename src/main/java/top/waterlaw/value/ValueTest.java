package top.waterlaw.value;

public class ValueTest {

    public static void main(String[] args) {
        //- Java 中只有值传递
        Student s1 = new Student("小张");
        Student s2 = new Student("小李");
        ValueTest.swap(s1, s2); //- 传递引用的拷贝，对原引用不影响
        System.out.println("s1:" + s1.getName());
        System.out.println("s2:" + s2.getName());
        setStudentName(s1, "xxxxx");
        System.out.println(s1.getName());
    }

    public static void setStudentName(Student s, String name){
        s.setName(name);
    }

    public static void swap(Student x, Student y) {
        Student temp = x;
        x = y;
        y = temp;
        System.out.println("x:" + x.getName());
        System.out.println("y:" + y.getName());

        Integer a = 128;Integer b = 128;
        System.out.println(a == b);// false
        Integer aa = 127;Integer bb = 127; // true
        System.out.println(aa == bb);
        Integer aaa = -127;Integer bbb = -127;// true
        System.out.println(aaa == bbb);
        Integer aaaa = -128;Integer bbbb = -128; // true
        System.out.println(aaaa == bbbb);

        Integer v1 = Integer.valueOf(12);
        Integer v2 = Integer.valueOf(12);
        System.out.println(v1 == v2);
    }
}

class Student {
    private String name;

    public Student(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
