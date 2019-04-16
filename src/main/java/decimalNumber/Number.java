package decimalNumber;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;

public class Number {
    private List<Integer> num = new ArrayList<>();
    private int scale;
    final private int base = 1000000000;

    public Number(List<Integer> num, int scale) {
        this.num = num;
        this.scale = scale;
    }

    public Number(@NotNull String num) {
        String[] n = num.split("\\.");
        if (n.length == 2) {
            if (Pattern.compile("^\\s*-?[0-9]+\\s*$").matcher(n[0]).matches()
                    && Pattern.compile("^\\s*[0-9]+\\s*$").matcher(n[1]).matches()) {
                scale = n[1].length();
                StringBuilder str = new StringBuilder(n[0] + n[1]);
                while (scale > 0 && str.charAt(str.length() - 1) == '0') {
                    str.deleteCharAt(str.length() - 1);
                    scale--;
                }
                boolean minus = str.charAt(0) == '-';
                if (minus) str.deleteCharAt(0);
                for (int i = str.length(); i > 0; i -= 9) {
                    if (i - 9 < 0)
                        this.num.add(parseInt(str.substring(0, i)));
                    else this.num.add(parseInt(str.substring(i - 9, i)));
                }
                if (minus) this.num.set(this.num.size() - 1, this.num.get(this.num.size() - 1) * -1);
            } else throw new NumberFormatException("Not number");

        } else throw new NumberFormatException("Not number");
    }

    public Number(@NotNull Integer num) {
        int n = num;
        if (n == 0) this.num.add(0);
        while (n != 0) {
            this.num.add(n % base);
            n /= base;
        }
    }

    public Number(@NotNull Long num) {
        long n = num;
        if (n == 0) this.num.add(0);
        while (n != 0) {
            this.num.add((int) (n % base));
            n /= base;
        }
    }

    public Number(@NotNull Float num) {
        Number n = new Number(num.toString());
        this.num = n.num;
        this.scale = n.scale;
    }

