package beanzooka.core;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface ResourceFinder<R> {

    void addResourcesTo(@NonNull Consumer<? super R> consumer);

    default @NonNull List<R> findResources() {
        List<R> result = new ArrayList<>();
        addResourcesTo(result::add);
        return result;
    }

    static <R> @NonNull List<R> findResources(@NonNull List<ResourceFinder<R>> finders) {
        List<R> result = new ArrayList<>();
        for (ResourceFinder<R> finder : finders) {
            finder.addResourcesTo(result::add);
        }
        return result;
    }
}
