package com.program.training.ya.tasks.contest2026.heap;

import java.io.*;

/**
 * Хипуй (Реализация кучи - дерево)
 * <a href="https://new.contest.yandex.ru/contests/97110/problems?id=30404%2F2018_03_15%2F9rmjNYTmLo">...</a>
 * <p>
 * Пример
 * Ввод:
 * 2
 * 0 10000
 * 1
 * Вывод:
 * 10000
 *
 * @author naletov
 */
public class Tree
{
    private static final String INSERT = "0";
    private static final String EXTRACT = "1";

    private final int[] tree;
    private int size;

    public Tree(int capacity)
    {
        tree = new int[capacity];
        size = 0;
    }

    public void insert(int value)
    {
        tree[size] = value;
        goToRoot(size);
        size++;
    }

    public int extract()
    {
        int max = tree[0];
        size--;
        tree[0] = tree[size];
        goToLeaf(0);
        return max;
    }

    private void goToRoot(int index)
    {
        while (index > 0)
        {
            int parent = (index - 1) / 2;

            if (tree[index] <= tree[parent])
            {
                break;
            }

            swapValues(index, parent);
            index = parent;
        }
    }

    private void goToLeaf(int index)
    {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int largest = index;

            if (leftChild < size && tree[leftChild] > tree[largest])
            {
                largest = leftChild;
            }

            if (rightChild < size && tree[rightChild] > tree[largest])
            {
                largest = rightChild;
            }

            if (largest == index)
            {
                break;
            }

            swapValues(index, largest);
            index = largest;
        }
    }

    private void swapValues(int i, int j)
    {
        int tmp = tree[i];
        tree[i] = tree[j];
        tree[j] = tmp;
    }

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());
        Tree heap = new Tree(n);

        for (int i = 0; i < n; i++)
        {
            String[] parts = reader.readLine().trim().split("\\s+");
            String type = parts[0];

            if (type.equals(INSERT))
            {
                int k = Integer.parseInt(parts[1]);
                heap.insert(k);
            }
            else if (type.equals(EXTRACT))
            {
                writer.write(String.valueOf(heap.extract()));
                writer.newLine();
            }
        }

        reader.close();
        writer.close();
    }
}

