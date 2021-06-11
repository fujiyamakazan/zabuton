package com.github.fujiyamakazan.zabuton.app.jinro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.wicket.util.lang.Generics;

public class CosmicHuman {

    public static void main(String[] args) {
        //new CosmicHuman().execute();

        new CosmicHuman().test();
    }

    private enum TEAM {
        W, V
    }

    private enum ROLE {
        NONE, WW, FT
    };

    private enum RESULT_INSPECT_WW {
        YES, NO
    }

    private static Map<ROLE, TEAM> roleTeam = Generics.newHashMap();
    static {
        roleTeam.put(ROLE.NONE, TEAM.V);
        roleTeam.put(ROLE.WW, TEAM.W);
        roleTeam.put(ROLE.FT, TEAM.V);
    }

    private Random rand = new Random(System.currentTimeMillis());

    private List<Player> players = Generics.newArrayList();
    private List<Player> table = Generics.newArrayList();
    private List<Info> infos = Generics.newArrayList();

    private class Player {

        final private String name;
        final private ROLE role;

        public Player(String name, ROLE role) {
            this.name = name;
            this.role = role;

        }

        public Player getVote(List<Player> targets) {
            while (true) {
                Player p = pickupOne(targets);
                if (p != this) {
                    System.out.println(name + " vote →" + p.name);
                    return p;
                }
            }

        }

        @Override
        public String toString() {
            return name;
        }

        /* ランダムにひとつの要素を返却する */
        private final <T> T pickupOne(List<T> col) {
            if (col == null || col.size() < 1) {
                return null;
            }
            return col.get(rand.nextInt(col.size()));
        }

        public void inspection() {
            Player p;
            while (true) {
                p= pickupOne(table);
                if (p != this) {
                    break;
                }
            }

            Info info;
            if (p.role.equals(ROLE.WW)) {
                info = new Info(this, p, RESULT_INSPECT_WW.YES);
            } else {
                info = new Info(this, p, RESULT_INSPECT_WW.NO);
            }
            System.out.println("★ new info ★ :" + info);
            infos.add(info);

        }
    }

    private class Info {
        private Player from;
        private Player to;
        private RESULT_INSPECT_WW result;
        public Info(Player from, Player to, RESULT_INSPECT_WW result) {
            this.from = from;
            this.to = to;
            this.result = result;
        }
        @Override
        public String toString() {
            return "Info [from=" + from + ", to=" + to + ", result=" + result + "]";
        }

    }

    public void execute() {

        players.add(new Player("me", ROLE.FT));
        players.add(new Player("setsu", ROLE.NONE));
        players.add(new Player("sq", ROLE.WW));
        players.add(new Player("racio", ROLE.NONE));
        players.add(new Player("jina", ROLE.NONE));

        /* 参加 */
        for (Player p : players) {
            table.add(p);
        }
        System.out.println("table:" + table);
        System.out.println("-----------------------------------------------");

        int tarm = 1;
        TEAM win = null;
        while (true) {
            System.out.println("【" + tarm + "】");

            {
                System.out.println("[vote of exile]");
                List<Player> voters = new ArrayList<Player>();
                List<Player> targets = new ArrayList<Player>();
                for (Player playerOnTable : table) {
                    voters.add(playerOnTable);
                    targets.add(playerOnTable);
                }
                Player target = vote(voters, targets);
                table.remove(target);
                System.out.println("exile>" + target);
                System.out.println("table>>" + table + "");
            }

            /* 判定 */
            win = judge(win);
            if (win != null) {
                break;
            }

            System.out.println("[action of night]");

            for (Player p: table) {
                if (p.role.equals(ROLE.FT)) {
                    p.inspection();
                }
            }


            {
                System.out.println("[vote of bite]");
                List<Player> voters = new ArrayList<Player>();
                List<Player> targets = new ArrayList<Player>(table);
                for (Player playerOnTable : table) {
                    if (playerOnTable.role.equals(ROLE.WW)) {
                        voters.add(playerOnTable);
                    } else {
                        targets.add(playerOnTable);
                    }
                }
                Player target = vote(voters, targets);
                table.remove(target);
                System.out.println("bite>" + target);
                System.out.println("table>>" + table + "");
            }
            tarm++;

            /* 判定 */
            win = judge(win);
            if (win != null) {
                break;
            }

        }

        System.out.println("-----------------------------------------------");
        System.out.println("Win is " + win);

    }

    public TEAM judge(TEAM win) {
        int countWW = 0;
        int countV = 0;
        for (Player p : table) {
            if (p.role.equals(ROLE.WW)) {
                countWW++;
            } else if (roleTeam.get(p.role).equals(TEAM.V)) {
                countV++;
            } else {
                throw new RuntimeException();
            }
        }
        if (countWW == 0) {
            win = TEAM.V;
        } else if (countWW >= countV) {
            win = TEAM.W;
        } else {
            // continue
        }
        return win;
    }

    public Player vote(List<Player> voters, List<Player> targets) {
        Player target = null;
        while (target == null) {
            Counter counter = new Counter();
            for (Player voter : voters) {
                Player vote = voter.getVote(targets);
                counter.up(vote);
            }
            List<Player> top = counter.getTop();
            if (top.size() == 1) {
                target = top.get(0);
            } else {
                System.out.println("[re-vote]");
            }
        }
        return target;
    }

    private class Counter {
        private Map<Player, Integer> map = Generics.newHashMap();

        public void up(Player p) {
            Integer value = map.get(p);
            if (value == null) {
                map.put(p, 1);
            } else {
                map.put(p, value + 1);
            }
        }

        public List<Player> getTop() {
            Integer max = 0;
            for (Integer value : map.values()) {
                max = Math.max(max, value);
            }
            List<Player> top = Generics.newArrayList();
            for (Map.Entry<Player, Integer> entry : map.entrySet()) {
                if (entry.getValue() == max) {
                    top.add(entry.getKey());
                }
            }
            return top;
        }


    }

    private void test() {
        Player me = new Player("me", ROLE.FT);
        players.add(me);
        players.add(new Player("setsu", ROLE.NONE));
        Player sq = new Player("sq", ROLE.WW);
        players.add(sq);
        Player racio = new Player("racio", ROLE.NONE);
        players.add(racio);
        Player jina = new Player("jina", ROLE.NONE);
        players.add(jina);

        /* 参加 */
        for (Player p : players) {
            table.add(p);
        }
        System.out.println("table:" + table);
        System.out.println("-----------------------------------------------");

        infos.add(new Info(me, sq, RESULT_INSPECT_WW.YES));
        infos.add(new Info(me, jina, RESULT_INSPECT_WW.NO));

        infos.add(new Info(sq, jina, RESULT_INSPECT_WW.NO));
        infos.add(new Info(sq, racio, RESULT_INSPECT_WW.NO));


        /* 評価 */
        Counter counter = new Counter();
        for (Info info: infos) {
            counter.up(info.from);
        }
        List<Player> top = counter.getTop();
        if (top.size() == 1) {
            System.out.println(top.get(0) + " は絶対FT");
        }


    }

}
