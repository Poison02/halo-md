package cdu.zch.day01;

/**
 * @author Poison02
 * 打印某一个 int 整数的所有二进制位
 */
public class PrintBinary {

    public static void print(int num) {
        for (int i = 31; i >= 0; i--) {
            System.out.print((num & (1 << i)) == 0 ? "0" : "1");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int num = 12345;
        print(num);

        // 取某一个数的相反数
        int a = 5;
        // 对其取反再加1
        int b = (~ a + 1);
        System.out.println(a + " " + b);
    }
}
