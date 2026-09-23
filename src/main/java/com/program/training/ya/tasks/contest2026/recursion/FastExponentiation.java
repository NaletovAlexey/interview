package com.program.training.ya.tasks.contest2026.recursion;

import java.io.*;

/**
 * Быстрое возведение в степень
 * <a href="https://new.contest.yandex.ru/contests/97107/problems?id=175943%2F2016_09_21%2FOyTG5tZF4H">...</a>
 *
 * @author naletov
 */
public class FastExponentiation
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        double a = Double.parseDouble(reader.readLine().trim());
        long n = Long.parseLong(reader.readLine().trim());

        double result = power(a, n);
        writer.write(formatResult(result));

        reader.close();
        writer.close();
    }

    private static double power(double a, long n)
    {
        if (n < 0)
            return 1.0 / power(a, -n);
        if (n == 0)
            return 1.0;
        if (n % 2 == 0)
        {
            double half = power(a, n / 2);
            return half * half;
        }
        return a * power(a, n - 1);
    }

    // method to avoid incorrect output (rounding)
    private static String formatResult(double result)
    {
        if (result == Math.rint(result) && !Double.isInfinite(result))
        {
            return String.valueOf((long) result);
        }
        return String.valueOf(result);
    }
}
