package com.program.training.ya.tasks.contest2026.recursion;

import java.io.*;

/**
 * Ханойские башни
 * <a href="https://new.contest.yandex.ru/contests/97107/problems?id=175943%2F2016_09_21%2FJ7HdYdllwN">...</a>
 *
 * Выведите последовательность перекладываний в формате a b c, где
 * a — номер перекладываемого диска,
 * b — номер стержня с которого снимается данный диск,
 * c — номер стержня на который надевается данный диск.
 *
 * @author naletov
 */
public class HanoiTowers
{
    private static BufferedWriter writer;

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());
        move(n, 1, 3);

        reader.close();
        writer.close();
    }

    // Moves a pyramid of n disks from stick a to stick b,
    // using stick c as an auxiliary (c is calculated as the third stick)
    private static void move(int n, int a, int b) throws IOException
    {
        if (n == 0)
        {
            return;
        }

        int c = 6 - a - b; // The stick numbers 1, 2, and 3 sum to 6, so the third one is 6 - a - b.

        move(n - 1, a, c);
        writer.write(n + " " + a + " " + b);
        writer.newLine();
        move(n - 1, c, b);
    }
}
