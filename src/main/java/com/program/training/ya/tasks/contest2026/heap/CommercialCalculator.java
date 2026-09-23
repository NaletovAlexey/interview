package com.program.training.ya.tasks.contest2026.heap;

import java.io.*;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 *  Коммерческий калькулятор
 * <a href="https://new.contest.yandex.ru/contests/97110/problems?id=30404%2F2021_12_13%2F0RnfACzM6R>...</a>
 * <p>
 * Пример
 * Ввод:
 * 4
 * 10 11 12 13
 * Вывод:
 * 4.60
 *
 * @author naletov
 */
public class CommercialCalculator
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());

        String[] tokens = reader.readLine().trim().split("\\s+");
        PriorityQueue<Long> heap = new PriorityQueue<>();

        for (int i = 0; i < n; i++)
        {
            heap.add(Long.parseLong(tokens[i]));
        }

        long totalMerged = 0;

        while (heap.size() > 1)
        {
            long first = heap.poll();
            long second = heap.poll();

            long merged = first + second;
            totalMerged += merged;

            heap.add(merged);
        }

        double cost = totalMerged * 0.05;

        writer.write(String.format(Locale.US, "%.2f", cost));
        writer.newLine();

        reader.close();
        writer.close();
    }
}

