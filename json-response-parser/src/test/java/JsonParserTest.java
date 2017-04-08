import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

public class JsonParserTest {

    private InputStream is = null;
    private CemPluginApiV2 plugin = null;

    /**
     * Prepare test: create plugin.
     */
    @Before
    public void setUp() {
        plugin = new CemJavaPlugin();
    }

    /**
     * After test: close file.
     */
    @After
    public void tearDown() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void testVersion() {
        Assert.assertEquals(false, plugin.initialize(0));
        Assert.assertEquals(false, plugin.initialize(1));
        Assert.assertEquals(true, plugin.initialize(2));
        Assert.assertEquals(false, plugin.initialize(3));
        Assert.assertEquals(false, plugin.initialize(-1));
        Assert.assertEquals(false, plugin.initialize(42));
    }

    @Test
    public void testJson() {

        String fileName = "target/test-classes/test.txt";
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {

            CemPluginOutput output = runPlugin(is);

            Assert.assertNotEquals(null, output);
            Assert.assertNotEquals(null, output.params);
            Assert.assertEquals(8, output.params.length);

            print(output.params);

            Assert.assertTrue(contains(output.params, "model", "XT1063"));
            Assert.assertTrue(contains(output.params, "osVersion", "4.4.4"));
            Assert.assertTrue(contains(output.params, "appVersion", "1.6.1"));
            Assert.assertTrue(contains(output.params, "hsv", "ql4hjgi34jgbqawenbo"));
            Assert.assertTrue(contains(output.params, "userId", "0"));
            Assert.assertTrue(contains(output.params, "modelVersion", "19"));
            Assert.assertTrue(contains(output.params, "osName", "Android"));
            Assert.assertTrue(contains(output.params, "deviceId", "369970"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testNested() {

        String fileName = "target/test-classes/testNested.txt";
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {

            CemPluginOutput output = runPlugin(is);

            Assert.assertNotEquals(null, output);
            Assert.assertNotEquals(null, output.params);
            Assert.assertEquals(8, output.params.length);
            
            print(output.params);
            
            Assert.assertTrue(contains(output.params, "model", "XT1063"));
            Assert.assertTrue(contains(output.params, "os#osVersion", "4.4.4"));
            Assert.assertTrue(contains(output.params, "app#appVersion", "1.6.1"));
            Assert.assertTrue(contains(output.params, "app#hsv", "ql4hjgi34jgbqawenbo"));
            Assert.assertTrue(contains(output.params, "userId", "0"));
            Assert.assertTrue(contains(output.params, "modelVersion", "19"));
            Assert.assertTrue(contains(output.params, "os#osName", "Android"));
            Assert.assertTrue(contains(output.params, "deviceId", "369970"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    private CemPluginOutput runPlugin(InputStream is) throws IOException {
        plugin.initialize(2);
        plugin.responseHeader(1, "Content-Type", "application/json");

        int result;
        int size = 100;
        byte[] bytes = new byte[size];
        int read = 0;

        // reads till the end of the stream
        while ((result = is.read(bytes, read, size - read)) != -1) {
            read = read + result;
            if (read == size) {
                plugin.responseBody(1, bytes);
                read = 0;
            }
        }

        if (read > 0) {
            byte[] rest = new byte[read];
            for (int i = 0; i < read; ++i) {
                rest[i] = bytes[i];
            }
            plugin.responseBody(1, rest);
        }

        return plugin.endResponse(1);
    }

    /**
     * Check if params contains a parameter with key and value.
     * @param params parameter array
     * @param key parameter key
     * @param value parameter value
     * @return assertTrue if params contains a parameter with key and value
     */
    private boolean contains(CemPluginOutput.CemParam[] params, String key, String value) {
        if (params == null) {
            return false;
        }

        for (int i = 0; i < params.length; ++i) {
            if (params[i].name.equals(key) && params[i].value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void print(CemPluginOutput.CemParam[] params) {
        if (params == null) {
            System.out.println("params == null!");
            return;
        }

        for (int i = 0; i < params.length; ++i) {
            System.out.println("  " + params[i].name + " = " + params[i].value);
        }
    }
}