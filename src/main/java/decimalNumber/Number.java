package decimalNumber;



import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;

public class Number {
    private List<Integer> num = new ArrayList<Integer>();
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
                this.scale = n[1].length();
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
        ArrayList<Long> result = new ArrayList<Long>();
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
        List<Integer> dec1 = this.getBefPoint();
        List<Integer> dec2 = other.getBefPoint();
        List<Integer> cel1 = this.getAftPoint();
        List<Integer> cel2 = other.getAftPoint();
        return new Number(listAdd(cel1, cel2), scale);

    }

    private List<Integer> getBefPoint() {
        List<Integer> res = new ArrayList<Integer>();
        int counter = 0;
        for (int i = 0; i < num.size(); i += 1) {
            counter += 9;
            if (counter > scale) {
                res.add((int) (num.get(i) % Math.pow(10, scale % 9)));
                break;
            }
            else res.add(num.get(i));
        }
        return res;
    }

    private List<Integer> getAftPoint() {
        List<Integer> res = new ArrayList<Integer>();
        int counter = num.size() * 9;
        for (int i = num.size() - 1; i >= 0; i--) {
            counter -= 9;
            if (counter < scale) {
                res.add((int) (this.num.get(i) / Math.pow(10, scale - counter)));
                break;
            }
            else res.add(num.get(i));
        }
        return res;
    }

    private static List<Integer> listAdd(@NotNull List<Integer> th, @NotNull List<Integer> other) {
        int counter = 0;

        int base = 1000000000;
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < max(th.size(), other.size()) || counter == 1; i++) {
            res.add(0);
            res.set(i,  th.get(i) + counter + (i < other.size() ? other.get(i) : 0));
            if (res.get(i) >= base)
                counter = 1;
            else counter = 0;
            if (counter == 1) res.set(i, res.get(i) - base);
        }
        return res;
    }

    private static List<Integer> toListInt(@NotNull List<Long> a) {
        List<Integer> res = new ArrayList<Integer>();
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
        Number n = new Number("4367365465416516216464.0");
        Number n1 = new Number("4564565465.0");
        System.out.println(n.add(n1).toString());
    }
}
