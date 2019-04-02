package decimalNumber;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberTest {

    @org.junit.Test
    public void mul() {

    }

    @org.junit.Test
    public void add() {
        Number n1 = new Number("213.004");
        Number n2 = new Number("0.12342142");
        assertEquals( n1.add(n2),new Number("213.12742142"));

    }
}