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

import java.util.function.Function;

/**
 *
 * @author Philippe Charles
 * @param <FROM>
 * @param <TO>
 */
public interface Converter<FROM, TO> {

    TO applyForward(FROM value) throws IllegalStateException;

    FROM applyBackward(TO value) throws IllegalStateException;

    Direction getDirection();

    enum Direction {
        BACKWARD, FORWARD, BOTH
    }

    static <T> Converter<T, T> identity() {
        return of(Function.identity(), Function.identity());
    }

    static <FROM, TO> Converter<FROM, TO> of(Function<FROM, TO> forward, Function<TO, FROM> backward) {
        return new Converter<FROM, TO>() {
            @Override
            public TO applyForward(FROM value) {
                return forward.apply(value);
            }

            @Override
            public FROM applyBackward(TO value) {
                return backward.apply(value);
            }

            @Override
            public Direction getDirection() {
                return Direction.BOTH;
            }
        };
    }
}
