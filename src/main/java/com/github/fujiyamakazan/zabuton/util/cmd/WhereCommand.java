<<<<<<< HEAD
package com.github.fujiyamakazan.zabuton.util.cmd;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WhereCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long size;
    private final Date date;
    private final String path;

    public static void main(String[] args) {
        String line = "   8454445   2017/01/09      11:38:47  C:\\Users\\xxx\\Music\\00 xxx xxx.mp3";
        WhereCommand where = new WhereCommand(line);
        System.out.println(where.getSize());
        System.out.println(where.getDate());
        System.out.println(where.getPath());
    }

    public WhereCommand(String line) {

        String size = null;
        String date = null;
        String time = null;
        String path = null;

        StringBuilder buffer = new StringBuilder();
        int lastIndex = line.toCharArray().length - 1;
        boolean split = true;
        for (int i = 0; i <= lastIndex; i++) {
            int nextIndex = i + 1;
            char c = line.charAt(i);
            boolean doFlash = false;

            if (split && c == ' ') {
                if (buffer.length() == 0) {
                    continue;

                } else {
                    doFlash = true;
                }
            } else {
                buffer.append(c);
            }
            if (nextIndex > lastIndex) {
                doFlash = true;
            }

            if (doFlash) {
                if (size == null) {
                    size = buffer.toString();
                } else {
                    if (date == null) {
                        date = buffer.toString();
                    } else {
                        if (time == null) {
                            time = buffer.toString();
                            split = false;
                        } else {
                            path = buffer.toString().trim();
                        }
                    }
                }
                buffer.setLength(0); // clear
            }
        }

        this.size = Long.parseLong(size);
        try {
            this.date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(date + " " + time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.path = path;

    }

    public Long getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "WhereCommand [size=" + size
            + ", date=" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
            + ", path=" + path + "]";
    }



}
=======
package com.github.fujiyamakazan.zabuton.util.cmd;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WhereCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long size;
    private final Date date;
    private final String path;

    public static void main(String[] args) {
        String line = "   8454445   2017/01/09      11:38:47  C:\\Users\\xxx\\Music\\00 xxx xxx.mp3";
        WhereCommand where = new WhereCommand(line);
        System.out.println(where.getSize());
        System.out.println(where.getDate());
        System.out.println(where.getPath());
    }

    public WhereCommand(String line) {

        String size = null;
        String date = null;
        String time = null;
        String path = null;

        StringBuilder buffer = new StringBuilder();
        int lastIndex = line.toCharArray().length - 1;
        boolean split = true;
        for (int i = 0; i <= lastIndex; i++) {
            int nextIndex = i + 1;
            char c = line.charAt(i);
            boolean doFlash = false;

            if (split && c == ' ') {
                if (buffer.length() == 0) {
                    continue;

                } else {
                    doFlash = true;
                }
            } else {
                buffer.append(c);
            }
            if (nextIndex > lastIndex) {
                doFlash = true;
            }

            if (doFlash) {
                if (size == null) {
                    size = buffer.toString();
                } else {
                    if (date == null) {
                        date = buffer.toString();
                    } else {
                        if (time == null) {
                            time = buffer.toString();
                            split = false;
                        } else {
                            path = buffer.toString().trim();
                        }
                    }
                }
                buffer.setLength(0); // clear
            }
        }

        this.size = Long.parseLong(size);
        try {
            this.date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(date + " " + time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.path = path;

    }

    public Long getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "WhereCommand [size=" + size
            + ", date=" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
            + ", path=" + path + "]";
    }



}
>>>>>>> refs/remotes/origin/master
