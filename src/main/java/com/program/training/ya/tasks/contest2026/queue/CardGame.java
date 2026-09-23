package com.program.training.ya.tasks.contest2026.queue;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Карточная игра
 * <a href="https://new.contest.yandex.ru/contests/97108/problems?id=175943%2F2016_11_22%2FCd32DDMkPb">...</a>
 *
 * Пример:
 * 1 3 5 7 9
 * 2 4 6 8 0
 * -> second 5
 *
 * @author naletov
 */
public class CardGame
{
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String BOTVA = "botva";

    private static final int TOTAL_CARDS = 10;
    private static final int MAX_MOVES = 1000000;

    // Card 0 beats 9 (or TOTAL_CARDS - 1)
    private static final int ZERO = 0;
    private static final int HIGHEST_CARD = TOTAL_CARDS - 1;

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        Deque<Integer> firstPlayer = readHand(reader.readLine());
        Deque<Integer> secondPlayer = readHand(reader.readLine());

        String result = playGame(firstPlayer, secondPlayer);

        writer.write(result);

        reader.close();
        writer.close();
    }

    private static Deque<Integer> readHand(String line)
    {
        Deque<Integer> hand = new ArrayDeque<>();
        String[] parts = line.trim().split("\\s+");
        for (String part : parts)
        {
            hand.addLast(Integer.parseInt(part));
        }
        return hand;
    }

    // Check if card1 beats card2
    private static boolean beats(int card1, int card2)
    {
        if (card1 == ZERO && card2 == HIGHEST_CARD)
        {
            return true;
        }
        if (card2 == ZERO && card1 == HIGHEST_CARD)
        {
            return false;
        }
        return card1 > card2;
    }

    private static String playGame(Deque<Integer> firstPlayer, Deque<Integer> secondPlayer)
    {
        int moves = 0;

        while (!firstPlayer.isEmpty() && !secondPlayer.isEmpty())
        {
            if (moves >= MAX_MOVES)
            {
                return BOTVA;
            }

            int firstCard = firstPlayer.pollFirst();
            int secondCard = secondPlayer.pollFirst();
            moves++;

            boolean firstWins = beats(firstCard, secondCard);

            if (firstWins)
            {
                firstPlayer.addLast(firstCard);
                firstPlayer.addLast(secondCard);
            }
            else
            {
                secondPlayer.addLast(firstCard);
                secondPlayer.addLast(secondCard);
            }
        }

        String winner = firstPlayer.isEmpty() ? SECOND : FIRST;
        return winner + " " + moves;
    }
}
