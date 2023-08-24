package com.github.fujiyamakazan.zabuton.app.cube;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class Cube {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cube.class);

    /**
     * 位置を識別します。
     */
    private enum Pos {
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

    /** Rのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_R = new Pos[] { Pos.FUR, Pos.BUR, Pos.BDR, Pos.FDR };
    /** Rのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_R = new Pos[] { Pos.FR, Pos.UR, Pos.BR, Pos.DR };
    /** Uのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_U = new Pos[] { Pos.FUL, Pos.BUL, Pos.BUR, Pos.FUR };
    /** Uのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_U = new Pos[] { Pos.FU, Pos.UL, Pos.BU, Pos.UR };
    /** Fのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_F = new Pos[] { Pos.FUL, Pos.FUR, Pos.FDR, Pos.FDL };
    /** Fのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_F = new Pos[] { Pos.FU, Pos.FR, Pos.FD, Pos.FL };
    /** Bのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_B = new Pos[] { Pos.BUR, Pos.BUL, Pos.BDL, Pos.BDR };
    /** Bのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_B = new Pos[] { Pos.BU, Pos.BL, Pos.BD, Pos.BR };
    /** Lのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_L = new Pos[] { Pos.BUL, Pos.FUL, Pos.FDL, Pos.BDL };
    /** Lのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_L = new Pos[] { Pos.UL, Pos.FL, Pos.DL, Pos.BL };
    /** Dのときのコーナーの遷移です。 */
    private static final Pos[] ORDER_CORNER_D = new Pos[] { Pos.FDL, Pos.FDR, Pos.BDR, Pos.BDL };
    /** Dのときのエッジの遷移です。 */
    private static final Pos[] ORDER_EDGE_D = new Pos[] { Pos.FD, Pos.DR, Pos.BD, Pos.DL };

    /**
     * 面を識別します。
     */
    private enum Face {
        U, F, B, L, R, D
    }

    /**
     * 移動の種類を識別します。
     */
    private enum Move {
        U, U_, F, F_, B, B_, L, L_, R, R_, D, D_
    }

    /**
     * 色を識別します。
     */
    private enum Color {
        Y, B, G, O, R, W
    }

    /**
     * 配色情報（色と方向）です。
     */
    private static class FaceColor {
        private Face face;
        private Color color;

        public static FaceColor of(Face face, Color color) {
            FaceColor v = new FaceColor();
            v.face = face;
            v.color = color;
            return v;
        }

        public void roll(Move move) {

            Face[] patternR = new Face[] { Face.U, Face.B, Face.D, Face.F };

            switch (move) {
                case R: // R面に向かって時計回り(x)

                    this.face = roll(patternR, this.face);

                    /*
                    switch (face) {
                        case U:
                            this.face = Face.B;
                            break;
                        case B:
                            this.face = Face.D;
                            break;
                        case D:
                            this.face = Face.F;
                            break;
                        case F:
                            this.face = Face.U;
                            break;
                        default:
                            // L,R 変更無し
                            break;
                    }
                    */

                    break;

                case R_: // R面に向かって反時計回り(x')
                    switch (face) {
                        case U:
                            this.face = Face.F;
                            break;
                        case B:
                            this.face = Face.U;
                            break;
                        case D:
                            this.face = Face.B;
                            break;
                        case F:
                            this.face = Face.D;
                            break;
                        default:
                            // L,R 変更無し
                            break;
                    }
                    break;

                case U: // U面に向かって時計回り(y)
                    switch (face) {
                        case F:
                            this.face = Face.L;
                            break;
                        case L:
                            this.face = Face.B;
                            break;
                        case B:
                            this.face = Face.R;
                            break;
                        case R:
                            this.face = Face.F;
                            break;
                        default:
                            // U,D 変更無し
                            break;
                    }
                    break;

                case U_: // U面に向かって反時計回り(y')
                    switch (face) {
                        case F:
                            this.face = Face.R;
                            break;
                        case L:
                            this.face = Face.F;
                            break;
                        case B:
                            this.face = Face.L;
                            break;
                        case R:
                            this.face = Face.B;
                            break;
                        default:
                            // U,D 変更無し
                            break;
                    }
                    break;

                case F: // F面に向かって時計回り(z)
                    switch (face) {
                        case U:
                            this.face = Face.R;
                            break;
                        case R:
                            this.face = Face.D;
                            break;
                        case D:
                            this.face = Face.L;
                            break;
                        case L:
                            this.face = Face.U;
                            break;
                        default:
                            // F,B 変更無し
                            break;
                    }
                    break;

                case F_: // F面に向かって反時計回り(z')
                    switch (face) {
                        case U:
                            this.face = Face.L;
                            break;
                        case R:
                            this.face = Face.U;
                            break;
                        case D:
                            this.face = Face.R;
                            break;
                        case L:
                            this.face = Face.D;
                            break;
                        default:
                            // F,B 変更無し
                            break;
                    }
                    break;

                //TODO D, D', B, B', L, L'

                default:
                    throw new RuntimeException();
            }
        }

        private static Face roll(Face[] pattern, Face face) {
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

        @Override
        public String toString() {
            return face + "[" + color + "]";
        }

    }

    private List<Pos> history = Generics.newArrayList();

    /**
     * ピースです。
     * 位置情報と配色情報を持ちます。
     */
    private class Piece {
        private Pos pos;
        protected final List<FaceColor> vectors;

        public Piece(FaceColor... vectors) {
            this.vectors = Arrays.asList(vectors);
        }

        public Piece pos(Pos pos) {
            history.add(pos);
            this.pos = pos;
            return this;
        }

        public void roll(Move move) {
            for (FaceColor v : vectors) {
                v.roll(move);
            }
        }
    }

    /** ピースです。 */
    private List<Piece> piecies = Generics.newArrayList();

    /**
     * コンストラクタです。
     */
    public Cube() {
        /* センター */
        piecies.add(new Piece(FaceColor.of(Face.U, Color.Y)).pos(Pos.U));
        piecies.add(new Piece(FaceColor.of(Face.F, Color.B)).pos(Pos.F));
        piecies.add(new Piece(FaceColor.of(Face.B, Color.G)).pos(Pos.B));
        piecies.add(new Piece(FaceColor.of(Face.L, Color.O)).pos(Pos.L));
        piecies.add(new Piece(FaceColor.of(Face.R, Color.R)).pos(Pos.R));
        piecies.add(new Piece(FaceColor.of(Face.D, Color.W)).pos(Pos.D));
        /* エッジ(上層) */
        piecies.add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.B, Color.G)).pos(Pos.BU));
        piecies.add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.L, Color.O)).pos(Pos.UL));
        piecies.add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.R, Color.R)).pos(Pos.UR));
        piecies.add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.F, Color.B)).pos(Pos.FU));
        /* エッジ(中層) */
        piecies.add(new Piece(FaceColor.of(Face.B, Color.G), FaceColor.of(Face.L, Color.O)).pos(Pos.BL));
        piecies.add(new Piece(FaceColor.of(Face.B, Color.G), FaceColor.of(Face.R, Color.R)).pos(Pos.BR));
        piecies.add(new Piece(FaceColor.of(Face.F, Color.B), FaceColor.of(Face.L, Color.O)).pos(Pos.FL));
        piecies.add(new Piece(FaceColor.of(Face.F, Color.B), FaceColor.of(Face.R, Color.R)).pos(Pos.FR));
        /* エッジ(下層) */
        piecies.add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.B, Color.G)).pos(Pos.BD));
        piecies.add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.L, Color.O)).pos(Pos.DL));
        piecies.add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.R, Color.R)).pos(Pos.DR));
        piecies.add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.F, Color.B)).pos(Pos.FD));
        /* コーナー(上層) */
        piecies
            .add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.B, Color.G), FaceColor.of(Face.L, Color.O))
                .pos(Pos.BUL));
        piecies
            .add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.B, Color.G), FaceColor.of(Face.R, Color.R))
                .pos(Pos.BUR));
        piecies
            .add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.F, Color.B), FaceColor.of(Face.L, Color.O))
                .pos(Pos.FUL));
        piecies
            .add(new Piece(FaceColor.of(Face.U, Color.Y), FaceColor.of(Face.F, Color.B), FaceColor.of(Face.R, Color.R))
                .pos(Pos.FUR));
        /* コーナー(下層) */
        piecies
            .add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.B, Color.G), FaceColor.of(Face.L, Color.O))
                .pos(Pos.BDL));
        piecies
            .add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.B, Color.G), FaceColor.of(Face.R, Color.R))
                .pos(Pos.BDR));
        piecies
            .add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.F, Color.B), FaceColor.of(Face.L, Color.O))
                .pos(Pos.FDL));
        piecies
            .add(new Piece(FaceColor.of(Face.D, Color.W), FaceColor.of(Face.F, Color.B), FaceColor.of(Face.R, Color.R))
                .pos(Pos.FDR));
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
        for (FaceColor v : p.vectors) {
            if (v.face.equals(face)) {
                return v.color.name();
            }
        }
        throw new RuntimeException("pos=" + pos + ", face=" + face);
    }

    private void move(Move move) {
        switch (move) {
            case R:
                move(move, ORDER_CORNER_R); // コーナー移動
                move(move, ORDER_EDGE_R); // エッジ移動
                break;

            case R_:
                move(move, reverse(ORDER_CORNER_R)); // コーナー移動
                move(move, reverse(ORDER_EDGE_R)); // エッジ移動
                break;

            case U:
                move(move, ORDER_CORNER_U); // コーナー移動
                move(move, ORDER_EDGE_U); // エッジ移動
                break;

            case U_:
                move(move, reverse(ORDER_CORNER_U)); // コーナー移動
                move(move, reverse(ORDER_EDGE_U)); // エッジ移動
                break;

            case F:
                move(move, ORDER_CORNER_F); // コーナー移動
                move(move, ORDER_EDGE_F); // エッジ移動
                break;

            case F_:
                move(move, reverse(ORDER_CORNER_F)); // コーナー移動
                move(move, reverse(ORDER_EDGE_F)); // エッジ移動
                break;

            case B:
                move(move, ORDER_CORNER_B); // コーナー移動
                move(move, ORDER_EDGE_B); // エッジ移動
                break;

            case B_:
                move(move, reverse(ORDER_CORNER_B)); // コーナー移動
                move(move, reverse(ORDER_EDGE_B)); // エッジ移動
                break;

            case L:
                move(move, ORDER_CORNER_L); // コーナー移動
                move(move, ORDER_EDGE_L); // エッジ移動
                break;

            case L_:
                move(move, reverse(ORDER_CORNER_L)); // コーナー移動
                move(move, reverse(ORDER_EDGE_L)); // エッジ移動
                break;

            case D:
                move(move, ORDER_CORNER_D); // コーナー移動
                move(move, ORDER_EDGE_D); // エッジ移動
                break;

            case D_:
                move(move, reverse(ORDER_CORNER_D)); // コーナー移動
                move(move, reverse(ORDER_EDGE_D)); // エッジ移動
                break;
            default:
                throw new IllegalArgumentException(move.name());
        }

    }

    private void move(Move move, Pos[] pos) {
        /* 移動 1>2>3>4 */
        Piece buffer = pick(pos[3]);
        pick(pos[2]).pos(pos[3]).roll(move);
        pick(pos[1]).pos(pos[2]).roll(move);
        pick(pos[0]).pos(pos[1]).roll(move);
        buffer.pos(pos[0]).roll(move);
    }

    private static Pos[] reverse(Pos[] ary) {
        List<Pos> asList = Arrays.asList(ary);
        Collections.reverse(asList);
        return asList.toArray(new Pos[asList.size()]);
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        Cube cube = new Cube();
        String org = cube.get();
        System.out.println(org);

        //System.out.println("-- RUR'U' --");
        cube.move(Move.R);
        //cube.move(Move.U);
        //cube.move(Move.R_);
        //cube.move(Move.U_);
        //cube.move(Move.D);

        System.out.println();
        String after = cube.get();
        System.out.println(after);
        //System.out.println(cube.history);

        System.out.println("-- df --");
        System.out.println(df(org, after));

    }

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

}
//package com.github.fujiyamakazan.zabuton.app.cube;
//
//import java.util.List;
//
//import org.apache.wicket.util.lang.Generics;
//
//public class Cube {
//    @SuppressWarnings("unused")
//    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cube.class);
//
//    /**
//     * 実行します。
//     */
//    public static void main(String[] args) {
//
//        Cube c = new Cube();
//        c.init();
//        System.out.println(c.toStringAddress());
//
//        Cube cube = new Cube();
//        cube.init();
//
//        System.out.println(cube.toString());
//
//        cube.turnR();
//
//        System.out.println(cube.toString());
//
//        cube.turnU();
//
//        System.out.println(cube.toString());
//
//        //        int[] ary = { 1, 2, 3, 4, 5, 6, 7, 8 };
//        //        for (int i = 0; i < ary.length; i++) {
//        //            int idx = ary[i];
//        //            //System.out.println(idx + "->" + ((idx + 6) % 8));
//        //            System.out.println(idx + "->" + (idx-2));
//        //        }
//        //
//        //        //        cels[1] = from.cels[7];
//        //        //        cels[2] = from.cels[8];
//        //        //        cels[3] = from.cels[1];
//        //        //        cels[4] = from.cels[2];
//        //        //        cels[5] = from.cels[3];
//        //        //        cels[6] = from.cels[4];
//        //        //        cels[7] = from.cels[5];
//        //        //        cels[8] = from.cels[6];
//
//    }
//
//    private void turnR() {
//
//        final Face buffUpper = new Face(upper);
//        buffUpper.cels[3] = front.cels[3];
//        buffUpper.cels[4] = front.cels[4];
//        buffUpper.cels[5] = front.cels[5];
//
//        final Face buffBottom = new Face(bottom);
//        buffBottom.cels[1] = back.cels[1];
//        buffBottom.cels[8] = back.cels[8];
//        buffBottom.cels[7] = back.cels[7];
//
//        final Face buffFront = new Face(front);
//        buffFront.cels[3] = bottom.cels[1];
//        buffFront.cels[4] = bottom.cels[8];
//        buffFront.cels[5] = bottom.cels[7];
//
//        final Face buffBack = new Face(back);
//        buffBack.cels[1] = upper.cels[3];
//        buffBack.cels[8] = upper.cels[4];
//        buffBack.cels[7] = upper.cels[5];
//
//        final Face bufLeft = new Face(left);
//
//        final Face bufRight = new Face(right);
//        roll(bufRight, right);
//
//        upper = buffUpper;
//        bottom = buffBottom;
//        front = buffFront;
//        back = buffBack;
//        left = bufLeft;
//        right = bufRight;
//
//    }
//
//    private void turnU() {
//
//        final Face buffUpper = new Face(upper);
//        roll(buffUpper, upper);
//
//        final Face buffBottom = new Face(bottom);
//
//        final Face buffFront = new Face(front);
//        buffFront.cels[1] = right.cels[1];
//        buffFront.cels[2] = right.cels[2];
//        buffFront.cels[3] = right.cels[3];
//
//        final Face buffBack = new Face(back);
//        buffBack.cels[5] = left.cels[7];
//        buffBack.cels[6] = left.cels[6];
//        buffBack.cels[7] = left.cels[5];
//
//        final Face bufLeft = new Face(left);
//        bufLeft.cels[5] = front.cels[3];
//        bufLeft.cels[6] = front.cels[2];
//        bufLeft.cels[7] = front.cels[1];
//
//        final Face bufRight = new Face(right);
//        bufRight.cels[1] = back.cels[7];
//        bufRight.cels[2] = back.cels[6];
//        bufRight.cels[3] = back.cels[5];
//
//        upper = buffUpper;
//        bottom = buffBottom;
//        front = buffFront;
//        back = buffBack;
//        left = bufLeft;
//        right = bufRight;
//
//    }
//
//    private static void roll(Face to, Face from) {
//        String[] cels = new String[9];
//
//        cels[0] = from.cels[0];
//        for (int i = 1; i <= 8; i++) {
//            int fromIdx = i - 2;
//            if (fromIdx <= 0) {
//                fromIdx += 8;
//            }
//            cels[i] = from.cels[fromIdx];
//
//        }
//        //        cels[1] = from.cels[7];
//        //        cels[2] = from.cels[8];
//        //        cels[3] = from.cels[1];
//        //        cels[4] = from.cels[2];
//        //        cels[5] = from.cels[3];
//        //        cels[6] = from.cels[4];
//        //        cels[7] = from.cels[5];
//        //        cels[8] = from.cels[6];
//
//        for (int i = 0; i < cels.length; i++) {
//            to.cels[i] = cels[i];
//        }
//
//    }
//
//    private Face upper;
//    private Face front;
//    private Face bottom;
//    private Face back;
//    private Face left;
//    private Face right;
//
//    private void init() {
//        upper = new Face("□", false);
//        front = new Face("△", false);
//        left = new Face("○", true);
//        bottom = new Face("◇", true);
//        back = new Face("▽", true);
//        right = new Face("☆", false);
//
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        Face dummy = new Face("　", false);
//        sb.append(joinString(dummy.toString(), dummy.toString(), upper.toString()));
//        sb.append(joinString(left.toString(), back.toString(), front.toString(), right.toString()));
//        sb.append(joinString(dummy.toString(), bottom.toString()));
//        return sb.toString();
//    }
//
//    private String toStringAddress() {
//        StringBuilder sb = new StringBuilder();
//        Face dummy = new Face("　", false);
//        sb.append(
//            joinString(dummy.toStringAddress(), dummy.toStringAddress(), upper.toStringAddress()));
//        sb.append(joinString(left.toStringAddress(), back.toStringAddress(), front.toStringAddress(),
//            right.toStringAddress()));
//        sb.append(joinString(dummy.toStringAddress(), bottom.toStringAddress()));
//        return sb.toString();
//    }
//

