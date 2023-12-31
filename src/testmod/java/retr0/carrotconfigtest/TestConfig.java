package retr0.carrotconfigtest;

import retr0.carrotconfig.config.CarrotConfig;

import java.util.HashMap;

public class TestConfig extends CarrotConfig {
    @Entry
    public static boolean booleanTest = false;

    @Entry(min = 0)
    public static int intTest = 45;

    @Entry(min = 0)
    public static float floatTest = 30.0f;

    @Comment
    public static Comment commentTest;

    @Entry(isColor = true)
    public static int colorTest = 0xFFF555;

    @Entry
    public static HashMap<String, Integer> mapTest = new HashMap<>() {{
       put("test", 1);
    }};
}
