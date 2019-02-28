/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.swing;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author Philippe Charles
 * @param <V>
 * @param <F>
 */
public interface Accessor<V, F> {

    boolean canRead(V value);

    F read(V value) throws IllegalStateException;

    boolean canWrite(V value);

    V write(V value, F field) throws IllegalStateException;

    static <V, F> Accessor<V, F> of(Function<V, F> reader, BiFunction<V, F, V> writer) {
        return new Accessor<V, F>() {
            @Override
            public boolean canRead(V value) {
                return reader != null;
            }

            @Override
            public F read(V obj) throws IllegalStateException {
                if (!canRead(obj)) {
                    throw new IllegalStateException();
                }
                return reader.apply(obj);
            }

            @Override
            public boolean canWrite(V value) {
                return writer != null;
            }

            @Override
            public V write(V value, F field) throws IllegalStateException {
                if (!canWrite(value)) {
                    throw new IllegalStateException();
                }
                return writer.apply(value, field);
            }
        };
    }

    static <V, F> Accessor<V, F> readOnly(Function<V, F> reader) {
        return of(reader, null);
    }

    static <V, F> Accessor<V, F> writeOnly(BiFunction<V, F, V> writer) {
        return of(null, writer);
    }
}
