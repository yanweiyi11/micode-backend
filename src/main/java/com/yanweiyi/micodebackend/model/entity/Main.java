package com.yanweiyi.micodebackend.model.entity;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int positiveInt = scanner.nextInt();
        palindromePrime(positiveInt);
    }

    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        } else {
            for (int e = 2; e <= Math.sqrt(n); e++) {
                if (n % e == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void palindromePrime(int number) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < number; i++) {
            String s = String.valueOf(i);
            String reversedS = new StringBuilder(s).reverse().toString();
            if (!s.equals(reversedS)) {
                continue;
            }
            if (i > 10) {
                if (Integer.parseInt(s.substring(s.length() - 1)) % 2 == 0 || s.endsWith("5")) {
                    continue;
                }
            }
            if (isPrime(i)) {
                res.append(s).append(" ");
            }
        }
        System.out.println(res.toString());
    }
}