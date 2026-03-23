package net.oldmanyounger.shroud.client;

public final class CorruptionHeartRenderState {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    private CorruptionHeartRenderState() {
    }

    public static void setActive(boolean active) {
        ACTIVE.set(active);
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static void clear() {
        ACTIVE.set(false);
    }
}