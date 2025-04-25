package com.github.fujiyamakazan.zabuton.app.cube;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class Cube implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cube.class);

    /**
     * 色を識別します。
     */
    private enum Color {
        Y, B, G, O, R, W
    }

    /**
     * 回転軸を識別します。
     */
    private enum Axis {
        /** R面からL面に向かう軸。時計回りの操作はRとL' */
        X,
        /** U面からD面に向かう軸。時計回りの操作はUとD' */
        Y,
        /** F面からB面に向かう軸。時計回りの操作はFとB' */
        Z,
    }

    /** X軸を時計回りに一周したときの面の配列です。 */
    private static final Face[] ORDER_X = { Face.U, Face.B, Face.D, Face.F };
    /** Y軸を時計回りに一周したときの面の配列です。 */
    private static final Face[] ORDER_Y = { Face.L, Face.B, Face.R, Face.F };
    /** Z軸を時計回りに一周したときの面の配列です。 */
    private static final Face[] ORDER_Z = { Face.R, Face.D, Face.L, Face.U };

    /**
     * 位置を識別します。
     */
    public enum Pos {
        BUL, BU, BUR, // 奥
        BL, B, BR, // 奥
        BDL, BD, BDR, // 奥
        UL, U, UR, //
        L, R, //
        DL, D, DR, //
        FUL, FU, FUR, // 前
        FL, F, FR, // 前
        FDL, FD, FDR, // 前
    }

    /**
     * 面と操作を識別します。
     */
    public enum Face {

        R(Axis.X, true, new Pos[] { Pos.FUR, Pos.BUR, Pos.BDR, Pos.FDR }, new Pos[] { Pos.FR, Pos.UR, Pos.BR, Pos.DR }), //
        L(Axis.X, false, new Pos[] { Pos.BUL, Pos.FUL, Pos.FDL, Pos.BDL },
            new Pos[] { Pos.UL, Pos.FL, Pos.DL, Pos.BL }), //

        U(Axis.Y, true, new Pos[] { Pos.FUL, Pos.BUL, Pos.BUR, Pos.FUR }, new Pos[] { Pos.FU, Pos.UL, Pos.BU, Pos.UR }), //
        D(Axis.Y, false, new Pos[] { Pos.FDL, Pos.FDR, Pos.BDR, Pos.BDL },
            new Pos[] { Pos.FD, Pos.DR, Pos.BD, Pos.DL }), //

        F(Axis.Z, true, new Pos[] { Pos.FUL, Pos.FUR, Pos.FDR, Pos.FDL }, new Pos[] { Pos.FU, Pos.FR, Pos.FD, Pos.FL }), //
        B(Axis.Z, false, new Pos[] { Pos.BUR, Pos.BUL, Pos.BDL, Pos.BDR },
            new Pos[] { Pos.BU, Pos.BL, Pos.BD, Pos.BR }), //
            ;

        /** 操作の回転軸です。 */
        private final Axis axis;
        /** 回転軸に対して正方向ならtrue, 逆方向ならfalseです。 */
        private final boolean clockwise;
        /** 操作で移動するコーナーの位置です。順序は回転軸の時計回りです。*/
        private final Pos[] corners;
        /** 操作で移動するエッジの位置です。順序は回転軸の時計回りです。*/
        private final Pos[] edges;

        private Face(final Axis axis, final boolean clockwise, final Pos[] corners, final Pos[] edges) {
            this.axis = axis;
            this.clockwise = clockwise;
            this.corners = corners;
            this.edges = edges;
        }

    }

    /**
     * 配色情報（色と方向）です。
     */
    private static class Fc {
        private Face face;
        private final Color color;

        public static Fc of(final Face face, final Color color) {
            return new Fc(face, color);
        }

        public Fc(final Face face, final Color color) {
            this.face = face;
            this.color = color;
        }

        public Fc(final Fc orz) {
            this.face = orz.face;
            this.color = orz.color;
        }

        @Override
        public String toString() {
            return face + "[" + color + "]";
        }

    }

    /**
     * ピースです。
     * 位置情報と配色情報を持ちます。
     */
    private class Piece implements Serializable {
        private static final long serialVersionUID = 1L;
        private Pos pos;
        protected final List<Fc> fcs;

        public Piece(final Fc... fcs) {
            this.fcs = Arrays.asList(fcs);
        }

        public Piece(final Piece orz) {
            this.fcs = Generics.newArrayList();
            for (final Fc fc : orz.fcs) {
                this.fcs.add(new Fc(fc));
            }
            this.pos = orz.pos;
        }

        public Piece pos(final Pos pos) {
            this.pos = pos;
            return this;
        }

        public void rollAll(final Axis axis, final boolean clockwise) {
            for (final Fc v : fcs) {
                final Face[] faces;
                switch (axis) {
                    case X:
                        faces = ORDER_X;
                        break;
                    case Y:
                        faces = ORDER_Y;
                        break;
                    case Z:
                        faces = ORDER_Z;
                        break;
                    default:
                        throw new RuntimeException();
                }
                v.face = roll(v.face, clockwise, faces);
            }
        }

        private Face roll(final Face face, final boolean clockwise, Face[] pattern) {
            if (!clockwise) {
                pattern = reverse(pattern).toArray(new Face[pattern.length]);
            }
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i].equals(face)) {
                    if (i == pattern.length - 1) {
                        return pattern[0];
                    } else {
                        return pattern[i + 1];
                    }
                }
            }
            return face;
        }
    }

    /** ピースです。 */
    private final List<Piece> piecies;

    /**
     * コンストラクタです。
     */
    public Cube() {
        piecies = Generics.newArrayList();
        /* センター */
        piecies.add(new Piece(Fc.of(Face.U, Color.Y)).pos(Pos.U));
        piecies.add(new Piece(Fc.of(Face.F, Color.B)).pos(Pos.F));
        piecies.add(new Piece(Fc.of(Face.B, Color.G)).pos(Pos.B));
        piecies.add(new Piece(Fc.of(Face.L, Color.O)).pos(Pos.L));
        piecies.add(new Piece(Fc.of(Face.R, Color.R)).pos(Pos.R));
        piecies.add(new Piece(Fc.of(Face.D, Color.W)).pos(Pos.D));
        /* エッジ(上層) */
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.B, Color.G)).pos(Pos.BU));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.L, Color.O)).pos(Pos.UL));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.R, Color.R)).pos(Pos.UR));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.F, Color.B)).pos(Pos.FU));
        /* エッジ(中層) */
        piecies.add(new Piece(Fc.of(Face.B, Color.G), Fc.of(Face.L, Color.O)).pos(Pos.BL));
        piecies.add(new Piece(Fc.of(Face.B, Color.G), Fc.of(Face.R, Color.R)).pos(Pos.BR));
        piecies.add(new Piece(Fc.of(Face.F, Color.B), Fc.of(Face.L, Color.O)).pos(Pos.FL));
        piecies.add(new Piece(Fc.of(Face.F, Color.B), Fc.of(Face.R, Color.R)).pos(Pos.FR));
        /* エッジ(下層) */
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.B, Color.G)).pos(Pos.BD));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.L, Color.O)).pos(Pos.DL));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.R, Color.R)).pos(Pos.DR));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.F, Color.B)).pos(Pos.FD));
        /* コーナー(上層) */
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.B, Color.G), Fc.of(Face.L, Color.O)).pos(Pos.BUL));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.B, Color.G), Fc.of(Face.R, Color.R)).pos(Pos.BUR));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.F, Color.B), Fc.of(Face.L, Color.O)).pos(Pos.FUL));
        piecies.add(new Piece(Fc.of(Face.U, Color.Y), Fc.of(Face.F, Color.B), Fc.of(Face.R, Color.R)).pos(Pos.FUR));
        /* コーナー(下層) */
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.B, Color.G), Fc.of(Face.L, Color.O)).pos(Pos.BDL));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.B, Color.G), Fc.of(Face.R, Color.R)).pos(Pos.BDR));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.F, Color.B), Fc.of(Face.L, Color.O)).pos(Pos.FDL));
        piecies.add(new Piece(Fc.of(Face.D, Color.W), Fc.of(Face.F, Color.B), Fc.of(Face.R, Color.R)).pos(Pos.FDR));
    }

    /**
     * コピーコンストラクタです。
     */
    public Cube(final Cube orz) {
        piecies = Generics.newArrayList();
        for (final Piece p : orz.piecies) {
            piecies.add(new Piece(p));
        }
    }

    /**
     * 完成状態かを判定します。
     */
    private boolean isGoal() {
        for (final Face face : Face.values()) {
            Color color = null;
            for (final Piece p : piecies) {
                for (final Fc fc : p.fcs) {
                    if (fc.face.equals(face)) {
                        if (color == null) {
                            color = fc.color;
                        } else {
                            if (color != fc.color) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 指定された位置、面の色を返します。
     */
    public String getColorString(final Pos pos, final Face face) {
        final Piece p = pick(pos);
        for (final Fc v : p.fcs) {
            if (v.face.equals(face)) {
                return v.color.name();
            }
        }
        throw new RuntimeException("pos=" + pos + ", face=" + face);
    }

    private Piece pick(final Pos pos) {
        for (final Piece p : piecies) {
            if (p.pos.equals(pos)) {
                return p;
            }
        }
        throw new RuntimeException("pos=" + pos);
    }

    private final List<String> history = Generics.newArrayList();

    /**
     * ある面を時計周りに回転させます。複数の操作をコマンドで指定します。
     */
    private void rotate(final CubeCommand... commands) {

        for (int i = 0; i < commands.length; i++) {

            final CubeCommand command = commands[i];

            history.add(command.toString());

            //Face face = Face.valueOf(String.valueOf(cmd));
            //rotate_(face, o);

            final int dist = command.getDist();
            final Face face = command.getFace();
            final boolean prime = dist <= -1;
            for (int j = 0; j < Math.abs(dist); j++) {
                rotate_(face, prime);
            }

        }
    }

    /**
     * ある面を時計周りに回転させます。移動量は1です。
     * @param face 面
     */
    private void rotate_(final Face face, final boolean prime) {

        List<Pos> posCorners;
        List<Pos> posEdges;
        if (!prime) {
            posCorners = Arrays.asList(face.corners);
            posEdges = Arrays.asList(face.edges);
        } else {
            posCorners = reverse(face.corners);
            posEdges = reverse(face.edges);
        }

        final boolean clockwise; // 軸に対して時計回りか否か
        if (!prime) {
            clockwise = face.clockwise;
        } else {
            clockwise = !face.clockwise;
        }
        move(face.axis, clockwise, posCorners);
        move(face.axis, clockwise, posEdges);
    }

    private void move(final Axis axis, final boolean clockwise, final List<Pos> pos) {
        /* 移動 1>2>3>4 */
        final Piece buffer = pick(pos.get(3));
        pick(pos.get(2)).pos(pos.get(3)).rollAll(axis, clockwise);
        pick(pos.get(1)).pos(pos.get(2)).rollAll(axis, clockwise);
        pick(pos.get(0)).pos(pos.get(1)).rollAll(axis, clockwise);
        buffer.pos(pos.get(0)).rollAll(axis, clockwise);
    }

    /**
     * 配列を反転させます。
     */
    private static <T> List<T> reverse(final T[] ary) {
        final List<T> list = new ArrayList<T>(Arrays.asList(ary));
        Collections.reverse(list);
        return list;
        //return asList.toArray(new T[asList.size()]);
    }

    /**
     * 動作確認をします。
     */
    public static void main(final String[] args) {
        final Cube cube = new Cube();
        final CubeViewer viewer = new CubeViewer(cube);

        final String org = viewer.getColorString();

        final String op = "RUR'U'";
        System.out.println("-- " + op + " --");
        for (int i = 0; i < 6; i++) {
            cube.rotate(CubeCommand.of(op));
            System.out.println(viewer.getColorString(Face.F));
            System.out.println();
        }
        //cube.rotate(CubeCommand.of("R'"));

        final String after = viewer.getColorString();
        System.out.println(dfAfter(org, after));
        System.out.println(cube.history);

        //sub2(cube, viewer, org);

        System.out.println("isGoal:" + cube.isGoal());

    }

    @SuppressWarnings("unused")
    private static void sub2(final Cube cube, final CubeViewer viewer, final String org) {
        /* シャッフル */
        for (int i = 0; i < 2; i++) {
            cube.rotate(CubeCommand.ofRandome());
        }
        System.out.println(cube.history);
        final String shuffle = viewer.getColorString();
        System.out.println("-- df --");
        System.out.println(dfAfter(org, shuffle));

        /* 繰り返し実験 */
        for (int i = 0; i < 10; i++) {
            System.out.println("〓" + i + "〓");
            final Cube c1 = new Cube(cube);
            final CubeViewer v1 = new CubeViewer(c1);
            for (int j = 0; j < 7; j++) {

                ///* ランダム */
                //Face f = Roulette.randomOne(Face.values());
                //c1.rotate(f, Roulette.getRandomTrueOrFalse());

                /* TODO D面クロス */

                if (c1.isGoal()) {
                    System.out.println("=完成=");
                    break;
                }

            }
            final String after = v1.getColorString1Line();
            System.out.println(c1.history);
            System.out.println(after);
            if (c1.isGoal()) {
                break;
            }
        }
    }

    /**
     * AとBで同じ場所をマスクした文字列を返します。
     * @param a A
     * @param b B
     */
    @SuppressWarnings("unused")
    private static String df(final String a, final String b) {
        final char[] c1 = a.toCharArray();
        final char[] c2 = b.toCharArray();

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c1.length; i++) {
            if (c1[i] == c2[i]) {
                sb.append(c1[i]);
            } else {
                sb.append('*');
            }
        }

        return sb.toString();
    }

    /**
     * Bで変わった場所を表示した文字列を返します。
     * @param a A
     * @param b B
     */
    private static String dfAfter(final String a, final String b) {
        final char[] c1 = a.toCharArray();
        final char[] c2 = b.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c1.length; i++) {
            if (c1[i] == c2[i] && c1[i] != '\n' && c1[i] != ' ') {
                //sb.append(c1[i]);
                sb.append('_');
            } else {
                sb.append(c2[i]);
            }
        }
        return sb.toString();
    }

}
