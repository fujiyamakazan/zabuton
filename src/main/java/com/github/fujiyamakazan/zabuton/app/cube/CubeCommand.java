package com.github.fujiyamakazan.zabuton.app.cube;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.app.cube.Cube.Face;
import com.github.fujiyamakazan.zabuton.util.random.Roulette;

public class CubeCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CubeCommand.class);
    private final Face face;
    private final int dist;

    public CubeCommand(Face face, int dist) {
        this.face = face;
        this.dist = dist;
    }

    public Face getFace() {
        return face;
    }

    public int getDist() {
        return dist;
    }

    @Override
    public String toString() {
        String str = this.face.name();
        if (dist == -1) {
            str += "'";
        } else if (dist == 1) {
            str += "";
        } else {
            str += dist;
        }
        return str;
    }

    public static CubeCommand ofRandome() {
        Face f = Roulette.randomOne(Face.values());
        //int dist = Roulette.getRandomTrueOrFalse() ? 1 : -1;
        int dist = Roulette.randomOne(new Integer[] { -1, 1, 2 });
        return new CubeCommand(f, dist);
    }

    /**
     * 文字列からコマンドを生成します。プライム「'」とダブル「2」に対応します。
     */
    public static CubeCommand[] of(String str) {
        List<CubeCommand> commands = Generics.newArrayList();
        for (int i = 0; i < str.length(); i++) {
            char cmd = str.charAt(i);

            final int dist;
            if (i == str.length() - 1) {
                dist = 1;
            } else {
                char option = str.charAt(i + 1);
                switch (option) {
                    case '\'':
                        dist = -1;
                        i++;
                        break;
                    case '2':
                        dist = 2;
                        i++;
                        break;
                    default:
                        dist = 1;
                        break;
                }
            }
            commands.add(new CubeCommand(Face.valueOf(String.valueOf(cmd)), dist));
        }
        return commands.toArray(new CubeCommand[commands.size()]);
    }
}
