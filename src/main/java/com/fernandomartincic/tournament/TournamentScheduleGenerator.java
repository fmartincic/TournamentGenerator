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
        gen.generate(16, 16, 8);
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

        for (int round = 0; round < numRounds; round++) {
            schedule.put(round, Lists.newArrayList());

            final List<List<Integer>> malePairs = getPairs(0, numMale, round);
            System.out.println(malePairs);
            final List<List<Integer>> femalePairs = getPairs(numMale, numPlayers, round);
            System.out.println(femalePairs);
            System.out.println();


        }


        //printSchedule();
    }

    private List<List<Integer>> getPairs(final int startNumber, final int endNumber, final int offset) {
        int front = startNumber + offset;
        int back = endNumber - 1 + offset;

        if (back > endNumber) {
            back = front - 1;
        }

        final List<List<Integer>> pairs = new ArrayList<>();

        for (int t = 0; t < 8; t++) {
            final List<Integer> pair = new ArrayList<>();
            pair.add(front);
            pair.add(back);
            pairs.add(pair);

            front++;
            if (front >= endNumber) {
                front = startNumber;
            }

            back--;
            if (back < startNumber) {
                back = endNumber - 1;
            }
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
            body.append("\nRound: " + (currentRound + 1) + ",Set 1, Set 2,Set 3,,Set 1,Set 2,Set 3\n");
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
                                + ",,,"
                                + "\"=concatenate("
                                + getPlayerCell(pool.get(2))
                                + ",E1,"
                                + getPlayerCell(pool.get(3))
                                + ")\","
                                + ",,,"
                                + "\n"
                );

                // update total sum entries
                final String rr = String.valueOf(row - 1);
                totalSumString.get(pool.get(0)).add("B" + rr);
                totalSumString.get(pool.get(0)).add("C" + rr);
                totalSumString.get(pool.get(0)).add("D" + rr);

                totalSumString.get(pool.get(1)).add("B" + rr);
                totalSumString.get(pool.get(1)).add("C" + rr);
                totalSumString.get(pool.get(1)).add("D" + rr);


                totalSumString.get(pool.get(2)).add("F" + rr);
                totalSumString.get(pool.get(2)).add("G" + rr);
                totalSumString.get(pool.get(2)).add("H" + rr);

                totalSumString.get(pool.get(3)).add("F" + rr);
                totalSumString.get(pool.get(3)).add("G" + rr);
                totalSumString.get(pool.get(3)).add("H" + rr);
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