    Number(@NotNull Double num) {
        Number n = new Number(num.toString());
        this.num = n.num;
        this.scale = n.scale;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = num.size() - 1; i >= 0; i--) {
            int length = getCountsOfDigits(num.get(i));
            if (length < 9 && i != num.size() - 1)
                for (int j = 0; j < 9 - length; ++j)
                    str.append(0);
            str.append(num.get(i));
        }
        while (str.length() - scale <= 0) str.insert(0, '0');
        str.insert(str.length() - scale, '.');
        while (str.charAt(str.length() - 1) == '0'
                && str.charAt(str.length() - 2) != '.') str.deleteCharAt(str.length() - 1);
        while (str.charAt(0) == '0' && str.charAt(1) != '.') str.deleteCharAt(0);
        return str.toString();
    }

    public float toFloat() {
        return Float.parseFloat(this.toString());
    }

    public double toDouble() {
        return Double.parseDouble(this.toString());
    }

    public int toInt() {
        int res = 0;
        for (int i = 0; i <= scale - 9; i += 9)
            num.remove(0);
        num.set(num.size() - 1, num.get(num.size() - 1) / (int) pow(10, scale % 9));
        for (int i = 0; i < num.size(); i++)
            res += num.get(i) * (int) pow(base, i);

        return res;
    }

    public long toLong() {
        long res = 0L;
        for (int i = 0; i <= scale - 9; i += 9)
            num.remove(0);
        num.set(num.size() - 1, num.get(num.size() - 1) / (int) pow(10, scale % 9));
        for (int i = 0; i < num.size(); i++)
            res += num.get(i) * (long) pow(base, i);

        return res;
    }


    public Number mul(@NotNull Number other) {
        if (this.equals(new Number(0)) || other.equals(new Number(0))) return new Number(0);
        boolean thisIsNegative = this.num.get(this.num.size() - 1) < 0;
        boolean otherIsNegative = other.num.get(other.num.size() - 1) < 0;
        int sumScale = this.scale + other.scale;
        boolean minus = false;
        List<Integer> res;
        if (thisIsNegative && !otherIsNegative) {
            this.setNegative();
            res = listMul(this.num, other.num);
            minus = true;
            this.setNegative();
        } else
        if (otherIsNegative && !thisIsNegative) {
            other.setNegative();
            res = listMul(this.num, other.num);
            minus = true;
            other.setNegative();
        }
        else {
            res = listMul(this.num, other.num);
        }
        while (!res.isEmpty() && res.get(0) == 0)
            if (res.size() == 1) {
                res.remove(0);
                sumScale = 0;
            } else {
                res.remove(0);
                sumScale -= 9;
            }
        while (res.get(0) % 10 == 0) {
            res.set(0, res.get(0) / 10);
            sumScale--;
        }
        if (minus) return new Number(res, sumScale).setNegative();
        else
        return new Number(res, sumScale);
    }

    public Number add(@NotNull Number other) {
        boolean thisIsNegative = this.num.get(this.num.size() - 1) < 0;
        boolean otherIsNegative = other.num.get(other.num.size() - 1) < 0;
        if (otherIsNegative && thisIsNegative) {
            other.setNegative();
            this.setNegative();
            Number res = this.add(other);
            other.setNegative();
            this.setNegative();
            res.setNegative();
            return res;
        }
        if (otherIsNegative) {
            other.setNegative();
            Number res = this.sub(other);
            other.setNegative();
            return res;
        }
        if (thisIsNegative) {
            this.setNegative();
            Number res = other.sub(this);
            this.setNegative();
            return res;
        }
        return this.addOrSub(other, Number::listAdd);
    }

    public Number sub(@NotNull Number other) {
        boolean thisIsNegative = this.num.get(this.num.size() - 1) < 0;
        boolean otherIsNegative = other.num.get(other.num.size() - 1) < 0;
        if (otherIsNegative && thisIsNegative) {
            other.setNegative();
            this.setNegative();
            Number res = this.sub(other);
            other.setNegative();
            this.setNegative();
            res.setNegative();
            return res;
        }
        if (otherIsNegative) {
            other.setNegative();
            Number res = this.add(other);
            other.setNegative();
            return res;
        }
        if (thisIsNegative) {
            this.setNegative();
            Number res = other.add(this);
            this.setNegative();
            return res;
        }
        return this.addOrSub(other, Number::listSub);
    }

    //Добавление нулей к числу с меньшим количесвтом цифр
    private List<Integer> addZeros(int addedZeros) {
        if (addedZeros == 0) return this.num;
        List<Integer> res = new ArrayList<>();
        int a = (int) pow(10, 9 - addedZeros);
        int b = (int) pow(10, addedZeros);
        for (int i = 0; i < num.size(); i++) {
            res.add(num.get(i) % a * b +
                    (i > 0 ? num.get(i - 1) / a : 0));

        }
        if (num.get(num.size() - 1) / a > 0) res.add(num.get(num.size() - 1) / a);
        return res;
    }

    private static List<Integer> listMul(@NotNull List<Integer> th, @NotNull List<Integer> other) {
        long counter = 0;
        int base = 1000000000;
        ArrayList<Long> result = new ArrayList<>();
        for (int i = 0; i < th.size(); ++i)
            for (int j = 0; j < other.size() || counter != 0; ++j) {
                if (i + j >= result.size()) result.add(0L);
                long count = result.get(i + j) + th.get(i).longValue()
                        * (j < other.size() ? other.get(j).longValue() : 0) + counter;
                result.set(i + j, count % base);
                counter = count / base;
            }
        List<Integer> res;
        res = toListInt(result);
        return res;
    }

    private static List<Integer> listAdd(@NotNull List<Integer> th, @NotNull List<Integer> other) {
        int counter = 0;
        final int base = 1000000000;
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < max(th.size(), other.size()) || counter == 1; i++) {
            res.add(0);
            res.set(i, (i < th.size() ? th.get(i) : 0) + counter + (i < other.size() ? other.get(i) : 0));
            if (res.get(i) >= base)
                counter = 1;
            else counter = 0;
            if (counter == 1) res.set(i, res.get(i) - base);
        }
        return res;
    }

    private static List<Integer> listSub(@NotNull List<Integer> th, @NotNull List<Integer> other) {
        int counter = 0;
        final int base = 1000000000;
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < max(th.size(), other.size()) || counter == 1; i++) {
            res.add(0);
            res.set(i, (i < th.size() ? th.get(i) : 0) - counter - (i < other.size() ? other.get(i) : 0));
            if (res.get(i) < 0 && i < max(th.size(), other.size()) - 1)
                counter = 1;
            else counter = 0;
            if (counter == 1) res.set(i, res.get(i) + base);
        }
        return res;
    }

    private Number addOrSub(@NotNull Number other, BiFunction<List<Integer>, List<Integer>, List<Integer>> func) {
        List<Integer> num1;
        List<Integer> num2;
        int maxScale;
        if (this.scale > other.scale) {
            num1 = this.addZeros(0);
            num2 = other.addZeros((this.scale - other.scale) % 9);
            maxScale = this.scale;
        } else {
            num1 = this.addZeros((other.scale - this.scale) % 9);
            num2 = other.addZeros(0);
            maxScale = other.scale;
        }
        while (num1.size() != num2.size() || num1.size() < 1) {
            if (num1.size() < num2.size())
                num1.add(0, 0);
            else num2.add(0, 0);
        }
        List<Integer> res = func.apply(num1, num2);
        //Избавляемся от незначащих нулей в дробной части
        while (!res.isEmpty() && res.get(0) == 0)
            if (res.size() == 1) {
                res.remove(0);
                maxScale = 0;
            } else {
                res.remove(0);
                maxScale -= 9;
            }
        while (res.get(0) % 10 == 0) {
            res.set(0, res.get(0) / 10);
            maxScale--;
        }

        return new Number(res, maxScale);
    }

    private Number setNegative() {
        this.num.set(this.num.size() - 1, this.num.get(this.num.size() - 1) * -1);
        return this;
    }

    private static List<Integer> toListInt(@NotNull List<Long> a) {
        List<Integer> res = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        for (int i = a.size() - 1; i >= 0; i--) {
            int length = getCountsOfDigits(a.get(i));
            if (length < 9 && i != a.size() - 1)
                for (int j = 0; j < 9 - length; ++j)
                    str.append(0);
            str.append(a.get(i));
        }
        for (int i = str.length(); i > 0; i -= 9) {
            if (i - 9 < 0)
                res.add(parseInt(str.substring(0, i)));
            else res.add(parseInt(str.substring(i - 9, i)));
        }
        return res;
    }

    private static int getCountsOfDigits(long number) {
        int count = 0;
        long n = number;
        while (n != 0) {
            count++;
            n /= 10;
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Number number = (Number) o;
        return scale == number.scale &&
                Objects.equals(num, number.num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, scale);
    }

    public static void main(String[] args) {
    }


}
