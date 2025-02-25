import org.command.CmdClientParser;
import org.data.Vote;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


public class UserTest {
    public final String path = System.getProperty("user.dir")+"\\src\\test\\java\\";
    @Test
    public void checkSerialize(){
        ArrayList<String> o = new ArrayList<>();
        ArrayList<ArrayList<String>> l = new ArrayList<>();
        ArrayList<String> k = new ArrayList<>();

        l.add(k);
        o.add("de");
        Vote toSer = new Vote("l","ok", "2", o,l);
        toSer.serializeAndWrite(path);
        Vote a = Vote.deserialize(path+ toSer.name+".vote");
        assertEquals(toSer.toString(), a.toString());
        new File(path+ toSer.name+".vote").delete();
    }

    @Test
    public void checkCmdParser(){
        String res1 = CmdClientParser.login("login -u ok".split(" "));
        String res2 = CmdClientParser.login("login -username ok".split(" "));
        String res3 = CmdClientParser.login("login ok".split(" "));
        String res4 = CmdClientParser.login("login -u".split(" "));
        String[] res5 = CmdClientParser.view("create topic -t ok".split(" "));
        String[] res6 = CmdClientParser.view("create topic -v ok".split(" "));
        String[] res7 = CmdClientParser.view("create topic -t ok -v ok".split(" "));
        assertEquals("ok",res1);
        assertEquals("ok",res2);
        assertNull(res3);
        assertNull(res4);
        assertEquals("ok", res5[0]);
        assertNull(res6);
        assertEquals("ok", res7[0]);
        assertEquals("ok", res7[1]);
    }
}
