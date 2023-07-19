package com.github.fujiyamakazan.zabuton.app.cube;

import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class Cube {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cube.class);

    /**
     * 実行します。
     */
    public static void main(String[] args) {

        Cube c = new Cube();
        c.init();
        System.out.println(c.toStringAddress());

        Cube cube = new Cube();
        cube.init();

        System.out.println(cube.toString());

        cube.turnR();

        System.out.println(cube.toString());

        cube.turnU();

        System.out.println(cube.toString());

        //        int[] ary = { 1, 2, 3, 4, 5, 6, 7, 8 };
        //        for (int i = 0; i < ary.length; i++) {
        //            int idx = ary[i];
        //            //System.out.println(idx + "->" + ((idx + 6) % 8));
        //            System.out.println(idx + "->" + (idx-2));
        //        }
        //
        //        //        cels[1] = from.cels[7];
        //        //        cels[2] = from.cels[8];
        //        //        cels[3] = from.cels[1];
        //        //        cels[4] = from.cels[2];
        //        //        cels[5] = from.cels[3];
        //        //        cels[6] = from.cels[4];
        //        //        cels[7] = from.cels[5];
        //        //        cels[8] = from.cels[6];

    }

    private void turnR() {

        final Face buffUpper = new Face(upper);
        buffUpper.cels[3] = front.cels[3];
        buffUpper.cels[4] = front.cels[4];
        buffUpper.cels[5] = front.cels[5];

        final Face buffBottom = new Face(bottom);
        buffBottom.cels[1] = back.cels[1];
        buffBottom.cels[8] = back.cels[8];
        buffBottom.cels[7] = back.cels[7];

        final Face buffFront = new Face(front);
        buffFront.cels[3] = bottom.cels[1];
        buffFront.cels[4] = bottom.cels[8];
        buffFront.cels[5] = bottom.cels[7];

        final Face buffBack = new Face(back);
        buffBack.cels[1] = upper.cels[3];
        buffBack.cels[8] = upper.cels[4];
        buffBack.cels[7] = upper.cels[5];

        final Face bufLeft = new Face(left);

        final Face bufRight = new Face(right);
        roll(bufRight, right);

        upper = buffUpper;
        bottom = buffBottom;
        front = buffFront;
        back = buffBack;
        left = bufLeft;
        right = bufRight;

    }

    private void turnU() {

        final Face buffUpper = new Face(upper);
        roll(buffUpper, upper);

        final Face buffBottom = new Face(bottom);

        final Face buffFront = new Face(front);
        buffFront.cels[1] = right.cels[1];
        buffFront.cels[2] = right.cels[2];
        buffFront.cels[3] = right.cels[3];

        final Face buffBack = new Face(back);
        buffBack.cels[5] = left.cels[7];
        buffBack.cels[6] = left.cels[6];
        buffBack.cels[7] = left.cels[5];

        final Face bufLeft = new Face(left);
        bufLeft.cels[5] = front.cels[3];
        bufLeft.cels[6] = front.cels[2];
        bufLeft.cels[7] = front.cels[1];

        final Face bufRight = new Face(right);
        bufRight.cels[1] = back.cels[7];
        bufRight.cels[2] = back.cels[6];
        bufRight.cels[3] = back.cels[5];

        upper = buffUpper;
        bottom = buffBottom;
        front = buffFront;
        back = buffBack;
        left = bufLeft;
        right = bufRight;

    }

    private static void roll(Face to, Face from) {
        String[] cels = new String[9];

        cels[0] = from.cels[0];
        for (int i = 1; i <= 8; i++) {
            int fromIdx = i - 2;
            if (fromIdx <= 0) {
                fromIdx += 8;
            }
            cels[i] = from.cels[fromIdx];

        }
        //        cels[1] = from.cels[7];
        //        cels[2] = from.cels[8];
        //        cels[3] = from.cels[1];
        //        cels[4] = from.cels[2];
        //        cels[5] = from.cels[3];
        //        cels[6] = from.cels[4];
        //        cels[7] = from.cels[5];
        //        cels[8] = from.cels[6];

        for (int i = 0; i < cels.length; i++) {
            to.cels[i] = cels[i];
        }

    }

    private Face upper;
    private Face front;
    private Face bottom;
    private Face back;
    private Face left;
    private Face right;

    private void init() {
        upper = new Face("□", false);
        front = new Face("△", false);
        left = new Face("○", true);
        bottom = new Face("◇", true);
        back = new Face("▽", true);
        right = new Face("☆", false);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Face dummy = new Face("　", false);
        sb.append(joinString(dummy.toString(), dummy.toString(), upper.toString()));
        sb.append(joinString(left.toString(), back.toString(), front.toString(), right.toString()));
        sb.append(joinString(dummy.toString(), bottom.toString()));
        return sb.toString();
    }

    private String toStringAddress() {
        StringBuilder sb = new StringBuilder();
        Face dummy = new Face("　", false);
        sb.append(
            joinString(dummy.toStringAddress(), dummy.toStringAddress(), upper.toStringAddress()));
        sb.append(joinString(left.toStringAddress(), back.toStringAddress(), front.toStringAddress(),
            right.toStringAddress()));
        sb.append(joinString(dummy.toStringAddress(), bottom.toStringAddress()));
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

    private class Face {
        private String[] cels = new String[9];
        private boolean isHide;

        public Face(String mark, boolean hide) {
            for (int i = 0; i < cels.length; i++) {
                cels[i] = mark;
            }
            this.isHide = hide;
        }

        public Face(Face other) {
            for (int i = 0; i < other.cels.length; i++) {
                cels[i] = other.cels[i];
            }
            this.isHide = other.isHide;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String str;
            if (isHide == false) {
                sb.append(cels[1] + cels[2] + cels[3] + "\n");
                sb.append(cels[8] + cels[0] + cels[4] + "\n");
                sb.append(cels[7] + cels[6] + cels[5] + "\n");
                str = sb.toString();

            } else {
                sb.append(cels[5] + cels[6] + cels[7] + "\n");
                sb.append(cels[4] + cels[0] + cels[8] + "\n");
                sb.append(cels[3] + cels[2] + cels[1] + "\n");
                str = sb.toString();

                str = str.replaceAll("□", "■");
                str = str.replaceAll("△", "▲");
                str = str.replaceAll("☆", "★");
                str = str.replaceAll("▽", "▼");
                str = str.replaceAll("○", "●");
                str = str.replaceAll("◇", "◆");
            }
            return str;
        }

        public String toStringAddress() {
            if (cels[1].equals("　")) {
                return "　　　\n　　　\n　　　\n";
            }

            StringBuilder sb = new StringBuilder();
            if (isHide == false) {
                sb.append("①②③\n");
                sb.append("⑧○④\n");
                sb.append("⑦⑥⑤\n");
            } else {
                sb.append("⑤⑥⑦\n");
                sb.append("④○⑧\n");
                sb.append("③②①\n");
            }
            return sb.toString();
        }
    }

}
