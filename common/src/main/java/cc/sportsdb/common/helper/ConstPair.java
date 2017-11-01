package cc.sportsdb.common.helper;

public final class ConstPair {

    private final Pair<Integer, String> pair;

    public ConstPair(Integer status, String message) {
        pair = Pair.make(status, message);
    }

    public static ConstPair make(Integer status, String message) {
        return new ConstPair(status, message);
    }

    public Integer getStatus() {
        return pair.getFirst();
    }

    public String getMessage() {
        return pair.getSecond();
    }
}
