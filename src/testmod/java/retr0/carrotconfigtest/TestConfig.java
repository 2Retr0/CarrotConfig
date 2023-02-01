package retr0.carrotconfigtest;

import retr0.carrotconfig.config.CarrotConfig;

public class TestConfig extends CarrotConfig {
    @Entry
    public static boolean booleanTest = false;

    @Entry(min = 0)
    public static int intTest = 45;

    @Entry(min = 0)
    public static float floatTest = 30.0f;

    @Entry(isColor = true)
    public static int colorTest = 0xFFF555;
}
