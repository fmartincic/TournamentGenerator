package com.fernandomartincic.tournament;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the tournament schedule.
 */
public class TournamentScheduleGenerator {

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

        resetMatched();

        // generate for each round
        final Map<Integer, List<List<Integer>>> schedule = Maps.newHashMap();
        printMatched();

        for (int round = 0; round < numRounds; round++) {
            resetAvailable();
            schedule.put(round, Lists.newArrayList());

            for (int team = 0; team < numPlayers / 4; team++) {
                System.out.println();
                // get next available players
                final List<Integer> pool = Lists.newArrayList();

                // get next available male player
                int i = availableMale.stream().findFirst().get().intValue();
                pool.add(i);
                System.out.println("male 1 = " + i);
                availableMale.remove(i);
                System.out.println(availableMale + " / " + availableFemale);


                // get partner
                i = availableFemale.stream()
                        .peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("female 1 = " + i);
                System.out.println();
                pool.add(i);
                availableFemale.remove(i);
                System.out.println(availableMale + " / " + availableFemale);

                // get male opponent
                i = availableMale.stream()
                        .peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("male 2 = " + i);
                System.out.println();
                pool.add(i);
                availableMale.remove(i);
                System.out.println(availableMale + " / " + availableFemale);

                // get female opponent
                i = availableFemale.stream()
                        .peek(j -> System.out.println("Examining j=" + j))
                        .peek(j -> Validate.isTrue(!pool.contains(j)))
                        .min((p1, p2) -> {
                            System.out.println("p1=" + p1 + ", p2=" + p2);
                            return Integer.compare(getMatchedScore(pool, p1.intValue()), getMatchedScore(pool, p2.intValue()));
                        })
                        .get();
                System.out.println("female 2 = " + i);
                System.out.println();
                pool.add(i);
                availableFemale.remove(i);
                System.out.println(availableMale + " / " + availableFemale);
                updateMatched(pool, 1);
                updateMatched(pool.subList(0, 2), numPlayers);
                updateMatched(pool.subList(2, 4), numPlayers);


                System.out.println(pool);
                printMatched();

                schedule.get(round).add(pool);
            }
        }


        System.out.println("\n\nSchedule:\n");
        for (int t = 0; t < numRounds; t++) {
            System.out.println("Round " + (t + 1) + ": " + schedule.get(t));
        }
    }

    private void printMatched() {
        final StringBuilder sb = new StringBuilder();
        for (int t = 0; t < numPlayers; t++) {
            for (int s = 0; s < numPlayers; s++) {
                sb.append(String.format("%2d ", matched[t][s]));
            }
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    private void updateMatched(final List<Integer> pool, final int incrementAmount) {
        System.out.println("pool=" + pool);
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
        System.out.println("pool=" + pool + ", p2=" + p2);
        final List<Integer> copy = Lists.newArrayList(pool);
        copy.add(p2);
        final List<Integer> copy2 = Lists.newArrayList(copy);
        Validate.isTrue(copy.containsAll(pool));
        int total = copy.stream()
                .map(Integer::intValue)
                .peek(i -> System.out.println("i=" + i))
                .mapToInt(i -> copy2.stream()
                        .map(Integer::intValue)
                        .filter(j -> i < j)
                        .peek(j -> System.out.println("i=" + i + ", j=" + j))
                        .mapToInt(j -> matched[i][j])
                        .sum()
                )
                .sum();

        System.out.println("total=" + total);
        return total;
    }

}
