package decimalNumber;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberTest {

    @Test
    public void mul() {
        Number n1 = new Number("9.999999999999");
        Number n2 = new Number(1.0);
        Number res = new Number("9.999999999999");
        assertEquals(res, n1.mul(n2));
    }

    @Test
    public void add() {
        Number n1 = new Number("5.59999999999999");
        Number n2 = new Number("5.00000000000001");
        Number res = new Number("10.6");
        assertEquals(res, n1.add(n2));
    }

    @Test
    public void sub() {
        Number n1 = new Number("2.4");
        Number n2 = new Number("3.6");
        assertEquals(new Number(-1.2), n1.sub(n2));
        assertEquals(new Number(1.2), n2.sub(n1));
        Number n3 = new Number("1000.4");
        Number n4 = new Number("988.65");
        assertEquals(new Number("11.75"), n3.sub(n4));
        assertEquals(new Number("-11.75"), n4.sub(n3));
        Number n5 = new Number("1.0" +
                "+");
        Number n6 = new Number("0.000000000000000000000000001");
    }
}