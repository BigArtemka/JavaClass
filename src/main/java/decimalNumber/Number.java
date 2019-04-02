package decimalNumber;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Integer.max;
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
                String s = n[0] + n[1];
                for (int i = s.length(); i > 0; i -= 9) {
                    if (i - 9 < 0)
                        this.num.add(parseInt(s.substring(0, i)));
                    else this.num.add(parseInt(s.substring(i - 9, i)));
                }
            }

        } else throw new NumberFormatException("Not number");
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
        List<Integer> dec1 = this.getAftPoint();
        List<Integer> dec2 = other.getAftPoint();
        List<Integer> cel1 = this.getBefPoint();
        List<Integer> cel2 = other.getBefPoint();
        while (dec1.size() != dec2.size() || dec1.size() < 1) {
            if (dec1.size() < dec2.size())
                dec1.add(0, 0);
            else dec2.add(0, 0);
        }
        List<Integer> resDec = listAdd(dec1, dec2);
        List<Integer> resCel = listAdd(cel1, cel2);
        if (resDec.size() > dec1.size()) {
            resCel = listAdd(resCel, resDec.subList(resDec.size() - 1, resDec.size()));
            resDec.remove(resDec.size() - 1);
        }
        resDec.addAll(resCel);
        return new Number(resDec, max(scale, other.scale) + 9 - max(scale, other.scale) % 9);
    }


    public Number sub(@NotNull Number other) {
        if (other.num.get(other.num.size() - 1) < 0) {
            other.num.set(other.num.size() - 1, other.num.get(other.num.size() - 1) * -1);
            return this.add(other);
        }
        List<Integer> dec1 = this.getAftPoint();
        List<Integer> dec2 = other.getAftPoint();
        List<Integer> cel1 = this.getBefPoint();
        List<Integer> cel2 = other.getBefPoint();
        while (dec1.size() != dec2.size() || dec1.size() < 1) {
            if (dec1.size() < dec2.size())
                dec1.add(0, 0);
            else dec2.add(0, 0);
        }
        List<Integer> resDec;
        List<Integer> resCel;
        if (cel2.size() > cel1.size() || cel2.size() == cel1.size() && (cel2.get(cel2.size() - 1) > cel1.get(cel1.size() - 1)
                || (cel2.get(cel2.size() - 1) == cel1.get(cel1.size() - 1) && dec2.get(dec2.size() - 1) > dec1.get(dec1.size() - 1)))) {
            resCel = listSub(cel2, cel1);
            resDec = listSub(dec2, dec1);
            if (dec1.get(dec1.size() - 1) > dec2.get(dec2.size() - 1))
                resCel.set(0, resCel.get(0) - 1);
            resCel.set(resCel.size() - 1, resCel.get(resCel.size() - 1) * -1);
        }
        else {
            resCel = listSub(cel1, cel2);
            resDec = listSub(dec1, dec2);
            if (dec1.get(dec1.size() - 1) < dec2.get(dec2.size() - 1))
                resCel.set(0, resCel.get(0) - 1);
        }

        resDec.addAll(resCel);
        return new Number(resDec, max(scale, other.scale) + 9 - max(scale, other.scale) % 9);
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

    private List<Integer> getAftPoint() {
        List<Integer> res = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < num.size(); i++) {
            counter += 9;
            res.add(num.get(i) % (int) pow(10, scale % 9) * (int) pow(10, 9 - scale % 9)
                    + (i > 0 ? num.get(i - 1) / (int) pow(10, scale % 9) : 0));
            if (counter > scale) break;
        }
        if (res.get(res.size() - 1) == 0) res.remove(res.size() - 1);
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
            if (counter == 1) res.set(i, res.get(i) + base);
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
        return (number == 0) ? 1 : (int) Math.ceil(Math.log10(number + 0.5));
    }

    public static void main(String[] args) {
        Number n = new Number("1000.6");
        Number n1 = new Number("10000000000000000000000.5");
        System.out.println(n.sub(n1).toString());
    }


}
