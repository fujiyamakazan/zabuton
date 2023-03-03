package com.github.fujiyamakazan.zabuton.spring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ITimerListnerを実装したコンポーネントをスケジュールで実行します。
 * @author fujiyama
 */
@Component
public class ZabuTimer {

    @Autowired(required = false)
    private ITimerListener listener;

    /*** Bean生成後に一度だけスケジュールを起動します。 */
    @PostConstruct
    protected void startSchedule() {

        if (this.listener == null) {
            return;
        }

        long span = this.listener.getSpan();
        if (span < 0) {
            return;
        }

        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (ZabuTimer.this.listener != null) {
                    ZabuTimer.this.listener.tick();
                }
            }
        };
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(command, span, span, TimeUnit.SECONDS);
    }

    /**
     * 呼び出されるリスナーの定義です。
     */
    public interface ITimerListener {

        /**
         * 呼出し周期(秒)を返します。
         * @return 呼出し周期(秒) です。
         */
        int getSpan();

        /** 処理をします。 */
        void tick();
    }
}
