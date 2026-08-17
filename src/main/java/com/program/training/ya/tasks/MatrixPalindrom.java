package com.program.training.ya.tasks;

import java.io.*;

/**
 * Дана матрица n×m, в каждой клетке которой записано натуральное число.
 * Необходимо заменить минимальное количество чисел так,
 * чтобы каждая строка и столбец этой матрицы стали палиндромами
 * (т.е. читались одинаково от начала к концу и от конца к началу).
 *
 * Формат ввода
 * В первой строке вводятся числа n и m — количество строк и столбцов в матрице (1 ≤ n, m ≤ 1000).
 *
 * В следующих n строках находится по m чисел — элементы матрицы, целые положительные числа, не превышающие 10^6.
 *
 * Формат вывода
 * Выведите одно число — минимальное количество элементов матрицы, которые нужно изменить, чтобы все строки и столбцы стали палиндромами.
 *
 * @author naletov
 */
public class MatrixPalindrom {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        java.util.StringTokenizer st = new java.util.StringTokenizer(reader.readLine());
        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());

        int[][] a = new int[n][m];
        for (int i = 0; i < n; i++) {
            st = new java.util.StringTokenizer(reader.readLine());
            for (int j = 0; j < m; j++) {
                a[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        long answer = 0;
        int[] group = new int[4];

        for (int i = 0; 2 * i <= n - 1; i++) {
            int i2 = n - 1 - i;
            boolean rowSame = (i == i2);
            for (int j = 0; 2 * j <= m - 1; j++) {
                int j2 = m - 1 - j;
                boolean colSame = (j == j2);

                int size;
                if (!rowSame && !colSame) {
                    group[0] = a[i][j];
                    group[1] = a[i][j2];
                    group[2] = a[i2][j];
                    group[3] = a[i2][j2];
                    size = 4;
                } else if (rowSame && !colSame) {
                    group[0] = a[i][j];
                    group[1] = a[i][j2];
                    size = 2;
                } else if (!rowSame) {
                    group[0] = a[i][j];
                    group[1] = a[i2][j];
                    size = 2;
                } else {
                    group[0] = a[i][j];
                    size = 1;
                }

                int maxFreq = 0;
                for (int x = 0; x < size; x++) {
                    int cnt = 0;
                    for (int y = 0; y < size; y++) {
                        if (group[y] == group[x]) cnt++;
                    }
                    if (cnt > maxFreq) maxFreq = cnt;
                }

                answer += (size - maxFreq);
            }
        }

        writer.write(String.valueOf(answer));

        reader.close();
        writer.close();
    }
}
