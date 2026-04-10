package com.program.training.ya.tasks;

import java.io.*;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/D/">...</a>
 *
 * Given an integer n, the task is to print all correct bracket sequences of length 2⋅n, ordered lexicographically.
 *
 * @author naletov
 */
public class GeneratingBracketSequences
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        byte n = (byte) Integer.parseInt(br.readLine());

        if  (n == 0)
            return;

        StringBuilder sb = new StringBuilder(2 * n);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        generateSequence(sb, 0, 0, n, out);

        //generateSequenceClassic(new char[2 * n], 0, 0, 0, n);
        out.flush();
        out.close();
    }

    private static void generateSequence(StringBuilder sb, int left, int right, byte count, BufferedWriter out) throws IOException
    {
        if (sb.length() == 2 * count)
        {
            out.write(sb.toString());
            out.newLine();
            return;
        }
        // '('
        if (left < count)
        {
            sb.append('(');
            generateSequence(sb, left + 1, right, count, out);
            sb.setLength(sb.length() - 1);
        }

        // ')'
        if (right < left)
        {
            sb.append(')');
            generateSequence(sb, left, right + 1, count, out);
            sb.setLength(sb.length() - 1);
        }
    }

    /**
     *
     * @param result array for storing the current sequence
     * @param pos current position
     * @param left open brackets
     * @param right close brackets
     * @param count brackets pair amount
     */
    private static void generateSequenceClassic(char[] result, int pos, int left, int right, byte count)
    {
        if (pos == 2 * count)
        {
            System.out.println(new String(result));
            return;
        }

        if (left < count)
        {
            result[pos] = '(';
            generateSequenceClassic(result, pos + 1, left + 1, right, count);
        }
        if (right < left)
        {
            result[pos] = ')';
            generateSequenceClassic(result, pos + 1, left, right + 1, count);
        }
    }
}
