import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;

public class Testing {

    // some tests for locally testing methods in the server
    @Test
    public void typeWrong() {
        JSONObject req = new JSONObject();
        req.put("type1", "echo");

        JSONObject res = SockServer.testField(req, "type");

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field type does not exist in request");
    }

    @Test
    public void echoCorrect() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals("echo", res.getString("type"));
        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("echo"), "Here is your echo: whooooo");
    }

    @Test
    public void echoErrors() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data1", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field data does not exist in request");

        JSONObject req2 = new JSONObject();
        req2.put("type", "echo");
        req2.put("data", 33);
        JSONObject res2 = SockServer.echo(req2);

        assertEquals(false, res2.getBoolean("ok"));
        assertEquals(res2.getString("message"), "Field data needs to be of type: String");

        JSONObject req3 = new JSONObject();
        req3.put("type", "echo");
        req3.put("data", true);
        JSONObject res3 = SockServer.echo(req3);

        assertEquals(res3.getBoolean("ok"), false);
        assertEquals(res3.getString("message"), "Field data needs to be of type: String");
    }

    @Test
    public void charCountWithoutSpecificCharacter() {
        JSONObject req = new JSONObject();
        req.put("type", "charcount");
        req.put("findchar", false);
        req.put("count", "Hello, World!");

        JSONObject res = SockServer.charCount(req);

        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("result"), "Count of characters: 13");
    }

    @Test
    public void charCountWithSpecificCharacter() {
        JSONObject req = new JSONObject();
        req.put("type", "charcount");
        req.put("findchar", true);
        req.put("find", 'o');
        req.put("count", "Hello, World!");

        JSONObject res = SockServer.charCount(req);

        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("result"), "Count of 'o' characters: 2");
    }

    @Test
    public void storyboardAdd() {
        JSONObject req = new JSONObject();
        req.put("type", "storyboard");
        req.put("view", false);
        req.put("name", "User123");
        req.put("story", "This is a test story.");

        JSONObject res = SockServer.storyboard(req);

        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("result"), "Your sentence has been added!");
    }

    @Test
    public void storyboardView() {
        JSONObject req = new JSONObject();
        req.put("type", "storyboard");
        req.put("view", true);

        JSONObject res = SockServer.storyboard(req);

        assertEquals(res.getBoolean("ok"), true);
        // You should adapt this based on your expected response
        assertTrue(res.getString("result").contains("User1: Story1"));
        assertTrue(res.getString("result").contains("User2: Story2"));
        assertTrue(res.getString("result").contains("User3: Story3"));
    }
}
