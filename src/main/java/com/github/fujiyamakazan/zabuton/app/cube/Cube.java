package com.github.fujiyamakazan.zabuton.app.cube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class Cube {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cube.class);

    /**
     * 操作を識別します。
     * @deprecated
     */
    private enum Move {
        U, U_, F, F_, B, B_, L, L_, R, R_, D, D_
    }

    /**
     * 面を識別します。
     */
    private enum Face {
        U, F, B, L, R, D
    }

    /**
     * 回転軸を識別します。
     */
    private enum Axis {
        X, Y, Z
    }

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
     * 色を識別します。
     */
    private enum Color {
        Y, B, G, O, R, W
    }

    /**
     * 操作を識別します。
     */
    private enum Op {

        R(Axis.X, new Pos[] { Pos.FUR, Pos.BUR, Pos.BDR, Pos.FDR }, new Pos[] { Pos.FR, Pos.UR, Pos.BR, Pos.DR }), //
        L(Axis.X, new Pos[] { Pos.BUL, Pos.FUL, Pos.FDL, Pos.BDL }, new Pos[] { Pos.UL, Pos.FL, Pos.DL, Pos.BL }), //

        U(Axis.Y, new Pos[] { Pos.FUL, Pos.BUL, Pos.BUR, Pos.FUR }, new Pos[] { Pos.FU, Pos.UL, Pos.BU, Pos.UR }), //
        D(Axis.Y, new Pos[] { Pos.FDL, Pos.FDR, Pos.BDR, Pos.BDL }, new Pos[] { Pos.FD, Pos.DR, Pos.BD, Pos.DL }), //

        F(Axis.Z, new Pos[] { Pos.FUL, Pos.FUR, Pos.FDR, Pos.FDL }, new Pos[] { Pos.FU, Pos.FR, Pos.FD, Pos.FL }), //
        B(Axis.Z, new Pos[] { Pos.BUR, Pos.BUL, Pos.BDL, Pos.BDR }, new Pos[] { Pos.BU, Pos.BL, Pos.BD, Pos.BR }), //

        ;

        /* 操作で移動するコーナーの位置です。 */
        private Pos[] corners;
        /* 操作で移動するエッジの位置です。 */
        private Pos[] edges;
        /* 操作の回転軸です。 */
        private Axis axis;

        private Op(Axis axis, Pos[] corners, Pos[] edges) {
            this.corners = corners;
            this.edges = edges;
            this.axis = axis;
        }

    }

    /** x(R面に向かって時計回り)のときの面の遷移です。 */
    private static final Face[] ORDER_FACE_X = new Face[] { Face.U, Face.B, Face.D, Face.F };
    /** y(U面に向かって時計回り)のときの面の遷移です。 */
    private static final Face[] ORDER_FACE_Y = new Face[] { Face.L, Face.B, Face.R, Face.F };
    /** z(F面に向かって時計回り)のときの面の遷移です。 */
    private static final Face[] ORDER_FACE_Z = new Face[] { Face.R, Face.D, Face.L, Face.U };

    /**
     * 配色情報（色と方向）です。
     */
    private static class Fc {
        private Face face;
        private Color color;

        public static Fc of(Face face, Color color) {
            Fc v = new Fc();
            v.face = face;
            v.color = color;
            return v;
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
    private class Piece {
        private Pos pos;
        protected final List<Fc> fcs;

        public Piece(Fc... fcs) {
            this.fcs = Arrays.asList(fcs);
        }

        public Piece pos(Pos pos) {
            this.pos = pos;
            return this;
        }

        public void roll(Axis axis, boolean reverse) {
            for (Fc v : fcs) {
                switch (axis) {
                    case X: // R面に向かって時計回り(x), 反時計回り(x')
                        v.face = roll(ORDER_FACE_X, v.face, reverse);
                        break;

                    case Y: // U面に向かって時計回り(y), 反時計回り(y')
                        v.face = roll(ORDER_FACE_Y, v.face, reverse);
                        break;

                    case Z: // F面に向かって時計回り(z), 反時計回り(z')
                        v.face = roll(ORDER_FACE_Z, v.face, reverse);
                        break;

                    default:
                        throw new RuntimeException();
                }
            }
        }

        private Face roll(Face[] pattern, Face face, boolean reverse) {
            if (reverse) {
                pattern = reverse(pattern);
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

    private static final String DUMMY = "    \n    \n    ";

    private String get() {
        String str = "";
        str += joinString(DUMMY, get(Face.U)) + "\n";
        str += joinString(get(Face.L), get(Face.F), get(Face.R)) + "\n";
        str += joinString(DUMMY, get(Face.D), DUMMY, get(Face.B)) + "\n";

        return str;
    }

    private String get(Face face) {
        String str = " ";
        switch (face) {
            case U:
                str += pcik(Pos.BUL, Face.U);
                str += pcik(Pos.BU, Face.U);
                str += pcik(Pos.BUR, Face.U);
                str += "\n ";
                str += pcik(Pos.UL, Face.U);
                str += pcik(Pos.U, Face.U);
                str += pcik(Pos.UR, Face.U);
                str += "\n ";
                str += pcik(Pos.FUL, Face.U);
                str += pcik(Pos.FU, Face.U);
                str += pcik(Pos.FUR, Face.U);
                break;
            case F:
                str += pcik(Pos.FUL, Face.F);
                str += pcik(Pos.FU, Face.F);
                str += pcik(Pos.FUR, Face.F);
                str += "\n ";
                str += pcik(Pos.FL, Face.F);
                str += pcik(Pos.F, Face.F);
                str += pcik(Pos.FR, Face.F);
                str += "\n ";
                str += pcik(Pos.FDL, Face.F);
                str += pcik(Pos.FD, Face.F);
                str += pcik(Pos.FDR, Face.F);
                break;
            case D:
                str += pcik(Pos.FDL, Face.D);
                str += pcik(Pos.FD, Face.D);
                str += pcik(Pos.FDR, Face.D);
                str += "\n ";
                str += pcik(Pos.DL, Face.D);
                str += pcik(Pos.D, Face.D);
                str += pcik(Pos.DR, Face.D);
                str += "\n ";
                str += pcik(Pos.BDL, Face.D);
                str += pcik(Pos.BD, Face.D);
                str += pcik(Pos.BDR, Face.D);
                break;
            case L:
                str += pcik(Pos.BUL, Face.L);
                str += pcik(Pos.UL, Face.L);
                str += pcik(Pos.FUL, Face.L);
                str += "\n ";
                str += pcik(Pos.BL, Face.L);
                str += pcik(Pos.L, Face.L);
                str += pcik(Pos.FL, Face.L);
                str += "\n ";
                str += pcik(Pos.BDL, Face.L);
                str += pcik(Pos.DL, Face.L);
                str += pcik(Pos.FDL, Face.L);
                break;
            case R:
                str += pcik(Pos.FUR, Face.R);
                str += pcik(Pos.UR, Face.R);
                str += pcik(Pos.BUR, Face.R);
                str += "\n ";
                str += pcik(Pos.FR, Face.R);
                str += pcik(Pos.R, Face.R);
                str += pcik(Pos.BR, Face.R);
                str += "\n ";
                str += pcik(Pos.FDR, Face.R);
                str += pcik(Pos.DR, Face.R);
                str += pcik(Pos.BDR, Face.R);
                break;
            case B:
                str += pcik(Pos.BUR, Face.B);
                str += pcik(Pos.BU, Face.B);
                str += pcik(Pos.BUL, Face.B);
                str += "\n ";
                str += pcik(Pos.BR, Face.B);
                str += pcik(Pos.B, Face.B);
                str += pcik(Pos.BL, Face.B);
                str += "\n ";
                str += pcik(Pos.BDR, Face.B);
                str += pcik(Pos.BD, Face.B);
                str += pcik(Pos.BDL, Face.B);
                break;
            default:
                throw new IllegalArgumentException(face.name());
        }

        return str;
    }

    private Piece pick(Pos pos) {
        for (Piece p : piecies) {
            if (p.pos.equals(pos)) {
                return p;
            }
        }
        throw new RuntimeException("pos=" + pos);
    }

    private String pcik(Pos pos, Face face) {
        Piece p = pick(pos);
        for (Fc v : p.fcs) {
            if (v.face.equals(face)) {
                return v.color.name();
            }
        }
        throw new RuntimeException("pos=" + pos + ", face=" + face);
    }

    private void move(Op op, boolean prime) {
        switch (op) {
            case R:
            case U:
            case F:
                move(op.axis, prime, op.corners, prime);
                move(op.axis, prime, op.edges, prime);
                break;
            case L:
            case D:
            case B:
                move(op.axis, !prime, op.corners, prime);
                move(op.axis, !prime, op.edges, prime);
                break;

            default:
                throw new IllegalArgumentException(op.name());
        }
    }

    private void move(Axis axis, boolean reverse, Pos[] pos, boolean posRevers) {
        if (posRevers) {
            pos = reverse(pos);
        }

        /* 移動 1>2>3>4 */
        Piece buffer = pick(pos[3]);
        pick(pos[2]).pos(pos[3]).roll(axis, reverse);
        pick(pos[1]).pos(pos[2]).roll(axis, reverse);
        pick(pos[0]).pos(pos[1]).roll(axis, reverse);
        buffer.pos(pos[0]).roll(axis, reverse);
    }

    private void movePosi(Op op) {
        move(op, false);
    }

    private void movePrime(Op op) {
        move(op, true);
    }

    /**
     * 配列を反転させます。
     */
    private static Pos[] reverse(Pos[] ary) {
        List<Pos> asList = new ArrayList<Pos>(Arrays.asList(ary));
        Collections.reverse(asList);
        return asList.toArray(new Pos[asList.size()]);
    }

    /**
     * 配列を反転させます。
     */
    private static Face[] reverse(Face[] ary) {
        List<Face> asList = new ArrayList<Face>(Arrays.asList(ary));
        Collections.reverse(asList);
        return asList.toArray(new Face[asList.size()]);
    }

    /**
     * AとBで同じ場所をマスクした文字列を返します。
     * @param a A
     * @param b B
     */
    @SuppressWarnings("unused")
    private static String df(String a, String b) {
        char[] c1 = a.toCharArray();
        char[] c2 = b.toCharArray();

        StringBuilder sb = new StringBuilder();
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
    @SuppressWarnings("unused")
    private static String dfAfter(String a, String b) {
        char[] c1 = a.toCharArray();
        char[] c2 = b.toCharArray();
        StringBuilder sb = new StringBuilder();
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

    private static String joinString(String... linesList) {
        List<String> joinedList = null;
        for (String lines : linesList) {
            List<String> listLine = Generics.newArrayList();
            for (String line : lines.split("\n")) {
                listLine.add(line);
            }
            if (joinedList == null) {
                joinedList = listLine;
            } else {
                for (int i = 0; i < listLine.size(); i++) {
                    joinedList.set(i, joinedList.get(i) + listLine.get(i));
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String str : joinedList) {
            sb.append(str + "\n");
        }
        return sb.toString();
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        Cube cube = new Cube();
        final String org = cube.get();
        //System.out.println(org);

        System.out.println("-- RUR'U' --");

        for (int i = 0; i < 6; i++) {
            cube.movePosi(Op.R);
            cube.movePosi(Op.U);
            cube.movePrime(Op.R);
            cube.movePrime(Op.U);
            System.out.println(cube.get(Face.F));
            System.out.println();
        }

        //cube.move(Move.L_);

        //System.out.println();
        String after = cube.get();
        //System.out.println(after);
        //System.out.println(cube.history);

        System.out.println("-- df --");
        //System.out.println(df(org, after));
        System.out.println(dfAfter(org, after));

    }

}
