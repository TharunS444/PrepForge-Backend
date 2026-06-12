package com.platform.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.platform.entity.Submission;
import com.platform.entity.User;
import com.platform.entity.UserBadge;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateProgressReport(User user, List<Submission> submissions, List<UserBadge> badges) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("Placement Platform Progress Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // User Info Section
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            document.add(new Paragraph("Student Information", headerFont));
            document.add(new Paragraph("Name: " + user.getName(), normalFont));
            document.add(new Paragraph("Email: " + user.getEmail(), normalFont));
            document.add(new Paragraph("Total Score: " + user.getScore() + " pts", normalFont));
            document.add(new Paragraph("Current Streak: " + user.getStreak() + " days", normalFont));
            document.add(new Paragraph(" ")); // Blank line

            // Badges Section
            document.add(new Paragraph("Achievements Unlocked", headerFont));
            if (badges.isEmpty()) {
                document.add(new Paragraph("No achievements unlocked yet.", normalFont));
            } else {
                for (UserBadge b : badges) {
                    document.add(new Paragraph("• " + b.getTitle() + " - " + b.getDescription(), normalFont));
                }
            }
            document.add(new Paragraph(" "));

            // Submissions Table
            document.add(new Paragraph("Recent Submissions", headerFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 2f, 2f});

            // Table Headers
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerBgColor = new BaseColor(99, 102, 241);

            String[] headers = {"Problem", "Language", "Status", "Date"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Rows
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            int limit = Math.min(submissions.size(), 20); // Show max 20 recent
            
            if (limit == 0) {
                PdfPCell cell = new PdfPCell(new Phrase("No submissions yet."));
                cell.setColspan(4);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            } else {
                for (int i = 0; i < limit; i++) {
                    Submission s = submissions.get(i);
                    table.addCell(new PdfPCell(new Phrase(s.getQuestion().getTitle(), normalFont)));
                    table.addCell(new PdfPCell(new Phrase(s.getLanguage(), normalFont)));
                    
                    // Status coloring
                    Font statusFont = FontFactory.getFont(FontFactory.HELVETICA, 11, 
                            "ACCEPTED".equals(s.getStatus()) ? new BaseColor(16, 185, 129) : BaseColor.RED);
                    table.addCell(new PdfPCell(new Phrase(s.getStatus(), statusFont)));
                    
                    table.addCell(new PdfPCell(new Phrase(s.getSubmittedAt().format(dtf), normalFont)));
                }
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
