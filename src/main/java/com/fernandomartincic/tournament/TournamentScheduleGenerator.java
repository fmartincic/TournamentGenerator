package com.fernandomartincic.tournament;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.output.StringBuilderWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the tournament schedule.
 */
public class TournamentScheduleGenerator {

    private int numRounds;
    private Map<Integer, List<List<Integer>>> schedule = Maps.newHashMap();

    public static void main(final String[] args) {
        TournamentScheduleGenerator gen = new TournamentScheduleGenerator();
        int m = 16;
        gen.generate(m, m, m / 2);
    }

    private int numMale = 0;
    private int numFemale = 0;
    private int numPlayers = 0;


    /**
     * Generates the schedule.
     *
     * @param numMale   number of men in the tournament
     * @param numFemale number of women in the tournament
     * @param numRounds number of rounds to be played
     */
    public void generate(final int numMale, final int numFemale, final int numRounds) {
        this.numMale = numMale;
        this.numFemale = numFemale;
        this.numPlayers = numMale + numFemale;
        this.numRounds = numRounds;

        int offset = 2;
        for (int round = 0; round < numRounds; round++) {
            schedule.put(round, Lists.newArrayList());
            final List<List<Integer>> malePairs = getPairs(numMale, round, 0);
            System.out.println(malePairs);
            final List<List<Integer>> femalePairs = getPairs(numFemale, offset, numMale);
            offset += 2;
            System.out.println(femalePairs);

            List<List<Integer>> pools = combinePairs(malePairs, femalePairs);
            System.out.println(pools);

            System.out.println();
            schedule.put(round, pools);
        }

        schedule.entrySet()
                .stream()
                .forEach(e -> System.out.println(e.getKey() + " (" + e.getValue().size() + "): " + e.getValue()));

        System.out.println();


        printSchedule();
    }

    private List<List<Integer>> combinePairs(final List<List<Integer>> malePairs, final List<List<Integer>> femalePairs) {
        final List<List<Integer>> results = new ArrayList<>();

        for (int t = 0; t < malePairs.size(); t++) {
            final List<Integer> pool = new ArrayList<>();

            pool.add(malePairs.get(t).get(0));
            pool.add(femalePairs.get(t).get(0));
            pool.add(malePairs.get(t).get(1));
            pool.add(femalePairs.get(t).get(1));

            results.add(pool);

        }


        return results;
    }

    private List<List<Integer>> getPairs(int size, int offset, int additional) {
        int front = offset;
        int back = offset - 1;

        if (front >= size) {
            front = front - size;
        }

        if (back < 0) {
            back = size - 1;
        }

        final List<List<Integer>> pairs = new ArrayList<>();

        for (int t = 0; t < size / 2; t++) {
            final List<Integer> pair = new ArrayList<>();

            pair.add(front + additional);
            pair.add(back + additional);

            back--;
            if (back < 0) {
                back = size - 1;
            }

            front++;
            if (front >= size) {
                front = 0;
            }

            pairs.add(pair);
        }

        return pairs;
    }

    private void printSchedule() {
        final StringBuilder body = new StringBuilder();

        final Map<Integer, List<String>> totalSumString = new HashMap<>();
        for (int t = 0; t < numPlayers; t++) {
            totalSumString.put(t, new ArrayList<>());
        }

        int row = numMale + 3;
        for (int currentRound = 0; currentRound < numRounds; currentRound++) {
            final List<List<Integer>> pools = schedule.get(currentRound);
            body.append("\nRound: " + (currentRound + 1) + ",Score,,Score\n");
            row += 2;

            for (final List<Integer> pool : pools) {
                row++;
                // output
                body.append(
                        "\"=concatenate("
                                + getPlayerCell(pool.get(0))
                                + ",E1,"
                                + getPlayerCell(pool.get(1))
                                + ")\","
                                + ","
                                + "\"=concatenate("
                                + getPlayerCell(pool.get(2))
                                + ",E1,"
                                + getPlayerCell(pool.get(3))
                                + ")\""
                                + "\n"
                );

                // update total sum entries
                final String rr = String.valueOf(row - 1);
                totalSumString.get(pool.get(0)).add("B" + rr);
                totalSumString.get(pool.get(1)).add("B" + rr);
                totalSumString.get(pool.get(2)).add("D" + rr);
                totalSumString.get(pool.get(3)).add("D" + rr);
            }

        }

        final StringBuilder header = new StringBuilder();
        header.append("Male,Total Score,Female,Total Score,\" - \"\n");
        for (int t = 0; t < numMale; t++) {
            header.append("M" + t + "," + getPlayerTotalScore(totalSumString.get(t))
                    + ",F" + (t + numMale) + "," + getPlayerTotalScore(totalSumString.get(t + numMale))
                    + "\n");
        }

        System.out.println(header);
        System.out.println(body.toString());

//        System.out.println("\n\n\n");
//        for (int t = 0; t < numPlayers; t++) {
//            System.out.println(totalSumString.get(t));
//        }


    }

    private String getPlayerTotalScore(final List<String> totals) {
        final StringBuilderWriter sb = new StringBuilderWriter();
        sb.append("=");
        for (int t = 0; t < totals.size() - 1; t++) {
            sb.append(totals.get(t) + "+");
        }

        sb.append(totals.get(totals.size() - 1));

        return sb.toString();
    }

    public String getPlayerCell(final int playerNum) {
        if (playerNum < numMale) {
            return "A" + (playerNum + 2);
        } else {
            return "C" + (playerNum - numMale + 2);
        }
    }


}
