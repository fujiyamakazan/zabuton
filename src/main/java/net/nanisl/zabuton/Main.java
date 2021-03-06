package net.nanisl.zabuton;

import net.nanisl.zabuton.container.ZabuContainer;
import net.nanisl.zabuton.myapp.MyApp;

public class Main {
    public static void main(String[] args) {
        ZabuContainer.invoke(MyApp.class, "zabuton");
    }
}
