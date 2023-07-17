package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

public class ContainerMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ContainerMap.class);

    private class Container implements Serializable {
        private static final long serialVersionUID = 1L;
        private final LocalDate date;

        public Container(LocalDate date) {
            this.date = date;
        }
    }

    public static void main(String[] args) {

        File docs = new File(EnvUtils.getUserDocuments(), "tools\\お丸");
        Collection<File> files = FileUtils.listFiles(docs, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        ContainerMap cm = new ContainerMap();
        for (File f : files) {
            cm.put(f);
        }
        LOGGER.debug(cm.toString());
    }

    private List<Container> list = Generics.newArrayList();
    
    private void put(File file) {

        long lng = file.lastModified();
        LocalDate date = Chronus.localDateOf(lng);

        Container c = new Container(date);
        
        

    }

}
