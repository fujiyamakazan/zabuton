package com.github.fujiyamakazan.zabuton.app.cube;

import java.io.Serializable;

import com.github.fujiyamakazan.zabuton.app.cube.Cube.Face;
import com.github.fujiyamakazan.zabuton.app.cube.Cube.Pos;
import com.github.fujiyamakazan.zabuton.util.StringJoinerMultiLine;

public class CubeViewer implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CubeViewer.class);

    private Cube cube;

    private static final String DUMMY = "    \n    \n    ";

    public CubeViewer(Cube cube) {
        this.cube = cube;
    }



    /**
     * 配色情報の文字列を返します。
     */
    public String getColorString() {
        String str = "";
        str += StringJoinerMultiLine.joinString(DUMMY, getColorString(Face.U)) + "\n";
        str += StringJoinerMultiLine.joinString(getColorString(Face.L), getColorString(Face.F), getColorString(Face.R))
            + "\n";
        str += StringJoinerMultiLine.joinString(DUMMY, getColorString(Face.D), DUMMY, getColorString(Face.B)) + "\n";
        return str;
    }

    /**
     * 配色情報の文字列を返します。
     */
    public String getColorString(Face face) {
        String str = " ";
        switch (face) {
            case U:
                str += getColorString(Pos.BUL, Face.U);
                str += getColorString(Pos.BU, Face.U);
                str += getColorString(Pos.BUR, Face.U);
                str += "\n ";
                str += getColorString(Pos.UL, Face.U);
                str += getColorString(Pos.U, Face.U);
                str += getColorString(Pos.UR, Face.U);
                str += "\n ";
                str += getColorString(Pos.FUL, Face.U);
                str += getColorString(Pos.FU, Face.U);
                str += getColorString(Pos.FUR, Face.U);
                break;
            case F:
                str += getColorString(Pos.FUL, Face.F);
                str += getColorString(Pos.FU, Face.F);
                str += getColorString(Pos.FUR, Face.F);
                str += "\n ";
                str += getColorString(Pos.FL, Face.F);
                str += getColorString(Pos.F, Face.F);
                str += getColorString(Pos.FR, Face.F);
                str += "\n ";
                str += getColorString(Pos.FDL, Face.F);
                str += getColorString(Pos.FD, Face.F);
                str += getColorString(Pos.FDR, Face.F);
                break;
            case D:
                str += getColorString(Pos.FDL, Face.D);
                str += getColorString(Pos.FD, Face.D);
                str += getColorString(Pos.FDR, Face.D);
                str += "\n ";
                str += getColorString(Pos.DL, Face.D);
                str += getColorString(Pos.D, Face.D);
                str += getColorString(Pos.DR, Face.D);
                str += "\n ";
                str += getColorString(Pos.BDL, Face.D);
                str += getColorString(Pos.BD, Face.D);
                str += getColorString(Pos.BDR, Face.D);
                break;
            case L:
                str += getColorString(Pos.BUL, Face.L);
                str += getColorString(Pos.UL, Face.L);
                str += getColorString(Pos.FUL, Face.L);
                str += "\n ";
                str += getColorString(Pos.BL, Face.L);
                str += getColorString(Pos.L, Face.L);
                str += getColorString(Pos.FL, Face.L);
                str += "\n ";
                str += getColorString(Pos.BDL, Face.L);
                str += getColorString(Pos.DL, Face.L);
                str += getColorString(Pos.FDL, Face.L);
                break;
            case R:
                str += getColorString(Pos.FUR, Face.R);
                str += getColorString(Pos.UR, Face.R);
                str += getColorString(Pos.BUR, Face.R);
                str += "\n ";
                str += getColorString(Pos.FR, Face.R);
                str += getColorString(Pos.R, Face.R);
                str += getColorString(Pos.BR, Face.R);
                str += "\n ";
                str += getColorString(Pos.FDR, Face.R);
                str += getColorString(Pos.DR, Face.R);
                str += getColorString(Pos.BDR, Face.R);
                break;
            case B:
                str += getColorString(Pos.BUR, Face.B);
                str += getColorString(Pos.BU, Face.B);
                str += getColorString(Pos.BUL, Face.B);
                str += "\n ";
                str += getColorString(Pos.BR, Face.B);
                str += getColorString(Pos.B, Face.B);
                str += getColorString(Pos.BL, Face.B);
                str += "\n ";
                str += getColorString(Pos.BDR, Face.B);
                str += getColorString(Pos.BD, Face.B);
                str += getColorString(Pos.BDL, Face.B);
                break;
            default:
                throw new IllegalArgumentException(face.name());
        }

        return str;
    }

    private String getColorString(Pos pos, Face face) {
        return cube.getColorString(pos, face);
    }

    /**
     * 配色情報の文字列を返します。１組の行で表します。
     */
    public String getColorString1Line() {
        return StringJoinerMultiLine.joinString(
            getColorString(Face.U), getColorString(Face.L), getColorString(Face.F),
            getColorString(Face.R), getColorString(Face.D), getColorString(Face.B));
    }

}
