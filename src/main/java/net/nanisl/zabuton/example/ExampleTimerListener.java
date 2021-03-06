package net.nanisl.zabuton.example;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.nanisl.zabuton.app.ZabuApp;
import net.nanisl.zabuton.app.ZabuTimer.I_TimerListener;


/**
 * I_TimerListener#tickは周期的に実行されます。
 * @author fujiyama
 */
@Component
public class ExampleTimerListener implements I_TimerListener {

	private static final Logger log = LoggerFactory.getLogger(ZabuApp.class);

    @Override
    public int getSpan() {
        return 3;
        //return -1;
    }

    @Override
    public void tick() {
    	log.info(new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }
}
