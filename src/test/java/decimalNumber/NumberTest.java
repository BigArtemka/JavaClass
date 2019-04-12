package decimalNumber;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberTest {

    @Test
    public void mul() {
        Number n1 = new Number("9.999999999999");
        Number n2 = new Number(-1.0);
        assertEquals(new Number("-9.999999999999"), n1.mul(n2));
        Number n3 = new Number(5);
        Number n4 = new Number("10.00002");
        assertEquals(new Number("50.0001"), n3.mul(n4));
        Number n5 = new Number(0);
        assertEquals(n5, n1.mul(n5));
        Number n6 = new Number("3516189789.564845632");
        assertEquals(new Number("35161968219.44424761691264"), n6.mul(n4));
    }

    @Test
    public void add() {
        Number n1 = new Number("5.59999999999999");
        Number n2 = new Number("5.00000000000001");
        assertEquals(new Number(10.6), n1.add(n2));
        Number n3 = new Number("-10.6");
        Number n4 = new Number("-0.4");
        Number n5 = new Number("35.111111");
        assertEquals(new Number("-11.0"), n3.add(n4));
        assertEquals(new Number("5.19999999999999"), n1.add(n4));
        assertEquals(new Number("24.511111"), n3.add(n5));
        Number n6 = new Number("0.0");
        assertEquals(n2, n2.add(n6));
        Number n7 = new Number("1.111");
        assertEquals(new Number("6.71099999999999"), n7.add(n1));
    }

    @Test
    public void sub() {
        Number n1 = new Number("2.4");
        Number n2 = new Number("3.6");
        assertEquals(new Number(-1.2), n1.sub(n2));
        assertEquals(new Number(1.2), n2.sub(n1));
        Number n3 = new Number("-1000.4");
        Number n4 = new Number("-988.65");
        assertEquals(new Number("-11.75"), n3.sub(n4));
        Number n5 = new Number("1.0");
        Number n6 = new Number("0.000000000000000000000000001");
        assertEquals(new Number("0.999999999999999999999999999"), n5.sub(n6));
    }
}