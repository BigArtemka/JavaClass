package decimalNumber;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;

public class Number {
    private List<Integer> num = new ArrayList<>();
    private int scale;
    final private int base = 1000000000;

    Number(List<Integer> num, int scale) {
        this.num = num;
        this.scale = scale;
    }

    Number(@NotNull String num) {
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

    Number(@NotNull Integer num) {
        int n = num;
        if (n == 0) this.num.add(0);
        while (n != 0) {
            this.num.add(n % base);
            n /= base;
        }
    }

    Number(@NotNull Long num) {
        long n = num;
        if (n == 0) this.num.add(0);
        while (n != 0) {
            this.num.add((int) (n % base));
            n /= base;
        }
    }

    Number(@NotNull Float num) {
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
        num.set(num.size() - 1, num.get(num.size() - 1 / (int) pow(10, scale % 9)));
        for (int i = 0; i < num.size(); i++)
            res += num.get(i) * (int) pow(base, i);

        return res;
    }

    public long toLong() {
        long res = 0L;
        for (int i = 0; i <= scale - 9; i += 9)
            num.remove(0);
        num.set(num.size() - 1, num.get(num.size() - 1 / (int) pow(10, scale % 9)));
        for (int i = 0; i < num.size(); i++)
            res += num.get(i) * (long) pow(base, i);

        return res;
    }


    public Number mul(@NotNull Number other) {
        long counter = 0;
        ArrayList<Long> result = new ArrayList<>();
        for (int i = 0; i < this.num.size(); ++i)
            for (int j = 0; j < other.num.size() || counter != 0; ++j) {
                if (i + j >= result.size()) result.add(0L);
                long count = result.get(i + j) + this.num.get(i).longValue()
                        * (j < other.num.size() ? other.num.get(j).longValue() : 0) + counter;
                result.set(i + j, count % base);
                counter = count / base;
            }
        return new Number(toListInt(result), this.scale + other.scale);
    }

    public Number add(@NotNull Number other) {
        if (other.num.get(other.num.size() - 1) < 0) {
            other.num.set(other.num.size() - 1, other.num.get(other.num.size() - 1) * -1);
            return this.sub(other);
        }
        List<Integer> dec1;
        List<Integer> dec2;
        int maxScale;
        if (this.scale > other.scale) {
            dec1 = this.getAftPoint(0);
            dec2 = other.getAftPoint((this.scale - other.scale) % 9);
            maxScale = this.scale;
        } else {
            dec1 = this.getAftPoint((other.scale - this.scale) % 9);
            dec2 = other.getAftPoint(0);
            maxScale = other.scale;
        }
        List<Integer> cel1 = this.getBefPoint();
        List<Integer> cel2 = other.getBefPoint();
        while (dec1.size() != dec2.size() || dec1.size() < 1) {
            if (dec1.size() < dec2.size())
                dec1.add(0, 0);
            else dec2.add(0, 0);
        }
        List<Integer> resDec = listAdd(dec1, dec2);
        List<Integer> resCel = listAdd(cel1, cel2);

        //Если результат сложения дробных частей >= 1.0, перебрасываем целую часть в resCel
        int resDecLast = resDec.get(resDec.size() - 1);     //последний элемент resDec
        int countOfDigitDecLast = getCountsOfDigits(dec1.get(dec1.size() - 1)); //количество цифр в последнем элементе dec1
        if (resDec.size() > dec1.size()) {
            resCel = listAdd(resCel, resDec.subList(resDec.size() - 1, resDec.size()));
            resDec.remove(resDec.size() - 1);
        } else if (getCountsOfDigits(resDecLast) > countOfDigitDecLast) {
            List<Integer> o = new ArrayList<>();
            o.add(1);
            resDec.set(resDec.size() - 1, resDecLast % (int) pow(10, countOfDigitDecLast));
            resCel = listAdd(resCel, o);
        }

        //Избавляемся от незначащих нулей в дробной части
        while (!resDec.isEmpty() && resDec.get(0) == 0)
            if (resDec.size() == 1) {
                resDec.remove(0);
                maxScale = 0;
            } else {
                resDec.remove(0);
                maxScale -= 9;
            }
        while (resDec.get(0) % 10 == 0) {
            resDec.set(0, resDec.get(0) / 10);
            maxScale--;
        }

        //соединяем resDec и resCel в один ArrayList
        if (resDec.isEmpty()) resDec.addAll(resCel);
        else
            for (int i = 0; i < resCel.size(); i++) {
                resDec.set((resDec.size() - 1), resDec.get(resDec.size() - 1) + resCel.get(i)
                        % (int) pow(10, 9 - maxScale % 9) * (int) pow(10, maxScale % 9));
                int newElem = resCel.get(i) / (int) pow(10, 9 - maxScale % 9);
                if (newElem != 0) resDec.add(newElem);
            }
        return new Number(resDec, maxScale);
    }


    public Number sub(@NotNull Number other) {
        if (other.num.get(other.num.size() - 1) < 0) {
            other.num.set(other.num.size() - 1, other.num.get(other.num.size() - 1) * -1);
            return this.add(other);
        }
        List<Integer> dec1;
        List<Integer> dec2;
        int maxScale;
        if (this.scale > other.scale) {
            dec1 = this.getAftPoint(0);
            dec2 = other.getAftPoint((this.scale - other.scale) % 9);
            maxScale = this.scale;
        } else {
            dec1 = this.getAftPoint((other.scale - this.scale) % 9);
            dec2 = other.getAftPoint(0);
            maxScale = other.scale;
        }
        List<Integer> cel1 = this.getBefPoint();
        List<Integer> cel2 = other.getBefPoint();
        while (dec1.size() != dec2.size() || dec1.size() < 1) {
            if (dec1.size() < dec2.size())
                dec1.add(0, 0);
            else dec2.add(0, 0);
        }

        List<Integer> resDec;
        List<Integer> resCel;
        int cel1Last = 0;
        int cel2Last = 0;
        if (cel1.size() > 0) cel1Last = cel1.get(cel1.size() - 1);
        if (cel2.size() > 0) cel2Last = cel2.get(cel2.size() - 1);
        boolean celSizesEquals = cel2.size() == cel1.size();

        /*
        Если второе число больше первого, то мы вычитаем из него первое число,
        при этом если деситичная часть первого числа больше, чем второго, мы
        вычитаем из resCel единицу. Результат получится отрицательным, поэтому
        умножаем старший элемент результата на -1.
        Если наоборот, то вычитаем из первого второе, при этом если десятичная
        часть второго числа больше, чем первого, мы вычитаем из resCel единицу.
        Также избавляемся от незначимых нулей целой части числа.
         */
        if (cel2.size() > cel1.size() ||
                celSizesEquals && cel2Last > cel1Last ||
                celSizesEquals && cel2Last == cel1Last
                        && dec2.get(dec2.size() - 1) > dec1.get(dec1.size() - 1)) {
            resCel = listSub(cel2, cel1);
            resDec = listSub(dec2, dec1);
            if (dec1.get(dec1.size() - 1) > dec2.get(dec2.size() - 1)) {
                List<Integer> s = new ArrayList<>();
                s.add(1);
                resCel = listSub(resCel, s);
            }
            while (!resCel.isEmpty() && resCel.get(0) == 0) resCel.remove(0);
            if (resCel.isEmpty()) resDec.set(resDec.size() - 1, resDec.get((resDec.size() - 1)) * -1);
            else resCel.set(resCel.size() - 1, resCel.get(resCel.size() - 1) * -1);
        } else {
            resCel = listSub(cel1, cel2);
            resDec = listSub(dec1, dec2);
            if (dec1.get(dec1.size() - 1) < dec2.get(dec2.size() - 1)) {
                List<Integer> s = new ArrayList<>();
                s.add(1);
                resCel = listSub(resCel, s);
        }
            while (!resCel.isEmpty() && resCel.get(0) == 0) resCel.remove(0);
        }

        //Избавляемся от незначащих нулей в дробной части
        while (!resDec.isEmpty() && resDec.get(0) == 0)
            if (resDec.size() == 1) {
                resDec.remove(0);
                maxScale = 0;
            } else {
                resDec.remove(0);
                maxScale -= 9;
            }
        while (resDec.get(0) % 10 == 0) {
            resDec.set(0, resDec.get(0) / 10);
            maxScale--;
        }

        //соединяем resDec и resCel в один ArrayList
        if (resDec.isEmpty()) resDec.addAll(resCel);
        else
            for (int i = 0; i < resCel.size(); i++) {
                resDec.set((resDec.size() - 1),
                        (resCel.get(i) < 0 ? -resDec.get(resDec.size() - 1) : resDec.get(resDec.size() - 1))
                                + resCel.get(i) % (int) pow(10, 9 - maxScale % 9) * (int) pow(10, maxScale % 9));
                int newElem = resCel.get(i) / (int) pow(10, 9 - maxScale % 9);
                if (newElem != 0) resDec.add(newElem);
            }
        return new Number(resDec, maxScale);
    }

    private List<Integer> getBefPoint() {
        List<Integer> res = new ArrayList<>();
        int counter = num.size() * 9;
        for (int i = num.size() - 1; i >= 0; i--) {
            counter -= 9;
            res.add(0, num.get(i) / (int) pow(10, scale % 9)
                    + (i < num.size() - 1 ? num.get(i + 1) % (int) pow(10, scale % 9) : 0) *
                    (int) pow(10, 9 - scale % 9));
            if (counter <= scale) break;
        }
        if (res.get(res.size() - 1) == 0) res.remove(res.size() - 1);
        return res;
    }

    private List<Integer> getAftPoint(int addedZeros) {
        List<Integer> res = new ArrayList<>();
        int counter = 0;
        int a = (int) pow(10, 9 - addedZeros);
        int b = (int) pow(10, addedZeros);
        for (int i = 0; i < num.size(); i++) {
            counter += 9;
            if (counter > scale) {
                //отделяем от элемента дробную часть и заносим ее в массив
                int decPart = num.get(i) % (int) pow(10, scale % 9);
                res.add(decPart % a * b +
                        (i > 0 ? num.get(i - 1) / a : 0));
                if (decPart / a != 0)
                    res.add(decPart / a);
                break;
            } else {
                res.add(num.get(i) % a * b +
                        (i > 0 ? num.get(i - 1) / a : 0));
            }
        }
        return res;
    }

    private static List<Integer> listAdd(@NotNull List<Integer> th, @NotNull List<Integer> other) {
        int counter = 0;
        int base = 1000000000;
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
        int base = 1000000000;
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < max(th.size(), other.size()); i++) {
            res.add(0);
            res.set(i, (i < th.size() ? th.get(i) : 0) - counter - (i < other.size() ? other.get(i) : 0));
            if (res.get(i) < 0)
                counter = 1;
            else counter = 0;
            if (counter == 1)
                if (i == max(th.size(), other.size()) - 1)
                    res.set(i, res.get(i) + (int) pow(10, getCountsOfDigits(res.get(i))));
                else
                    res.set(i, res.get(i) + base);
        }
        return res;
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
