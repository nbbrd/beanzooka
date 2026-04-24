package internal.swing;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * A narrow vertical strip placed beside a table that shows an at-a-glance
 * summary of every change made to the list relative to the snapshot taken when
 * the editor dialog was opened, mirroring the change-bar gutter found in code
 * editors such as IntelliJ IDEA.
 *
 * <p>Markers are painted at the exact y-coordinate of their corresponding table
 * row so they stay visually aligned with the data:
 * <ul>
 *   <li><b>Green</b> — row was added (not present in the original list).</li>
 *   <li><b>Red</b> (thin) — row was deleted (present in the original list but
 *       since removed).</li>
 *   <li>No marker — row is unchanged.</li>
 * </ul>
 *
 * <p>Changes are detected with a <em>Longest Common Subsequence</em> (LCS)
 * algorithm so that deleting a row only marks that row as deleted; rows below
 * it that merely shifted up are correctly left unmarked.
 *
 * <p>Clicking a marker scrolls the table to the corresponding row and selects
 * it. Clicks on empty space are ignored. The cursor switches to a hand pointer
 * only when hovering over an actual marker.
 *
 * @author Philippe Charles
 */
final class DiffBarOverlay extends JComponent implements TableModelListener {

    private static final int BAR_WIDTH = 12;
    private static final int MIN_STRIPE_HEIGHT = 2;

    private static final Color COLOR_ADDED = new Color(98, 181, 67);
    private static final Color COLOR_DELETED = new Color(188, 74, 74);

    private final List<?> original;
    private final JTable table;

    DiffBarOverlay(List<?> original, JTable table) {
        this.original = original;
        this.table = table;
        setPreferredSize(new Dimension(BAR_WIDTH, 0));
        setOpaque(true);
        MouseAdapter handler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = findRowAt(e.getY());
                if (row >= 0) {
                    navigateTo(row);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean onMarker = findRowAt(e.getY()) >= 0;
                setCursor(Cursor.getPredefinedCursor(onMarker ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    private int findRowAt(int clickY) {
        @SuppressWarnings("unchecked")
        List<Object> current = (List<Object>) ((ListTableModel<?>) table.getModel()).getRows();
        int headerHeight = table.getTableHeader() != null ? table.getTableHeader().getHeight() : 0;
        int rowHeight = table.getRowHeight();
        int stripeH = Math.max(MIN_STRIPE_HEIGHT, rowHeight);

        int[] mapping = computeMapping(original, current);
        boolean[] originalMatched = new boolean[original.size()];
        int clickedCurrentRow = -1;

        // Collect matched originals and check green (added) markers
        for (int i = 0; i < current.size(); i++) {
            if (mapping[i] != -1) {
                originalMatched[mapping[i]] = true;
            } else {
                int y = headerHeight + i * rowHeight;
                if (clickedCurrentRow < 0 && clickY >= y && clickY < y + stripeH) {
                    clickedCurrentRow = i;
                }
            }
        }
        if (clickedCurrentRow >= 0) {
            return clickedCurrentRow;
        }

        // Check red (deleted) markers — navigate to nearest surviving row
        for (int i = 0; i < original.size(); i++) {
            if (!originalMatched[i]) {
                int y = headerHeight + i * rowHeight;
                if (clickY >= y && clickY < y + MIN_STRIPE_HEIGHT) {
                    return Math.max(0, Math.min(i, current.size() - 1));
                }
            }
        }

        return -1;
    }

    private void navigateTo(int modelRow) {
        int viewRow = table.convertRowIndexToView(modelRow);
        if (viewRow >= 0) {
            table.setRowSelectionInterval(viewRow, viewRow);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        // Background — match the scrollbar track when available
        Color track = UIManager.getColor("ScrollBar.track");
        g.setColor(track != null ? track : getBackground());
        g.fillRect(0, 0, w, h);

        // Left separator line
        Color separator = UIManager.getColor("Separator.foreground");
        g.setColor(separator != null ? separator : Color.GRAY);
        g.drawLine(0, 0, 0, h - 1);

        @SuppressWarnings("unchecked")
        List<Object> current = (List<Object>) ((ListTableModel<?>) table.getModel()).getRows();

        if (original.isEmpty() && current.isEmpty()) {
            return;
        }

        // Reserve space so markers align with the data rows
        int headerHeight = table.getTableHeader() != null ? table.getTableHeader().getHeight() : 0;
        int availableHeight = h - headerHeight;
        if (availableHeight <= 0) {
            return;
        }

        // Use the actual row height so markers align with visible rows
        int rowHeight = table.getRowHeight();
        int stripeH = Math.max(MIN_STRIPE_HEIGHT, rowHeight);

        // LCS-based diff: matched rows are unchanged regardless of index shift
        int[] mapping = computeMapping(original, current);
        boolean[] originalMatched = new boolean[original.size()];

        // Current-row markers: added (not matched by LCS)
        for (int i = 0; i < current.size(); i++) {
            int y = headerHeight + i * rowHeight;
            if (y >= h) {
                break;
            }
            if (mapping[i] == -1) {
                g.setColor(COLOR_ADDED);
                g.fillRect(1, y, w - 1, stripeH);
            } else {
                originalMatched[mapping[i]] = true;
            }
        }

        // Deletion markers: original items not matched by LCS
        for (int i = 0; i < original.size(); i++) {
            if (!originalMatched[i]) {
                int y = headerHeight + i * rowHeight;
                if (y >= h) {
                    break;
                }
                g.setColor(COLOR_DELETED);
                g.fillRect(1, y, w - 1, MIN_STRIPE_HEIGHT);
            }
        }
    }

    /**
     * Computes LCS-based mapping: for each index {@code j} in {@code current},
     * returns the matched index in {@code original}, or {@code -1} if unmatched.
     */
    private static int[] computeMapping(List<?> original, List<?> current) {
        int n = original.size();
        int m = current.size();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (original.get(i).equals(current.get(j))) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }
        int[] result = new int[m];
        Arrays.fill(result, -1);
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (original.get(i).equals(current.get(j))) {
                result[j] = i;
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }
}

