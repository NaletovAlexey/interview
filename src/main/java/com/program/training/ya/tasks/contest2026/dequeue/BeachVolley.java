package com.program.training.ya.tasks.contest2026.dequeue;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Пляжный волейбол
 * <a href="https://new.contest.yandex.ru/contests/97109/problems?id=30404%2F2021_10_27%2FvXyC6otXEb">...</a>
 *
 * Пример
 * Ввод:
 * 4
 * 1 3 2 4
 * 1
 * 3
 * Вывод:
 * 3 4
 *
 * @author naletov
 */
public class BeachVolley
{

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());
        String[] streng = reader.readLine().trim().split("\\s+");
        Deque<Integer> deque = new ArrayDeque<>();

        for (int i = 0; i < n; i++)
        {
            deque.addLast(Integer.parseInt(streng[i]));
        }

        // gameA[i], gameB[i] - a pair of teams that played in game number i (i = 1..m)
        int[] gameA = new int[n];
        int[] gameB = new int[n];

        int a = deque.pollFirst();
        int b = deque.pollFirst();

        int winner = Math.max(a, b);
        int loser = Math.min(a, b);

        gameA[1] = a;
        gameB[1] = b;
        deque.addLast(loser);

        int m = 1;

        // until the strongest team (strength n) becomes the winner
        while (winner != n)   //  1 to N — eam strengths
        {
            m++;
            int challenger = deque.pollFirst();

            gameA[m] = winner;
            gameB[m] = challenger;

            int currentWinner = Math.max(winner, challenger);
            int currentLoser = Math.min(winner, challenger);

            deque.addLast(currentLoser);
            winner = currentWinner;
        }

        // from this moment on the winner (n) no longer changes, games repeat with a period of n – 1.
        int period = n - 1;
        int[] periodicQueue = new int[period];

        for (int i = 0; i < period; i++)
        {
            periodicQueue[i] = deque.pollFirst();
        }

        int q = Integer.parseInt(reader.readLine().trim());

        for (int i = 0; i < q; i++)
        {
            long k = Long.parseLong(reader.readLine().trim());

            if (k <= m)
            {
                writer.write(gameA[(int) k] + " " + gameB[(int) k]);
            }
            else
            {
                long remainder = k - m - 1;
                int id = (int) (remainder % period);
                writer.write(n + " " + periodicQueue[id]);
            }
            writer.newLine();
        }
        reader.close();
        writer.close();
    }
}
