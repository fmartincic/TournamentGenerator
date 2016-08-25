package com.fernandomartincic.tournament;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.output.StringBuilderWriter;

import java.util.*;

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

    private Set<Integer> availableMale = Sets.newHashSet();
    private Set<Integer> availableFemale = Sets.newHashSet();

    private int[][] matched = null;

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

        resetMatched();

        // generate for each round
        printMatched();

        for (int round = 0; round < numRounds; round++) {
            resetAvailable();

            schedule.put(round, Lists.newArrayList());

            System.out.println("\nRound " + (round + 1) + ":");


            for (int team = 0; team < numPlayers / 4; team++) {

                final List<Integer> m = new ArrayList<>(availableMale);
                final List<Integer> f = new ArrayList<>(availableFemale);

                //System.out.println("males=" + availableMale);
                //System.out.println("females=" + availableFemale);

                final List<List<Integer>> pools = combinePairs(generateAllPairs(m), generateAllPairs(f));
                //System.out.println("pools=" + pools);

                final List<Integer> pool =
                        pools.stream()
                                .min((p1, p2) -> Integer.compare(calculatePoolScore(p1), calculatePoolScore(p2)))
                                .get();
                System.out.println("pool=" + pool);

                updateMatched(pool, 1);
                updateMatched(pool.subList(0, 2), numPlayers * 2);
                updateMatched(pool.subList(2, 4), numPlayers * 2);

                // remove available players
                availableMale.removeAll(pool);
                availableFemale.removeAll(pool);

                schedule.get(round).add(pool);

            }

            System.out.println();
            printMatched();

        }

//        printSchedule();
    }


    private void printMatched() {
        final StringBuilder sb = new StringBuilder();
        for (int t = 0; t < numPlayers; t++) {
            sb.append(String.format("%2d ", t));
        }
        sb.append("\n");
        for (int t = 0; t < numPlayers; t++) {
            sb.append("---");
        }
        sb.append("\n");

        for (int t = 0; t < numPlayers; t++) {
            for (int s = 0; s < numPlayers; s++) {
                sb.append(String.format("%2d ", matched[t][s]));
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }


    private void resetAvailable() {
        availableMale.clear();
        availableFemale.clear();

        int index = 0;
        for (int t = 0; t < numMale; t++) {
            availableMale.add(index++);
        }

        for (int t = 0; t < numFemale; t++) {
            availableFemale.add(index++);
        }
    }

    private void resetMatched() {
        // initialize matched matrix
        numPlayers = numMale + numFemale;
        matched = new int[numPlayers][numPlayers];
        for (int t = 0; t < numPlayers; t++) {
            for (int s = 0; s < numPlayers; s++) {
                matched[t][s] = 0;
            }
            // don't match yourself
            matched[t][t] = -1;
        }
    }


    private void updateMatched(final List<Integer> pool, final int incrementAmount) {
        //System.out.println("pool=" + pool);
        final List<Integer> copy = Lists.newArrayList(pool);
        copy.stream()
                .map(Integer::intValue)
                .forEach(i -> {
                    pool.stream()
                            .map(Integer::intValue)
                            .filter(j -> i < j)
                            .forEach(j -> {
                                matched[i][j] += incrementAmount;
                                matched[j][i] += incrementAmount;
                            });
                });
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

    final int calculatePoolScore(final List<Integer> pool) {
        final List<Integer> copy = new ArrayList<>(pool);

        final int total = pool.stream()
                .mapToInt(Integer::intValue)
                .map(i -> {
                    return copy.stream()
                            .mapToInt(Integer::intValue)
                            .filter(j -> j < i)
                            .map(j -> matched[i][j])
                            .sum();
                })
                .sum();

        //System.out.println(pool + "=" + total);
        return total;

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

    private List<List<Integer>> combinePairs(final List<List<Integer>> malePairs, final List<List<Integer>> femalePairs) {
        final List<List<Integer>> results = new ArrayList<>();

        malePairs.stream()
                .forEach(malePair -> {
                    femalePairs.stream()
                            .forEach(femalePair -> {
                                final List<Integer> pool = new ArrayList<>();

                                pool.add(malePair.get(0));
                                pool.add(femalePair.get(0));
                                pool.add(malePair.get(1));
                                pool.add(femalePair.get(1));

                                results.add(pool);
                            });
                });

        return results;
    }

    final List<List<Integer>> generateAllPairs(final List<Integer> pool) {
        final List<List<Integer>> results = new ArrayList<>();

        final List<Integer> poolCopy = new ArrayList<>(pool);
        pool.stream()
                .forEach(i -> {
                    poolCopy.stream()
                            .filter(j -> i < j)
                            .forEach(j -> {
                                List<Integer> pair = new ArrayList<>();
                                pair.add(i);
                                pair.add(j);
                                results.add(pair);
                            });
                });

        return results;
    }

}
