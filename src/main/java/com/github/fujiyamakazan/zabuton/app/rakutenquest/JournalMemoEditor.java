package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.Serializable;
import java.util.List;

public abstract class JournalMemoEditor implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    public abstract void convert(List<Journal> journals);


}
