package com.program.training.ya.tasks;

import java.io.*;

/**
 * Счётчик может отображать значения от 1 до m.
 * При нажатии на кнопку значение счётчика увеличивается на 1,
 * при этом если на счётчике отображалось число m,
 * то после нажатия кнопки будет отображаться число 1.
 *
 * Изначально на счётчике отображалось число a.
 * Определите минимальное количество нажатий на кнопку, чтобы на счётчике отображалось число b.
 *
 * Формат ввода
 * В первой строке вводится целое число m — наибольшее значение,
 * которое может отображать счётчик (2 ≤ m ≤ 100).
 *
 * Во второй строке вводится целое число a,
 * которое изначально отображал счётчик (1 ≤ a ≤ m).
 *
 * В третьей строке вводится целое число b — число,
 * которое должно отображаться на счётчике (1 ≤ b ≤ m).
 *
 * Формат вывода
 * Выведите одно целое число — минимальное количество нажатий на кнопку,
 * которое требуется сделать, чтобы на счётчике стало отображаться число b.
 *
 * @author naletov
 */
public class Counter {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int m = Integer.parseInt(reader.readLine().trim());
        int a = Integer.parseInt(reader.readLine().trim());
        int b = Integer.parseInt(reader.readLine().trim());

        int result = ((b - a) % m + m) % m;

        writer.write(String.valueOf(result));

        reader.close();
        writer.close();
    }
}
