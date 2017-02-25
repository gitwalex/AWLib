package de.aw.awlib.pdf;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

import android.support.annotation.NonNull;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Erstellt ein pdf.
 */
public abstract class PDFDocument {
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static Font redFont =
            new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private final Document mDocument;
    private final PdfWriter writer;
    private String header;
    private String footer;

    /**
     * @param filename
     *         Names des Files. .pdf wird angehaengt
     * @throws FileNotFoundException
     *         Wenn das File nicht gefunden wird
     * @throws DocumentException
     *         Wenn das pdf nicht erstellt werden kann
     */
    public PDFDocument(String filename) throws FileNotFoundException, DocumentException {
        mDocument = new Document(PageSize.A4);
        writer = PdfWriter.getInstance(mDocument, new FileOutputStream(filename + ".pdf"));
        writer.setPageEvent(new PDFEventHelper());
        mDocument.open();
    }

    /**
     * Fuegt leere Zeilen hinzu
     *
     * @param paragraph
     *         Paragraph
     * @param number
     *         Anzahl der leeren Zeilen
     */
    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    /**
     * Setzt die Meta-Daten
     *
     * @param titel
     *         Titel des pdf
     * @param subject
     *         Inhalt des pdf
     */
    protected void addMetaData(@NonNull String titel, @NonNull String subject) {
        mDocument.addTitle(titel);
        mDocument.addSubject(subject);
        mDocument.addAuthor("Alexander Winkler");
        mDocument.addCreator("Alexander Winkler");
    }

    /**
     * Erstellt die Ueberschrift
     *
     * @param documentTitle
     *         Titel
     * @param ueberschrift
     *         Uebeschrift
     * @throws DocumentException
     */
    protected void addTitlePage(@NonNull String documentTitle, @NonNull String ueberschrift)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph(documentTitle, catFont));
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(ueberschrift, smallBold));
        addEmptyLine(preface, 1);
        mDocument.add(preface);
    }

    /**
     * Ist nach fertigstellung des pdf zu rufen.
     */
    public void close() {
        mDocument.close();
    }

    /**
     * Erstellt eine Tabelle im pdf
     *
     * @param columnHeaders
     *         Header der Tabellenspalten
     * @param rowCount
     *         Anzahl der Tabellenzeilen
     * @throws DocumentException
     *         wenn das Document nicht erstellt werden kann.
     */
    protected void createTable(@NonNull String[] columnHeaders, int rowCount)
            throws DocumentException {
        PdfPTable table = new PdfPTable(columnHeaders.length);
        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);
        for (String columnHeader : columnHeaders) {
            PdfPCell c1 = new PdfPCell(new Phrase(columnHeader));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnHeaders.length; column++) {
                table.addCell(getCellContent(row, column));
            }
        }
        mDocument.add(table);
    }

    /**
     * Wird aus {@link PDFDocument#createTable(String[], int)} gerufen und muss ueberschrieben
     * werden.
     *
     * @param row
     *         aktuelle Reihe der Tabelle
     * @param column
     *         aktuelle Spalte der Tabelle
     * @return Inhalt der Zelle
     *
     * @throws IllegalStateException
     *         wenn diese Methode nicht ueberschrieben wurde.
     */
    protected String getCellContent(int row, int column) {
        throw new IllegalStateException(
                "Methode muss bei Nutzung von createTable (...) ueberschrieben werden!");
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    private class PDFEventHelper extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
        private final SimpleDateFormat fmt =
                new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", Locale.getDefault());

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            if (header != null) {
                Phrase headerPhrase = new Phrase(header, footerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, headerPhrase,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() + 10, 0);
            }
            Date date = new Date(System.currentTimeMillis());
            String mFooter = "created: " + fmt.format(date);
            if (footer != null) {
                mFooter = footer + ", " + mFooter;
            }
            Phrase footer = new Phrase(mFooter, footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer,
                    document.right() - document.rightMargin(), document.bottom() - 20, 0);
        }
    }
}