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

import static internal.swing.ListTableDescriptor.computeIndex;
import static internal.swing.ListTableDescriptor.move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ListTableDescriptorTest {

    @Test
    public void testComputeIndex() {
        assertThat(computeIndex(0, new int[]{1, 2, 3})).isEqualTo(0);
        assertThat(computeIndex(1, new int[]{1, 2, 3})).isEqualTo(1);
        assertThat(computeIndex(2, new int[]{1, 2, 3})).isEqualTo(1);
        assertThat(computeIndex(3, new int[]{1, 2, 3})).isEqualTo(1);
        assertThat(computeIndex(4, new int[]{1, 2, 3})).isEqualTo(1);

        assertThat(computeIndex(0, new int[]{1, 3})).isEqualTo(0);
        assertThat(computeIndex(1, new int[]{1, 3})).isEqualTo(1);
        assertThat(computeIndex(2, new int[]{1, 3})).isEqualTo(1);
        assertThat(computeIndex(3, new int[]{1, 3})).isEqualTo(2);
        assertThat(computeIndex(4, new int[]{1, 3})).isEqualTo(2);
    }

    @Test
    public void testMove() {
        List<Character> list;
        int[] selection;

        list = listOf('0', '1', '2');
        selection = new int[]{1, 2};
        move(list, selection, computeIndex(0, selection));
        assertThat(list).containsExactly('1', '2', '0');

        list = listOf('0', '1', '2');
        selection = new int[]{1, 2};
        move(list, selection, computeIndex(1, selection));
        assertThat(list).containsExactly('0', '1', '2');

        list = listOf('0', '1', '2');
        selection = new int[]{1, 2};
        move(list, selection, computeIndex(2, selection));
        assertThat(list).containsExactly('0', '1', '2');

        list = listOf('0', '1', '2');
        selection = new int[]{1, 2};
        move(list, selection, computeIndex(3, selection));
        assertThat(list).containsExactly('0', '1', '2');

        list = listOf('0', '1', '2');
        selection = new int[]{0, 2};
        move(list, selection, computeIndex(0, selection));
        assertThat(list).containsExactly('0', '2', '1');

        list = listOf('0', '1', '2');
        selection = new int[]{0, 2};
        move(list, selection, computeIndex(1, selection));
        assertThat(list).containsExactly('0', '2', '1');

        list = listOf('0', '1', '2');
        selection = new int[]{0, 2};
        move(list, selection, computeIndex(2, selection));
        assertThat(list).containsExactly('1', '0', '2');

        list = listOf('0', '1', '2');
        selection = new int[]{0, 2};
        move(list, selection, computeIndex(3, selection));
        assertThat(list).containsExactly('1', '0', '2');
    }

    private static <X> List<X> listOf(X... list) {
        return new ArrayList<>(Arrays.asList(list));
    }
}
