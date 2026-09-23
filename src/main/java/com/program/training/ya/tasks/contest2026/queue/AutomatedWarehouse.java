package com.program.training.ya.tasks.contest2026.queue;

import java.io.*;
import java.util.*;

/**
 * Автоматизированный склад
 * <a href="https://new.contest.yandex.ru/contests/97108/problems?id=30404%2F2024_11_02%2FG0KugiBpvC">...</a>
 *
 * Пример:
 * Ввод
 * 4
 * 1 3
 * 1 1
 * 3 1
 * 2 1
 * 2 2
 *
 * Вывод:
 * 1
 * 1
 * 2
 * 3
 *
 * @author naletov
 */
public class AutomatedWarehouse
{
    private static final int SIDES_COUNT = 4;

    // Rover description
    private record Rover(int index, int direction, int time) {}

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());

        String[] mainRoad = reader.readLine().trim().split("\\s+");
        int a = Integer.parseInt(mainRoad[0]);
        int b = Integer.parseInt(mainRoad[1]);

        // Rovers by sides (N/E/S/W - 1..4)
        List<Rover>[] bySide = new List[SIDES_COUNT + 1];   // just skip 0 index
        for (int side = 1; side <= SIDES_COUNT; side++)
        {
            bySide[side] = new ArrayList<>();
        }

        for (int i = 1; i <= n; i++)
        {
            String[] parts = reader.readLine().trim().split("\\s+");
            int direction = Integer.parseInt(parts[0]);
            int time = Integer.parseInt(parts[1]);
            bySide[direction].add(new Rover(i, direction, time));
        }

        boolean[] isMain = new boolean[SIDES_COUNT + 1];
        isMain[a] = true;
        isMain[b] = true;

        // dependsOn[s] — sides whose crossing times side s must avoid (that is, s yields to each of them)
        List<Integer>[] dependsOn = new List[SIDES_COUNT + 1];
        for (int s = 1; s <= SIDES_COUNT; s++)
        {
            dependsOn[s] = new ArrayList<>();
            for (int t = 1; t <= SIDES_COUNT; t++)
            {
                if (s == t) continue;
                if (mustYield(s, t, isMain))
                {
                    dependsOn[s].add(t);
                }
            }
        }

        int[] answer = new int[n + 1];
        Set<Integer>[] passTimes = new Set[SIDES_COUNT + 1];
        boolean[] processed = new boolean[SIDES_COUNT + 1];

        int processedCount = 0;
        while (processedCount < SIDES_COUNT)
        {
            for (int s = 1; s <= SIDES_COUNT; s++)
            {
                if (processed[s]) continue;

                boolean ready = true;
                for (int dep : dependsOn[s])
                {
                    if (!processed[dep])
                    {
                        ready = false;
                        break;
                    }
                }
                if (!ready) continue;

                Set<Integer> avoid = new HashSet<>();
                for (int dep : dependsOn[s])
                {
                    avoid.addAll(passTimes[dep]);
                }

                passTimes[s] = processSide(bySide[s], answer, avoid);
                processed[s] = true;
                processedCount++;
            }
        }

        for (int i = 1; i <= n; i++)
        {
            writer.write(String.valueOf(answer[i]));
            writer.newLine();
        }

        reader.close();
        writer.close();
    }

    // The side to the right of s (yield to the right)
    private static int rightOf(int s)
    {
        return s == 1 ? 4 : s - 1;
    }

    // Check if side s1 yield to side s2:
    // - a major road never yields to a minor road;
    // - a minor road always yields to a major road;
    // - between roads of the same class, the "yield to the right" rule applies.
    private static boolean mustYield(int s1, int s2, boolean[] isMain)
    {
        if (isMain[s1] && !isMain[s2]) return false;
        if (!isMain[s1] && isMain[s2]) return true;
        return rightOf(s1) == s2;
    }

    // A side is processed according to its arrival-time queue, avoiding time slots occupied by sides to which it yields priority.
    private static Set<Integer> processSide(List<Rover> rovers, int[] answer, Set<Integer> avoid)
    {
        List<Rover> sorted = new ArrayList<>(rovers);
        sorted.sort(Comparator.comparingInt(Rover::time));

        Set<Integer> myTimes = new HashSet<>();
        int prevPassTime = 0;
        for (Rover rover : sorted)
        {
            int candidate = Math.max(rover.time(), prevPassTime + 1);
            while (avoid.contains(candidate))
            {
                candidate++;
            }
            answer[rover.index()] = candidate;
            myTimes.add(candidate);
            prevPassTime = candidate;
        }
        return myTimes;
    }
}
