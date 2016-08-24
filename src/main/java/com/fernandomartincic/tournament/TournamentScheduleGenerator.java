package com.fernandomartincic.tournament;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.Validate;

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

            for (int team = 0; team < numPlayers / 4; team++) {
                System.out.println("\nRound " + (round + 1) + ":");
                //System.out.println();
                // get next available players
                final List<Integer> pool = Lists.newArrayList();

                // get next available male player
                int i = availableMale.stream().min(Integer::min).get().intValue();

                pool.add(i);
                System.out.println("male 1 = " + i);
                availableMale.remove(i);
                System.out.println(availableMale + " / " + availableFemale);

                // get partner
                i = availableFemale.stream()
                        //.peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            //System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("female 1 = " + i);
                pool.add(i);
                availableFemale.remove(i);
                //System.out.println(availableMale + " / " + availableFemale);

                // get male opponent
                i = availableMale.stream()
                        //.peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            //System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("male 2 = " + i);
                pool.add(i);
                availableMale.remove(i);
                //System.out.println(availableMale + " / " + availableFemale);

                // get female opponent
                i = availableFemale.stream()
                        //.peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            //System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("female 2 = " + i);
                pool.add(i);
                availableFemale.remove(i);
                //System.out.println(availableMale + " / " + availableFemale);
                updateMatched(pool, 1);
                updateMatched(pool.subList(0, 2), numPlayers * 2);
                updateMatched(pool.subList(2, 4), numPlayers * 2);

                //System.out.println(pool);
                printMatched();

                schedule.get(round).add(pool);
            }
        }


        //printSchedule();
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

    private int getMatchedScore(final List<Integer> pool, final int p2) {
        //System.out.println("pool=" + pool + ", p2=" + p2);
        final List<Integer> copy = Lists.newArrayList(pool);
        copy.add(p2);
        final List<Integer> copy2 = Lists.newArrayList(copy);
        Validate.isTrue(copy.containsAll(pool));
        int total = copy.stream()
                .map(Integer::intValue)
                //.peek(i -> System.out.println("i=" + i))
                .mapToInt(i -> copy2.stream()
                        .map(Integer::intValue)
                        .filter(j -> i < j)
                        //.peek(j -> System.out.println("i=" + i + ", j=" + j))
                        .mapToInt(j -> matched[i][j])
                        .sum()
                )
                .sum();

        //System.out.println("total=" + total);
        return total;
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
