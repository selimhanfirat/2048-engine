package ui;

import game.core.Board;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

public final class BoardRenderer {

    private BoardRenderer() {}

    private static final int TILE_W = 8;
    private static final int TILE_H = 3;
    private static final int GAP = 1;
    private static final int OUTER_PAD = 1;

    private static final int BOARD_BG = 180; // warm tan
    private static final int EMPTY_BG = 187; // muted beige-gray tile background
    private static final int FG_DARK = 238;  // dark brown/gray text
    private static final int FG_LIGHT = 231; // white text

    public static void render(Terminal terminal, Board board, String headerLine, String footerLine) {
        if (terminal == null || board == null) {
            return;
        }

        int termW = Math.max(1, terminal.getWidth());
        int termH = Math.max(1, terminal.getHeight());
        int n = board.getDimension();

        boolean hasHeader = headerLine != null && !headerLine.isBlank();
        boolean hasFooter = footerLine != null && !footerLine.isBlank();

        int headerH = hasHeader ? 1 : 0;
        int footerH = hasFooter ? 1 : 0;

        int boardW = n * TILE_W + (n - 1) * GAP + OUTER_PAD * 2;
        int boardH = n * TILE_H + (n - 1) * GAP + OUTER_PAD * 2;
        int totalH = headerH + boardH + footerH;

        int leftPad = Math.max(0, (termW - boardW) / 2);
        int topPad = Math.max(0, (termH - totalH) / 2);

        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.puts(InfoCmp.Capability.clear_screen);

        for (int i = 0; i < topPad; i++) {
            terminal.writer().println();
        }

        if (hasHeader) {
            writeCenteredPlainLine(terminal, headerLine.strip(), termW);
            terminal.writer().println();
        }

        for (int y = 0; y < boardH; y++) {
            terminal.writer().println(buildBoardRow(board, y, leftPad).toAnsi(terminal));
        }

        if (hasFooter) {
            terminal.writer().println();
            writeCenteredPlainLine(terminal, footerLine.strip(), termW);
        }

        terminal.flush();
    }

    private static AttributedStringBuilder buildBoardRow(Board board, int y, int leftPad) {
        int n = board.getDimension();
        int innerBoardY = y - OUTER_PAD;

        AttributedStringBuilder line = new AttributedStringBuilder();
        line.append(" ".repeat(leftPad));

        // Top/bottom outer padding rows
        if (y < OUTER_PAD || y >= OUTER_PAD + n * TILE_H + (n - 1) * GAP) {
            appendBoardSpaces(line, n * TILE_W + (n - 1) * GAP + OUTER_PAD * 2);
            return line;
        }

        // Left outer padding
        appendBoardSpaces(line, OUTER_PAD);

        for (int row = 0; row < n; row++) {
            int rowStart = row * (TILE_H + GAP);
            int rowEnd = rowStart + TILE_H;

            if (innerBoardY >= rowStart && innerBoardY < rowEnd) {
                int tileInnerY = innerBoardY - rowStart;
                int numberRow = TILE_H / 2;

                for (int col = 0; col < n; col++) {
                    int value = board.get(row, col);
                    appendTile(line, value, tileInnerY == numberRow);

                    if (col < n - 1) {
                        appendBoardSpaces(line, GAP);
                    }
                }

                appendBoardSpaces(line, OUTER_PAD);
                return line;
            }

            // Gap row between tile rows
            if (innerBoardY >= rowEnd && innerBoardY < rowEnd + GAP) {
                appendBoardSpaces(line, n * TILE_W + (n - 1) * GAP + OUTER_PAD);
                return line;
            }
        }

        appendBoardSpaces(line, n * TILE_W + (n - 1) * GAP + OUTER_PAD);
        return line;
    }

    private static void appendTile(AttributedStringBuilder line, int value, boolean showNumber) {
        TileStyle style = styleFor(value);
        String text = showNumber && value != 0 ? Integer.toString(value) : "";
        String centered = center(text, TILE_W);

        AttributedStyle ansi = AttributedStyle.DEFAULT
                .background(style.bg())
                .foreground(style.fg());

        if (style.bold()) {
            ansi = ansi.bold();
        }

        line.style(ansi);
        line.append(centered);
    }

    private static void appendBoardSpaces(AttributedStringBuilder line, int count) {
        line.style(AttributedStyle.DEFAULT.background(BOARD_BG).foreground(BOARD_BG));
        line.append(" ".repeat(Math.max(0, count)));
    }

    private static void writeCenteredPlainLine(Terminal terminal, String text, int width) {
        int pad = Math.max(0, (width - text.length()) / 2);
        terminal.writer().println(" ".repeat(pad) + text);
    }

    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }

        int total = width - text.length();
        int left = total / 2;
        int right = total - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static TileStyle styleFor(int value) {
        return switch (value) {
            case 0    -> new TileStyle(EMPTY_BG, EMPTY_BG, false);

            case 2    -> new TileStyle(230, FG_DARK, true); // very light cream
            case 4    -> new TileStyle(223, FG_DARK, true); // light tan

            case 8    -> new TileStyle(215, FG_LIGHT, true); // orange
            case 16   -> new TileStyle(209, FG_LIGHT, true); // deeper orange
            case 32   -> new TileStyle(203, FG_LIGHT, true); // red-orange
            case 64   -> new TileStyle(196, FG_LIGHT, true); // red

            case 128  -> new TileStyle(221, FG_LIGHT, true); // gold
            case 256  -> new TileStyle(220, FG_LIGHT, true); // gold
            case 512  -> new TileStyle(214, FG_LIGHT, true); // amber
            case 1024 -> new TileStyle(208, FG_LIGHT, true); // orange-gold
            case 2048 -> new TileStyle(226, FG_LIGHT, true); // bright yellow

            default   -> new TileStyle(227, FG_LIGHT, true); // very bright yellow for >2048
        };
    }

    private record TileStyle(int bg, int fg, boolean bold) {}
}