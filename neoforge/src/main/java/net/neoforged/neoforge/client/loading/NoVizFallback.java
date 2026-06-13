package net.neoforged.neoforge.client.loading;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class NoVizFallback {
    private NoVizFallback() {}

    public static LongSupplier windowHandoff(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        return () -> 0L;
    }

    public static boolean windowPositioning(Optional<Object> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
        return false;
    }

    public static <T> Supplier<T> loadingOverlay(Supplier<?> minecraft, Supplier<?> reloadInstance, Consumer<Optional<Throwable>> errorHandler, boolean fade) {
        return () -> null;
    }

    public static String glVersion() {
        return "3.2";
    }
}
