package w.core;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.Global;
import w.web.message.ExecMessage;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Frank
 * @date 2024/6/30 20:43
 */
public class ExecuteTest {


    ChangeTarget target = new ChangeTarget();

    Swapper swapper = Swapper.getInstance();;

    @BeforeAll
    public static void setUp() throws Exception {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        Global.instrumentation = instrumentation;
        Global.fillLoadedClasses();
        System.setProperty("maxHit", "3");
    }

    @BeforeEach
    public void reset() {
        Global.reset();
    }


    @Test
    public void test() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ExecMessage msg = new ExecMessage();
        msg.setBody("{System.out.println(\"Hello Swapper!\");}");
        Assertions.assertTrue(swapper.swap(msg));
        ExecBundle.invoke();
    }
}