//
//    private class Face {
//        private String[] cels = new String[9];
//        private boolean isHide;
//
//        public Face(String mark, boolean hide) {
//            for (int i = 0; i < cels.length; i++) {
//                cels[i] = mark;
//            }
//            this.isHide = hide;
//        }
//
//        public Face(Face other) {
//            for (int i = 0; i < other.cels.length; i++) {
//                cels[i] = other.cels[i];
//            }
//            this.isHide = other.isHide;
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            String str;
//            if (isHide == false) {
//                sb.append(cels[1] + cels[2] + cels[3] + "\n");
//                sb.append(cels[8] + cels[0] + cels[4] + "\n");
//                sb.append(cels[7] + cels[6] + cels[5] + "\n");
//                str = sb.toString();
//
//            } else {
//                sb.append(cels[5] + cels[6] + cels[7] + "\n");
//                sb.append(cels[4] + cels[0] + cels[8] + "\n");
//                sb.append(cels[3] + cels[2] + cels[1] + "\n");
//                str = sb.toString();
//
//                str = str.replaceAll("□", "■");
//                str = str.replaceAll("△", "▲");
//                str = str.replaceAll("☆", "★");
//                str = str.replaceAll("▽", "▼");
//                str = str.replaceAll("○", "●");
//                str = str.replaceAll("◇", "◆");
//            }
//            return str;
//        }
//
//        public String toStringAddress() {
//            if (cels[1].equals("　")) {
//                return "　　　\n　　　\n　　　\n";
//            }
//
//            StringBuilder sb = new StringBuilder();
//            if (isHide == false) {
//                sb.append("①②③\n");
//                sb.append("⑧○④\n");
//                sb.append("⑦⑥⑤\n");
//            } else {
//                sb.append("⑤⑥⑦\n");
//                sb.append("④○⑧\n");
//                sb.append("③②①\n");
//            }
//            return sb.toString();
//        }
//    }
//
//}
