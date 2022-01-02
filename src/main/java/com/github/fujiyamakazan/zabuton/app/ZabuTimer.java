package com.github.fujiyamakazan.zabuton.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZabuTimer {

    @Autowired(required = false)
    private I_TimerListener listener;

    /** Bean生成後に一度だけスケジュールを起動する */
    @PostConstruct
    protected void startSchedule() {

        if (listener == null) {
            return;
        }

        long span = listener.getSpan();
        if (span < 0) {
            return;
        }

        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.tick();
                }
            }
        };
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(command, span, span, TimeUnit.SECONDS);
    }

    /**
     * 呼び出されるリスナーの定義
     */
    public interface I_TimerListener {

        /**
         * @return 呼出し周期(秒) です。
         */
        int getSpan();

        /** 処理をします。 */
        void tick();
    }
}
